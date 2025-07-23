// File: app/src/main/java/com/hindu/pooja/data/User.kt
package com.hindu.pooja.data

data class User(
    val name: String = "",
    val fatherName: String = "",
    val motherName: String = "",
    val gothram: String = "",
    val nakshatram: String = "",
    val children: List<String> = emptyList()
)
