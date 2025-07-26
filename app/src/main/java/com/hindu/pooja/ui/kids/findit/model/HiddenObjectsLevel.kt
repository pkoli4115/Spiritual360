@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package com.hindu.pooja.ui.kids.findit.model

import kotlinx.serialization.Serializable

@Serializable
data class HiddenObjectsLevel(
    val sceneImage: String, // 🔁 replaces imageRes
    val title: String,
    val timeLimitSeconds: Int,
    val objects: List<HiddenObject>
)

@Serializable
data class HiddenObject(
    val name: String,
    val x: Float,
    val y: Float
)

