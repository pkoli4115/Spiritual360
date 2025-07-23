package com.hindu.pooja.app.util

import android.content.Context
import android.os.Build
import java.util.*

object LanguageManager {
    fun SetAppLocale(language: String, context: Context) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = context.resources
        val config = resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            config.locale = locale
        }
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
