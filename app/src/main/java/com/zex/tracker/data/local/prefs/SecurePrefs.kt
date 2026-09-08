package com.zex.tracker.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.core.logging.ZexLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePrefs @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            ZexConstants.PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        ZexLogger.e("SecurePrefs", "Failed to init EncryptedSharedPreferences. Falling back.", e)
        context.getSharedPreferences(ZexConstants.FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun putString(key: String, value: String?) = prefs.edit().putString(key, value).apply()
    fun getString(key: String, default: String? = null): String? = prefs.getString(key, default)

    fun putBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    fun getBoolean(key: String, default: Boolean = false): Boolean = prefs.getBoolean(key, default)

    fun putInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    fun getInt(key: String, default: Int = 0): Int = prefs.getInt(key, default)

    fun putLong(key: String, value: Long) = prefs.edit().putLong(key, value).apply()
    fun getLong(key: String, default: Long = 0L): Long = prefs.getLong(key, default)

    fun clear() = prefs.edit().clear().apply()
}
