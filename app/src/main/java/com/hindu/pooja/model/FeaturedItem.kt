package com.hindu.pooja.model

import androidx.annotation.DrawableRes

data class FeaturedItem(
    val id: String,
    val title: String,
    val description: String,
    val route: String,
    @DrawableRes val imageRes: Int
)
