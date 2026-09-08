package com.zex.tracker.core.utils

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import com.zex.tracker.core.logging.ZexLogger
import java.util.Locale

object LanguageSwitcher {
    fun setLanguage(context: Context, languageTag: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val localeManager = context.getSystemService(LocaleManager::class.java)
                localeManager?.applicationLocales = LocaleList(Locale.forLanguageTag(languageTag))
            } else {
                // Fallback for older versions if needed
                val locale = Locale(languageTag)
                Locale.setDefault(locale)
                val resources = context.resources
                val configuration = resources.configuration
                configuration.setLocale(locale)
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }
            ZexLogger.i("LanguageSwitcher", "Language switched to $languageTag")
        } catch (e: Exception) {
            ZexLogger.e("LanguageSwitcher", "Failed to switch language", e)
        }
    }
}
