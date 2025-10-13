package com.hindu.pooja.feature.ramakoti

import com.hindu.pooja.feature.ramakoti.data.RamakotiExportUploader.ExportType
import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.hindu.pooja.feature.ramakoti.data.*
import com.hindu.pooja.feature.ramakoti.sync.RamakotiSyncManager
import com.hindu.pooja.feature.ramakoti.util.RamakotiPdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/** UI model */
data class RamakotiUiState(
    val totalCount: Long = 0,
    val currentBatchCount: Int = 0,
    val currentCrore: Int = 1,
    val language: String = "en",
    val lifetimeCount: Int = 0,
    val lifetimeBatches: Int = 0,
    val showCelebration: Boolean = false,
    val canStartSecondCrore: Boolean = false,
    val lastExportUrl: String? = null,
    val lastCertificateId: String? = null,
    val lastLocalFile: File? = null,
    val error: String? = null
)

class RamakotiViewModel(app: Application) : AndroidViewModel(app) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val repo = RamakotiRepository(auth, db)
    private val phase2 = RamakotiPhase2Repository(auth, db)
    private val certRepo = CertificateRepository()
    private val sync = RamakotiSyncManager(auth, db)

    private var metaListener: ListenerRegistration? = null
    private val _ui = MutableStateFlow(RamakotiUiState())
    val ui: StateFlow<RamakotiUiState> = _ui

    /** rebind when user changes */
    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val newUid = firebaseAuth.currentUser?.uid
        viewModelScope.launch {
            _ui.value = RamakotiUiState(language = _ui.value.language)
            metaListener?.remove()
            if (newUid != null) {
                try {
                    sync.ensureMetaInitialized()
                    startMetaListener()
                    val j = repo.getJourney()
                    _ui.value = _ui.value.copy(
                        totalCount = j.totalCount,
                        currentBatchCount = j.currentBatchCount,
                        currentCrore = j.currentCrore,
                        language = j.language,
                        error = null
                    )
                } catch (t: Throwable) {
                    _ui.value = _ui.value.copy(error = t.message ?: t.toString())
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            try {
                val j = repo.getJourney()
                _ui.value = _ui.value.copy(
                    totalCount = j.totalCount,
                    currentBatchCount = j.currentBatchCount,
                    currentCrore = j.currentCrore,
                    language = j.language
                )
                sync.ensureMetaInitialized()
                startMetaListener()
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(error = t.message ?: t.toString())
            }
        }
        FirebaseAuth.getInstance().addAuthStateListener(authListener)
    }

    /** listens to lifetime meta counts */
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
            val batches = snap?.getLong("batches")?.toInt() ?: 0
            _ui.value = _ui.value.copy(
                lifetimeCount = life,
                lifetimeBatches = batches
            )
        }
    }

    /** tick next Jai Sri Ram */
    fun tickNext() {
        viewModelScope.launch {
            try {
                val r = sync.addCount(1)
                val newBatch = r.todayCountAfter % 108
                val newTotal = _ui.value.totalCount + 1

                var showCelebration = r.justCompleted108
                var canStartSecond = _ui.value.canStartSecondCrore

                if (r.justCompleted108) repo.resetBatchCounter()

                val (isCrore, _) = phase2.isCroreMilestone(newTotal)
                if (isCrore) canStartSecond = true

                _ui.value = _ui.value.copy(
                    totalCount = newTotal,
                    currentBatchCount = if (showCelebration) 0 else newBatch,
                    showCelebration = showCelebration,
                    canStartSecondCrore = canStartSecond,
                    error = null
                )
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(error = t.message ?: t.toString())
            }
        }
    }

    fun onCelebrationShown() {
        _ui.value = _ui.value.copy(showCelebration = false)
    }

    fun startSecondCrore() {
        viewModelScope.launch {
            try {
                val j = repo.startSecondCrore()
                _ui.value = _ui.value.copy(
                    totalCount = j.totalCount,
                    currentBatchCount = j.currentBatchCount,
                    currentCrore = j.currentCrore,
                    canStartSecondCrore = false,
                    error = null
                )
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(error = t.message ?: t.toString())
            }
        }
    }

    /** export full grid honoring lifetime */
    fun exportGrid(onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val ctx = getApplication<Application>().applicationContext
                val gridFile = RamakotiPdfGenerator.generateGridPdf(
                    context = ctx,
                    input = RamakotiPdfGenerator.GridInput(
                        languageCode = ui.value.language,
                        pageTitle = "Sri Rama Namam — 1 to 108",
                        lifetimeCount = ui.value.lifetimeCount,
                        currentBatchCount = ui.value.currentBatchCount
                    )
                )

                val url = RamakotiExportUploader.uploadAndRecord(
                    auth = auth,
                    storage = storage,
                    db = db,
                    localFileUri = gridFile.toUri(),
                    fileName = gridFile.name,
                    type = ExportType.PDF_GRID
                )

                _ui.value = _ui.value.copy(
                    lastExportUrl = url,
                    lastLocalFile = gridFile,
                    error = null
                )
                onDone?.invoke()
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(error = t.message ?: t.toString())
            }
        }
    }

    /** generate certificate with offline QR */
    fun generateCertificate(milestoneText: String) {
        viewModelScope.launch {
            try {
                val ctx = getApplication<Application>().applicationContext
                val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                val displayName = auth.currentUser?.displayName ?: "Devotee"

                val result = certRepo.generateAndOptionallyUpload(
                    context = ctx,
                    uid = auth.currentUser?.uid ?: error("Not signed in"),
                    input = CertificateInput(
                        devoteeName = displayName,
                        countText = milestoneText,
                        dateText = today,
                        language = ui.value.language,
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
                    extraMeta = mapOf("certificateId" to result.certificateId)
                )

                val (isCrore, croreNum) = phase2.isCroreMilestone(ui.value.totalCount)
                if (isCrore) {
                    phase2.onCroreCompleted(
                        croreNumber = croreNum,
                        totalAtCompletion = ui.value.totalCount,
                        certificateId = result.certificateId,
                        certificateUrl = url
                    )
                }

                _ui.value = _ui.value.copy(
                    lastExportUrl = url,
                    lastCertificateId = result.certificateId,
                    lastLocalFile = result.localPdf,
                    error = null
                )
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(error = t.message ?: t.toString())
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        metaListener?.remove()
        FirebaseAuth.getInstance().removeAuthStateListener(authListener)
    }
}
