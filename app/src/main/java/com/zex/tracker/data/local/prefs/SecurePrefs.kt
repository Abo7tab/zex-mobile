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
        createEncryptedPrefs(context)
    } catch (e: Exception) {
        ZexLogger.e("SecurePrefs", "Failed to init EncryptedSharedPreferences. Wiping and retrying.", e)
        try {
            // Delete corrupt preferences file to resolve potential MasterKey conflicts
            context.deleteSharedPreferences(ZexConstants.PREFS_NAME)
            // Retry
            createEncryptedPrefs(context)
        } catch (e2: Exception) {
            ZexLogger.e("SecurePrefs", "FATAL: Could not create EncryptedSharedPreferences after wipe.", e2)
            throw RuntimeException("Fatal Error: EncryptedSharedPreferences initialization failed", e2)
        }
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            ZexConstants.PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
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
