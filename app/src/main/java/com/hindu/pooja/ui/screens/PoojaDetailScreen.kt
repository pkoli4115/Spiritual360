package com.hindu.pooja.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.hindu.pooja.app.data.PoojaLoader
import com.hindu.pooja.ui.screens.*
import com.hindu.pooja.app.ui.components.SectionCard
import com.hindu.pooja.data.Pooja

@Composable
fun PoojaDetailScreen(fileName: String, imageRes: String) {
    val context = LocalContext.current
    var pooja by remember { mutableStateOf<Pooja?>(null) }

    LaunchedEffect(fileName) {
        pooja = PoojaLoader.loadPoojaFromAssets(context, fileName)
    }

    pooja?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Image(
                painter = painterResource(id = context.resources.getIdentifier(imageRes, "drawable", context.packageName)),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            it.content.forEach { (title, content) ->
                SectionCard(title, content)
            }
        }
    }
}
