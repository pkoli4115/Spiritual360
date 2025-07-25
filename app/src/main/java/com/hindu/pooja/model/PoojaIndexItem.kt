package com.hindu.pooja.model

data class PoojaIndexItem(
    val id: String,
    val name: String,
    val file: String,
    val image: String = "default_pooja_image.png" // Image from drawable without extension
)
