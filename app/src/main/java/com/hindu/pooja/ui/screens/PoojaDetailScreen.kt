package com.hindu.pooja.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hindu.pooja.model.PoojaIndexItem
import java.io.InputStreamReader

@Composable
fun PoojaDetailScreen(
    poojaId: String,
    navEntry: NavBackStackEntry
) {
    val context = LocalContext.current
    val pooja by remember(poojaId) {
        mutableStateOf(loadPoojaById(context, poojaId))
    }

    if (pooja == null) {
        Text(
            text = "Pooja not found.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(32.dp)
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = pooja!!.name, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        val imageResId = remember(pooja!!.image) {
            context.resources.getIdentifier(
                pooja!!.image.substringBeforeLast("."), // remove .png if present
                "drawable",
                context.packageName
            )
        }

        Image(
            painter = painterResource(id = imageResId),
            contentDescription = pooja!!.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )


        Spacer(modifier = Modifier.height(16.dp))

        // Placeholder: You can extend this to show steps, kathas, mantras, etc.
        Text(
            text = "Detailed pooja content coming soon...",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

fun loadPoojaById(context: Context, poojaId: String): PoojaIndexItem? {
    val files = listOf(
        "daily_weekly_te.json",
        "vrathams_te.json",
        "festival_te.json",
        "sahasranamas_te.json",
        "kids_te.json"
    )
    for (file in files) {
        try {
            val inputStream = context.assets.open("poojas/$file")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<PoojaIndexItem>>() {}.type
            val poojas: List<PoojaIndexItem> = Gson().fromJson(reader, type)
            val matched = poojas.firstOrNull { it.id == poojaId }
            if (matched != null) return matched
        } catch (_: Exception) { }
    }
    return null
}
