package com.hindu.pooja.feature.ramakoti

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.hindu.pooja.feature.ramakoti.data.CertificateInput
import com.hindu.pooja.feature.ramakoti.data.CertificateRepository
import com.hindu.pooja.feature.ramakoti.data.RamakotiExportUploader
import com.hindu.pooja.feature.ramakoti.data.RamakotiExportUploader.ExportType
import com.hindu.pooja.feature.ramakoti.prefs.RamakotiPreferences
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
private const val TAG = "RamaVM"

/** Single screen state (RUN-centric). */
data class RamakotiUiState(
    val runId: String = "",
    val language: String = "en",
    val targetCount: Int = 10_000_000,
    // RUN progress (fresh per run)
    val runTotal: Int = 0,
    val currentBatchCount: Int = 0,
    val currentBatchNumber: Int = 1,

    // Lifetime (for info header)
    val lifetimeCount: Int = 0,
    val currentCrore: Int = 1,

    val targetReached: Boolean = false,
    val isIncrementBusy: Boolean = false,
    val showCelebration: Boolean = false,

    // Certificate states (tied to RUN)
    val isIssuingCertificate: Boolean = false,
    val certificateStep: Int = 0,
    val certificateStepLabel: String = "",
    val certificateUrl: String? = null,
    val certificateError: String? = null,

    // Secondary UI
    val canPickNextTarget: Boolean = false,
    val showNextTargetPrompt: Boolean = false,

    val error: String? = null
)

