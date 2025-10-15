@file:OptIn(ExperimentalMaterial3Api::class)

package com.hindu.pooja.feature.ramakoti.ui

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.hindu.pooja.R
import com.hindu.pooja.feature.ramakoti.data.CertificateInput
import com.hindu.pooja.feature.ramakoti.data.CertificateRepository
import com.hindu.pooja.feature.ramakoti.data.LanguagePreferenceManager
import com.hindu.pooja.feature.ramakoti.data.RamakotiExportUploader
import com.hindu.pooja.feature.ramakoti.data.RamakotiExportUploader.ExportType
import com.hindu.pooja.feature.ramakoti.util.ShareHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CertificateScreen(
    modifier: Modifier = Modifier,
    devoteeName: String? = null,
    milestoneCountText: String,          // e.g., "Completed 1 Crore Sri Rama Namas"
    useTemplateDrawablePreview: Boolean = true,
    onCompleted: (localFile: File, remoteUrl: String?, certificateId: String) -> Unit = { _, _, _ -> },
    onDone: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    val auth = remember { FirebaseAuth.getInstance() }
    val storage = remember { FirebaseStorage.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val repo = remember { CertificateRepository() }
    // ✅ use singleton getter (constructor is private)
    val langMgr = remember { LanguagePreferenceManager.getInstance(context) }

    var generating by remember { mutableStateOf(false) }
    var lastLocalFile by remember { mutableStateOf<File?>(null) }
    var lastRemoteUrl by remember { mutableStateOf<String?>(null) }
    var lastCertificateId by remember { mutableStateOf<String?>(null) }
    var lastError by remember { mutableStateOf<String?>(null) }

    val today = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }
    val userName = remember(devoteeName) { devoteeName ?: auth.currentUser?.displayName ?: "Devotee" }

    // 🔁 Read per-user language; default to "en" if blank / null
    val lang by produceState(initialValue = "en", context, auth.currentUser) {
        val uid = auth.currentUser?.uid
        value = langMgr.languageFlowFor(uid).first().ifBlank { "en" }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Certificate Preview") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (useTemplateDrawablePreview) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.414f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ramakoti_certificate_bg),
                        contentDescription = null
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            Text(
                text = when (lang.lowercase()) {
                    "te" -> "ఈ ధృవపత్రం $userName గారికి."
                    "hi" -> "यह प्रमाणपत्र $userName के लिए है."
                    else -> "This certificate is for $userName."
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = when (lang.lowercase()) {
                    "te" -> "మైలురాయి: $milestoneCountText"
                    "hi" -> "माइलस्टोन: $milestoneCountText"
                    else -> "Milestone: $milestoneCountText"
                },
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    enabled = !generating,
                    onClick = {
                        scope.launch {
                            generating = true
                            lastError = null
                            try {
                                val uid = auth.currentUser?.uid ?: error("Not signed in")

                                val templateBmp = runCatching {
                                    BitmapFactory.decodeResource(context.resources, R.drawable.ramakoti_certificate_bg)
                                }.getOrNull()

                                val input = CertificateInput(
                                    devoteeName = userName,
                                    countText = milestoneCountText,
                                    dateText = today,
                                    language = lang,
                                    verificationUrl = "",   // OFFLINE QR
                                    templateBitmap = templateBmp
                                )

                                val result = repo.generateAndOptionallyUpload(
                                    context = context,
                                    uid = uid,
                                    input = input,
                                    uploadToStorage = false
                                )
                                val local = result.localPdf

                                val remoteUrl = RamakotiExportUploader.uploadAndRecord(
                                    auth = auth,
                                    storage = storage,
                                    db = db,
                                    localFileUri = local.toURI().toString().toUri(),
                                    fileName = local.name,
                                    type = ExportType.CERTIFICATE,
                                    extraMeta = mapOf("certificateId" to result.certificateId)
                                )

                                lastLocalFile = local
                                lastRemoteUrl = remoteUrl
                                lastCertificateId = result.certificateId
                                onCompleted(local, remoteUrl, result.certificateId)
                                toast(context, "Certificate saved & uploaded")
                            } catch (t: Throwable) {
                                lastError = t.message ?: t.toString()
                                toast(context, "Failed: $lastError")
                            } finally {
                                generating = false
                            }
                        }
                    }
                ) { Text(if (generating) "Generating…" else "Generate & Upload") }

                OutlinedButton(
                    enabled = lastLocalFile != null,
                    onClick = { lastLocalFile?.let { ShareHelper.openPdf(context, it) } }
                ) { Text("Open") }

                OutlinedButton(
                    enabled = lastLocalFile != null,
                    onClick = { lastLocalFile?.let { ShareHelper.sharePdf(context, it) } }
                ) { Text("Share") }
            }

            Spacer(Modifier.height(16.dp))

            lastCertificateId?.let {
                Text(
                    text = "Certificate ID: $it",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            lastRemoteUrl?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Cloud URL:\n$it",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            lastError?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Error: $it",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            onDone?.let {
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = it) { Text("Done") }
            }
        }
    }
}

private fun toast(ctx: Context, msg: String) {
    android.widget.Toast.makeText(ctx, msg, android.widget.Toast.LENGTH_SHORT).show()
}
