// file: app/src/main/java/com/hindu/pooja/util/ImageLoader.kt
package com.hindu.pooja.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import com.hindu.pooja.R

@Composable
fun rememberSafePainter(source: Any?): Painter {
    val ctx = LocalContext.current

    return when (source) {
        null -> painterResource(R.drawable.ic_profile_placeholder)

        is Int -> painterResource(source)

        is String -> {
            // Normalize string: remove folder, extension, special chars
            val name = source.substringAfterLast('/')
                .substringBeforeLast('.')
                .lowercase()
                .replace(Regex("[^a-z0-9_]+"), "_")
                .trim('_')

            val id = ctx.resources.getIdentifier(name, "drawable", ctx.packageName)

            if (id != 0) {
                painterResource(id)
            } else if (isUrlString(source)) {
                rememberAsyncImagePainter(model = source)
            } else {
                painterResource(R.drawable.ic_profile_placeholder)
            }
        }

        else -> painterResource(R.drawable.ic_profile_placeholder)
    }
}

private fun isUrlString(s: String): Boolean {
    val t = s.trim().lowercase()
    return t.startsWith("http://") || t.startsWith("https://")
}
