package com.hindu.pooja.feature.ramakoti

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.hindu.pooja.feature.ramakoti.data.CertificateInput
import com.hindu.pooja.feature.ramakoti.data.CertificateRepository
import com.hindu.pooja.feature.ramakoti.data.RamakotiExportUploader
import com.hindu.pooja.feature.ramakoti.data.RamakotiExportUploader.ExportType
import com.hindu.pooja.feature.ramakoti.data.RamakotiRepository
import com.hindu.pooja.feature.ramakoti.prefs.RamakotiPreferences
import com.hindu.pooja.feature.ramakoti.sync.RamakotiSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.floor

private const val BATCH_SIZE = 108
private const val CRORE_SIZE = 10_000_000

data class RamakotiUiState(
    val totalCount: Long = 0L,
    val lifetimeCount: Int = 0,
    val currentBatchCount: Int = 0,
    val currentBatchNumber: Int = 1,
    val currentCrore: Int = 1,
    val language: String = "en",
    val targetCount: Int = 10_000_000,
    val targetReached: Boolean = false,
    val isIncrementBusy: Boolean = false,
    val showCelebration: Boolean = false,

    // certificate states
    val isIssuingCertificate: Boolean = false,
    val certificateUrl: String? = null,
    val certificateError: String? = null,
    val showNextTargetPrompt: Boolean = false,

    // legacy/compat
    val showTargetCompleteDialog: Boolean = false,
    val lastExportUrl: String? = null,
    val lastCertificateId: String? = null,
    val lastLocalFile: java.io.File? = null,

    val error: String? = null
)