@HiltViewModel
class RamakotiViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val prefs: RamakotiPreferences
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val certRepo = CertificateRepository()

    private val _ui = MutableStateFlow(RamakotiUiState())
    val ui: StateFlow<RamakotiUiState> = _ui

    private var runListener: ListenerRegistration? = null
    private var metaListener: ListenerRegistration? = null

    // serialize increments
    private var pendingTaps = 0
    private var writeInProgress = false

    // Re-run attach once auth is ready (prevents early-exit when currentUser is null)
    private val authListener = FirebaseAuth.AuthStateListener { fa ->
        val user = fa.currentUser
        Log.d(TAG, "AuthStateListener: user=${user?.uid ?: "null"}")
        if (user != null) ensureRunAttached()
    }

    init {
        auth.addAuthStateListener(authListener)

        // Adopt latest target & language from prefs (unchanged base behavior)
        viewModelScope.launch {
            prefs.targetCount.collectLatest { t ->
                Log.d(TAG, "prefs.targetCount -> $t")
                _ui.value = _ui.value.copy(targetCount = t)
                ensureRunAttached()
            }
        }
        viewModelScope.launch {
            prefs.selectedLanguage.collectLatest { lang ->
                Log.d(TAG, "prefs.selectedLanguage -> $lang")
                _ui.value = _ui.value.copy(language = lang)
                ensureRunAttached()
            }
        }
        viewModelScope.launch {
            prefs.currentRunId.collectLatest { id ->
                Log.d(TAG, "prefs.currentRunId -> $id")
                if (id != _ui.value.runId) {
                    _ui.value = _ui.value.copy(runId = id)
                    attachRunListener(id)
                }
            }
        }

        attachMetaListener()
    }

    /** Called from screen’s LaunchedEffect(Unit). */
    fun refreshFromServer() {
        Log.d(TAG, "refreshFromServer()")
        ensureRunAttached()
        attachMetaListener()
    }

    /** Decide which run to attach to (and create one when none exists). */
    private fun ensureRunAttached() {
        viewModelScope.launch {
            val user = auth.currentUser ?: run {
                Log.w(TAG, "ensureRunAttached(): uid is null; will retry via AuthStateListener")
                return@launch
            }
            val uid = user.uid
            val prefRunId = prefs.currentRunId.firstOrNullSafe().orEmpty()
            val target = _ui.value.targetCount
            val lang = _ui.value.language

            // If we already have a runId, verify it still exists & ACTIVE
            if (prefRunId.isNotBlank()) {
                val snap = db.collection("users").document(uid)
                    .collection("ramakotiRuns").document(prefRunId).get().await()
                if (snap.exists()) {
                    val status = snap.getString("status") ?: "ACTIVE"
                    if (status == "ACTIVE") {
                        // If target/lang changed but runTotal == 0, update the run in place
                        val runTotal = (snap.getLong("runTotal") ?: 0L).toInt()
                        val stTarget = (snap.getLong("targetCount") ?: target.toLong()).toInt()
                        val stLang = snap.getString("language") ?: lang
                        if ((stTarget != target || stLang != lang) && runTotal == 0) {
                            Log.d(TAG, "Active run with 0 total → updating target/lang in place")
                            snap.reference.update(
                                mapOf("targetCount" to target, "language" to lang)
                            ).await()
                        }
                        attachRunListener(prefRunId)
                        return@launch
                    }
                }
                Log.d(TAG, "Stored runId=$prefRunId is not ACTIVE/missing → will adopt/create")
            }

            // Try to adopt any ACTIVE run
            val active = db.collection("users").document(uid)
                .collection("ramakotiRuns")
                .whereEqualTo("status", "ACTIVE")
                .limit(1)
                .get().await()

            if (!active.isEmpty) {
                val doc = active.documents.first()
                val runId = doc.id
                Log.d(TAG, "Adopting existing ACTIVE run: $runId")
                prefs.setCurrentRunId(runId)
                attachRunListener(runId)
                return@launch
            }

            // -------- First-time user signal (based on YOUR data) --------
            // IMPORTANT: even if first-time, we STILL create the run right away (failsafe)
            val runsRef = db.collection("users").document(uid).collection("ramakotiRuns")
            val anyRunSnap = runsRef.limit(1).get().await()
            val metaRef = db.collection("users").document(uid)
                .collection("ramakoti_meta").document("meta")
            val metaSnap = metaRef.get().await()
            val isFirstTimeUser = anyRunSnap.isEmpty && !metaSnap.exists()

            if (isFirstTimeUser) {
                Log.d(TAG, "First-time user detected → raising picker flags (AND creating run).")
                _ui.value = _ui.value.copy(
                    canPickNextTarget = true,
                    showNextTargetPrompt = true
                )
                // Do NOT early-return; proceed to create a run so writer is live.
            }
            // -------------------------------------------------------------

            // No ACTIVE run → create new one (original working behavior)
            val newId = "run-${System.currentTimeMillis()}"
            val ref = db.collection("users").document(uid)
                .collection("ramakotiRuns").document(newId)
            val data = mapOf(
                "runId" to newId,
                "status" to "ACTIVE",
                "language" to lang,
                "targetCount" to target,
                "runTotal" to 0,
                "createdAt" to FieldValue.serverTimestamp()
            )
            Log.d(TAG, "Creating new run: $newId, target=$target, lang=$lang")
            ref.set(data).await()
            prefs.setCurrentRunId(newId)
            attachRunListener(newId)
        }
    }

    private fun attachRunListener(runId: String) {
        runListener?.remove()
        if (runId.isBlank()) return
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection("users").document(uid)
            .collection("ramakotiRuns").document(runId)

        Log.d(TAG, "attachRunListener($runId)")
        runListener = ref.addSnapshotListener { snap, err ->
            if (err != null) {
                Log.e(TAG, "runListener error", err)
                _ui.value = _ui.value.copy(error = err.message)
                return@addSnapshotListener
            }
            if (snap == null || !snap.exists()) {
                Log.w(TAG, "runListener: doc missing for $runId")
                _ui.value = _ui.value.copy(
                    runTotal = 0,
                    currentBatchCount = 0,
                    currentBatchNumber = 1,
                    targetReached = false,
                    canPickNextTarget = true
                )
                return@addSnapshotListener
            }
            val runTotal = (snap.getLong("runTotal") ?: 0L).toInt()
            val target   = (snap.getLong("targetCount") ?: _ui.value.targetCount.toLong()).toInt()
            val language = snap.getString("language") ?: _ui.value.language
            val status   = snap.getString("status") ?: "ACTIVE"
            val inBatch  = runTotal % BATCH_SIZE
            val batchNo  = floor(runTotal.toDouble() / BATCH_SIZE).toInt() + 1

            // UI completed ONLY when server value == target
            val reachedExactly = runTotal == target
            val reachedOrBeyond = runTotal >= target

            val certUrl  = snap.getString("certificateUrl")

            Log.d(TAG, "runSnap: total=$runTotal target=$target status=$status reached=$reachedOrBeyond cert=${!certUrl.isNullOrBlank()}")

            _ui.value = _ui.value.copy(
                runId = runId,
                language = language,
                targetCount = target,
                runTotal = runTotal,
                currentBatchCount = inBatch,
                currentBatchNumber = batchNo,
                targetReached = reachedExactly,     // server-driven
                isIssuingCertificate = false,
                certificateUrl = certUrl,
                certificateError = null,
                canPickNextTarget = (status != "ACTIVE")
            )

            // Reached (or overshot) while still ACTIVE → finish exactly once
            if (reachedOrBeyond && status == "ACTIVE") {
                maybeCompleteRun(runId, runTotal, target, language)
            }
        }
    }

    private fun attachMetaListener() {
        metaListener?.remove()
        val uid = auth.currentUser?.uid ?: return
        val metaRef = db.collection("users").document(uid)
            .collection("ramakoti_meta").document("meta")

        Log.d(TAG, "attachMetaListener()")
        metaListener = metaRef.addSnapshotListener { snap, err ->
            if (err != null) {
                Log.e(TAG, "metaListener error", err)
                return@addSnapshotListener
            }
            val life = snap?.getLong("lifetimeCount")?.toInt() ?: 0
            val croreNo = floor(life.toDouble() / CRORE_SIZE).toInt() + 1
            _ui.value = _ui.value.copy(lifetimeCount = life, currentCrore = croreNo)
        }
    }

    /** Increment: updates both RUN and LIFETIME atomically. */
    fun tickNext() {
        val s = _ui.value
        if (s.runId.isBlank()) {
            Log.w(TAG, "tickNext(): no runId; ignoring")
            return
        }
        if (s.targetReached) {
            Log.d(TAG, "tickNext(): target already reached; ignoring")
            return
        }
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

                    val uid = auth.currentUser?.uid ?: break
                    val runId = _ui.value.runId
                    if (runId.isBlank()) break

                    val runRef = db.collection("users").document(uid)
                        .collection("ramakotiRuns").document(runId)
                    val metaRef = db.collection("users").document(uid)
                        .collection("ramakoti_meta").document("meta")

                    Log.d(TAG, "write: increment start (run=$runId)")
                    // Batch write (await so UI reflects server success)
                    db.runBatch { b ->
                        b.update(runRef, mapOf("runTotal" to FieldValue.increment(1)))
                        b.set(
                            metaRef,
                            mapOf(
                                "lifetimeCount" to FieldValue.increment(1),
                                "updatedAt" to FieldValue.serverTimestamp()
                            ),
                            SetOptions.merge()
                        )
                    }.await()
                    Log.d(TAG, "write: increment OK (run=$runId)")

                    // Optimistic UI *without* flipping completion; snapshot will do that.
                    val newRunTotal = _ui.value.runTotal + 1
                    val inBatch = newRunTotal % BATCH_SIZE
                    val rolled = inBatch == 0
                    val batchNo = floor(newRunTotal.toDouble() / BATCH_SIZE).toInt() + 1

                    _ui.value = _ui.value.copy(
                        runTotal = newRunTotal,
                        currentBatchCount = if (rolled) 0 else inBatch,
                        currentBatchNumber = batchNo,
                        showCelebration = rolled
                    )
                }
            } catch (t: Throwable) {
                Log.e(TAG, "flushPending() failure", t)
                _ui.value = _ui.value.copy(error = t.message ?: t.toString())
            } finally {
                writeInProgress = false
                _ui.value = _ui.value.copy(isIncrementBusy = false)
            }
        }
    }

    fun clearCelebration() {
        _ui.value = _ui.value.copy(showCelebration = false)
    }

    /** Complete this run exactly once: mark COMPLETED + generate certificate + history. */
    private fun maybeCompleteRun(runId: String, runTotal: Int, target: Int, language: String) {
        if (_ui.value.isIssuingCertificate) return
        Log.d(TAG, "maybeCompleteRun(runId=$runId total=$runTotal target=$target)")

        _ui.value = _ui.value.copy(
            isIssuingCertificate = true,
            certificateError = null,
            certificateStep = 1,
            certificateStepLabel = "Preparing certificate..."
        )
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: error("Not signed in")
                val runRef = db.collection("users").document(user.uid)
                    .collection("ramakotiRuns").document(runId)

                // If certificate already present (race w/ other client), skip generation
                val existing = runRef.get().await()
                existing.getString("certificateUrl")?.let { already ->
                    if (already.isNotBlank()) {
                        Log.d(TAG, "Run already has certificate; skipping generation")
                        _ui.value = _ui.value.copy(
                            isIssuingCertificate = false,
                            certificateUrl = already,
                            canPickNextTarget = true
                        )
                        return@launch
                    }
                }

                val milestone = when (target) {
                    10 -> "Completed 10 Sri Rama Namas"
                    100 -> "Completed 100 Sri Rama Namas"
                    1000 -> "Completed 1000 Sri Rama Namas"
                    100_000 -> "Completed 1 Lakh Sri Rama Namas"
                    1_000_000 -> "Completed 10 Lakh Sri Rama Namas"
                    10_000_000 -> "Completed 1 Crore Sri Rama Namas"
                    else -> "Completed $target Sri Rama Namas"
                }

                // Generate locally
                val result = certRepo.generateAndOptionallyUpload(
                    context = appContext,
                    uid = user.uid,
                    input = CertificateInput(
                        devoteeName = user.displayName ?: "Devotee",
                        countText = milestone,
                        dateText = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                        language = language,
                        verificationUrl = "",
                        templateBitmap = null
                    ),
                    uploadToStorage = false
                )

                // Upload + record
                _ui.value = _ui.value.copy(
                    certificateStep = 2,
                    certificateStepLabel = "Uploading securely..."
                )
                val url = RamakotiExportUploader.uploadAndRecord(
                    auth = auth,
                    storage = storage,
                    db = db,
                    localFileUri = result.localPdf.toUri(),
                    fileName = result.localPdf.name,
                    type = ExportType.CERTIFICATE,
                    extraMeta = mapOf(
                        "certificateId" to result.certificateId,
                        "targetCount" to target,
                        "runId" to runId,
                        "language" to language
                    )
                )
                Log.d(TAG, "Certificate uploaded: $url (id=${result.certificateId})")

                // Atomically mark the run COMPLETED (if still ACTIVE)
                _ui.value = _ui.value.copy(
                    certificateStep = 3,
                    certificateStepLabel = "Finalizing completion..."
                )
                db.runTransaction { tx ->
                    val snap = tx.get(runRef)
                    if ((snap.getString("status") ?: "ACTIVE") == "ACTIVE") {
                        tx.update(
                            runRef, mapOf(
                                "status" to "COMPLETED",
                                "certificateUrl" to url,
                                "certificateId" to result.certificateId,
                                "completedAt" to Timestamp.now()
                            )
                        )
                    }
                    null
                }.await()

                // (Optional) add a history doc used by Profile screen
                db.collection("users").document(user.uid)
                    .collection("ramakotiHistory")
                    .add(
                        mapOf(
                            "totalAtCompletion" to runTotal,
                            "targetCount" to target,
                            "language" to language,
                            "certificateId" to result.certificateId,
                            "certificateUrl" to url,
                            "completedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()

                _ui.value = _ui.value.copy(
                    isIssuingCertificate = false,
                    certificateUrl = url,
                    canPickNextTarget = true,
                    certificateStep = 0,
                    certificateStepLabel = ""
                )
                Log.d(TAG, "Run COMPLETED + history recorded")
            } catch (t: Throwable) {
                Log.e(TAG, "maybeCompleteRun() failed", t)
                _ui.value = _ui.value.copy(
                    isIssuingCertificate = false,
                    certificateError = t.message ?: t.toString(),
                    certificateStep = 0,
                    certificateStepLabel = ""
                )
            }
        }
    }

    /** Mark any existing ACTIVE run as ABANDONED and start a fresh one. Use this for “Set New Target”. */
    suspend fun abandonActiveAndStartNew(target: Int, language: String) {
        val uid = auth.currentUser?.uid ?: return
        val runsRef = db.collection("users").document(uid).collection("ramakotiRuns")

        // Close all ACTIVE runs (should be at most 1, but keep this safe)
        val actives = runsRef.whereEqualTo("status", "ACTIVE").get().await()
        for (doc in actives.documents) {
            Log.d(TAG, "Abandoning active run ${doc.id}")
            doc.reference.update("status", "ABANDONED").await()
        }

        val newId = "run-${System.currentTimeMillis()}"
        Log.d(TAG, "Starting new run $newId target=$target lang=$language")
        runsRef.document(newId).set(
            mapOf(
                "runId" to newId,
                "status" to "ACTIVE",
                "language" to language,
                "targetCount" to target,
                "runTotal" to 0,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
        prefs.setCurrentRunId(newId)
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

    fun onNextTargetDecision(accept: Boolean, onNavigateToPicker: () -> Unit) {
        _ui.value = _ui.value.copy(showNextTargetPrompt = false)
        if (accept) onNavigateToPicker()
    }

    override fun onCleared() {
        super.onCleared()
        runListener?.remove()
        metaListener?.remove()
        auth.removeAuthStateListener(authListener)
    }
}

/* ---------- tiny Flow helper ---------- */
suspend fun <T> Flow<T>.firstOrNullSafe(): T? =
    try { this.firstOrNull() } catch (_: Throwable) { null }
