// file: com/hindu/pooja/ui/common/AppVersionBadge.kt
package com.hindu.pooja.ui.common

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hindu.pooja.BuildConfig

@Composable
fun AppVersionBadge(modifier: Modifier = Modifier) {
    val vName = BuildConfig.VERSION_NAME
    val vCode = BuildConfig.VERSION_CODE
    val bType = BuildConfig.BUILD_TYPE
    val bTime = BuildConfig.BUILD_TIME
    val sha   = BuildConfig.GIT_SHA

    AssistChip(
        modifier = modifier,
        onClick = {},
        label = { Text("v$vName ($vCode) • $bType • $bTime • $sha") }
    )
}