@HiltViewModel
class RamakotiViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val prefs: RamakotiPreferences
) : ViewModel() {

    // Use your singletons (matches your working setup)
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val repo = RamakotiRepository(auth, db)
    private val certRepo = CertificateRepository()
    private val sync = RamakotiSyncManager(auth, db)

    private val _ui = MutableStateFlow(RamakotiUiState())
    val ui: StateFlow<RamakotiUiState> = _ui

    private var metaListener: ListenerRegistration? = null

    // serialize writes
    private var pendingTaps = 0
    private var writeInProgress = false

    // guards
    private var lastIssuedForTarget = 0                 // from prefs.lastCertForTarget
    private var lastPromptedPersisted = 0               // from prefs.lastPromptedForTarget

    init {
        // Observe target changes → re-check auto-issue
        viewModelScope.launch {
            prefs.targetCount.collectLatest { t ->
                _ui.value = _ui.value.copy(targetCount = t)
                maybeAutoIssue(_ui.value.lifetimeCount, t)
            }
        }
        // Observe last issued guard
        viewModelScope.launch {
            prefs.lastCertForTarget.collectLatest { last ->
                lastIssuedForTarget = last
                maybeAutoIssue(_ui.value.lifetimeCount, _ui.value.targetCount)
            }
        }
        // NEW: observe persisted “prompted once” guard
        viewModelScope.launch {
            prefs.lastPromptedForTarget.collectLatest { prompted ->
                lastPromptedPersisted = prompted
                // no immediate UI change; used inside maybeAutoIssue
            }
        }

        // Initial attach
        refreshFromServer()
    }

    /** Call from the screen’s LaunchedEffect(Unit) to ensure live state after back/navigation. */
    fun refreshFromServer() {
        viewModelScope.launch {
            try {
                // Ensure baseline docs exist
                sync.ensureMetaInitialized()

                // (Re)start live listener for lifetime/meta
                startMetaListener()

                // One-shot journey load (language, etc.)
                val j = repo.getJourney()
                _ui.value = _ui.value.copy(
                    totalCount = j.totalCount,
                    language = j.language
                )
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(error = t.message ?: t.toString())
            }
        }
    }

    private fun startMetaListener() {
        val uid = auth.currentUser?.uid ?: return
        val metaRef = db.collection("users").document(uid)
            .collection("ramakoti_meta").document("meta")

        metaListener?.remove()
        metaListener = metaRef.addSnapshotListener { snap, err ->
            if (err != null) {
                _ui.value = _ui.value.copy(error = err.message)
                return@addSnapshotListener
            }
            val life = snap?.getLong("lifetimeCount")?.toInt() ?: 0
            val inBatch = life % BATCH_SIZE
            val batchNo = floor(life.toDouble() / BATCH_SIZE).toInt() + 1
            val croreNo = floor(life.toDouble() / CRORE_SIZE).toInt() + 1
            val reached = life >= _ui.value.targetCount

            _ui.value = _ui.value.copy(
                lifetimeCount = life,
                totalCount = life.toLong(),
                currentBatchCount = inBatch,
                currentBatchNumber = batchNo,
                currentCrore = croreNo,
                targetReached = reached,
                error = null
            )
            // Optionally persist batch state to journey doc
            viewModelScope.launch { writeBatchStateToFirestore() }
            maybeAutoIssue(life, _ui.value.targetCount)
        }
    }

    /** Single source of truth for any increment (button, volume, etc.) */
    fun tickNext() {
        // hard guard: never go past the selected target
        if (_ui.value.targetReached) return
        pendingTaps++
        if (!writeInProgress) flushPending()
    }

    private fun flushPending() {
        viewModelScope.launch {
            writeInProgress = true
            _ui.value = _ui.value.copy(isIncrementBusy = true)
            try {
                while (pendingTaps > 0) {
                    if (_ui.value.targetReached) {
                        pendingTaps = 0
                        break
                    }
                    pendingTaps--

                    // Atomic server-side increment
                    sync.addCount(1)

                    // Optimistic local UI update (listener will confirm shortly)
                    val newLife = _ui.value.lifetimeCount + 1
                    val inBatch = newLife % BATCH_SIZE
                    val rolled = inBatch == 0
                    val batchNo = floor(newLife.toDouble() / BATCH_SIZE).toInt() + 1
                    val croreNo = floor(newLife.toDouble() / CRORE_SIZE).toInt() + 1
                    val reached = newLife >= _ui.value.targetCount

                    _ui.value = _ui.value.copy(
                        lifetimeCount = newLife,
                        totalCount = newLife.toLong(),
                        currentBatchCount = if (rolled) 0 else inBatch,
                        currentBatchNumber = batchNo,
                        currentCrore = croreNo,
                        targetReached = reached,
                        showCelebration = rolled
                    )

                    if (reached) maybeAutoIssue(newLife, _ui.value.targetCount)
                }
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(error = t.message ?: t.toString())
            } finally {
                writeInProgress = false
                _ui.value = _ui.value.copy(isIncrementBusy = false)
            }
        }
    }

    /** Called by the UI after showing the celebration for 108 completion. */
    fun clearCelebration() {
        _ui.value = _ui.value.copy(showCelebration = false)
    }

    // -------- Certificate flow --------

    private fun maybeAutoIssue(life: Int, target: Int, force: Boolean = false) {
        if (life < target) return
        if (target <= 0) return

        // If a certificate already exists for this target, prompt only once per target (persisted)
        if (!force && lastIssuedForTarget == target) {
            if (lastPromptedPersisted != target) {
                _ui.value = _ui.value.copy(showNextTargetPrompt = true)
            }
            return
        }

        _ui.value = _ui.value.copy(
            isIssuingCertificate = true,
            certificateError = null,
            certificateUrl = null
        )

        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: error("Not signed in")
                val milestone = when (target) {
                    100_000   -> "Completed 1 Lakh Sri Rama Namas"
                    1_000_000 -> "Completed 10 Lakh Sri Rama Namas"
                    else      -> "Completed 1 Crore Sri Rama Namas"
                }

                val result = certRepo.generateAndOptionallyUpload(
                    context = appContext,
                    uid = user.uid,
                    input = CertificateInput(
                        devoteeName = user.displayName ?: "Devotee",
                        countText = milestone,
                        dateText = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                        language = _ui.value.language,
                        verificationUrl = "",
                        templateBitmap = null
                    ),
                    uploadToStorage = false
                )

                val url = RamakotiExportUploader.uploadAndRecord(
                    auth = auth,
                    storage = storage,
                    db = db,
                    localFileUri = result.localPdf.toUri(),
                    fileName = result.localPdf.name,
                    type = ExportType.CERTIFICATE,
                    extraMeta = mapOf("certificateId" to result.certificateId, "targetCount" to target)
                )

                // mark guards
                prefs.markCertIssuedFor(target)
                lastIssuedForTarget = target

                _ui.value = _ui.value.copy(
                    isIssuingCertificate = false,
                    certificateUrl = url,
                    certificateError = null,
                    lastExportUrl = url,
                    lastCertificateId = result.certificateId,
                    lastLocalFile = result.localPdf,
                    showNextTargetPrompt = true
                )
                // Also persist that we've shown/handled the prompt for this target
                // (the actual toggle to false happens when user taps a dialog button)
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(
                    isIssuingCertificate = false,
                    certificateError = t.message ?: t.toString()
                )
            }
        }
    }

    fun retryCertificate() {
        val s = _ui.value
        maybeAutoIssue(s.lifetimeCount, s.targetCount, force = true)
    }

    fun openCertificate(context: Context) {
        val url = _ui.value.certificateUrl ?: return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun dismissCertError() {
        _ui.value = _ui.value.copy(certificateError = null)
    }

    /** Screen passes nav lambda here for "Choose next target"; Not now passes default. */
    fun onNextTargetDecision(accept: Boolean, onNavigateToPicker: () -> Unit = {}) {
        viewModelScope.launch {
            // Persist that we already prompted for THIS target → won't reopen after process death
            prefs.setLastPromptedForTarget(_ui.value.targetCount)
            _ui.value = _ui.value.copy(showNextTargetPrompt = false)
            if (accept) onNavigateToPicker()
        }
    }

    // -------- Optional: persist batch state also to journey doc --------
    private suspend fun writeBatchStateToFirestore() {
        val uid = auth.currentUser?.uid ?: return
        val s = _ui.value
        val userDoc = db.collection("users").document(uid)
        val stateDoc = userDoc.collection("ramakoti").document("state")
        val data = mapOf(
            "totalCount" to s.lifetimeCount,
            "currentBatchCount" to s.currentBatchCount,
            "currentBatchNumber" to s.currentBatchNumber,
            "language" to s.language
        )
        stateDoc.set(data, SetOptions.merge()).await()
    }

    fun dismissTargetDialog() {
        _ui.value = _ui.value.copy(showTargetCompleteDialog = false)
    }

    override fun onCleared() {
        super.onCleared()
        metaListener?.remove()
        metaListener = null
    }
}
