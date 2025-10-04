package com.hindu.pooja.feature.ramakoti.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun RamakotiIntroScreen(
    navController: NavController,
    onNextRoute: String // e.g., "ramakoti/writer"
) {
    Scaffold(
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Box(Modifier.fillMaxWidth().padding(12.dp)) {
                    Button(
                        onClick = { navController.navigate(onNextRoute) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("NEXT")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("మా ఆశయం", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                """
“శ్రీరామ” అని రాయడం యుగయుగాలుగా కొనసాగుతున్న పవిత్ర సాధన.
ఈ డిజిటల్ శ్రీరామకోటిలో ఎక్కడ ఉన్నా, ఎప్పుడైనా సులభంగా
ఈ సేవలో పాల్గొనండి.

ఒక్కో 108 లేఖనాల తరువాత ఘంటానాదం, పుష్పవర్షం జరుగుతుంది.
మీ పురోగతి భద్రంగా నిల్వవుండి జీవితాంతం లెక్కలతో కొనసాగుతుంది.

ప్రత్యేకంగా వృద్ధులు, చూపు సమస్య ఉన్న భక్తుల కోసం:
• టైపింగ్ అవసరం లేదు — పెద్ద బటన్‌పై ఒక్క ట్యాప్‌తో “జై శ్రీరామ్”
• టాక్‌బ్యాక్‌ సహకారం, పెద్ద అక్షరాలు, హై-కాన్ట్రాస్ట్ రంగులు
• వైబ్రేషన్/ఘంటా శబ్దంతో ప్రతి జపానికి ప్రతిస్పందన
• ఐచ్చిక “ఆడియో మోడ్” — వాల్యూం అప్ = జపం, వాల్యూం డౌన్ = అన్‌డూ
• ఆఫ్‌లైన్‌లో పనిచేస్తుంది; తరువాత ఆటో సింక్

మా సంకల్పం: అందరికీ శ్రీరామనామాన్ని అలవాటుగా చేసి భక్తి భావాన్ని పెంచడం.
జై శ్రీరామ్ ✨
                """.trimIndent(),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
