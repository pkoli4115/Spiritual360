package com.hindu.pooja.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.hindu.pooja.R

sealed class BottomNavItem(
    val route: String,
    @StringRes val label: Int,
    @DrawableRes val icon: Int
) {
    object Home : BottomNavItem("home", R.string.nav_home, R.drawable.ic_home)
    object Featured : BottomNavItem("featured", R.string.nav_featured, R.drawable.ic_star)
    object Profile : BottomNavItem("profile", R.string.nav_profile, R.drawable.ic_profile)
}
