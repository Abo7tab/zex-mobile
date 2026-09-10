package com.zex.tracker.core.constants

object ZexConstants {
    const val PREFS_NAME = "zex_secure_prefs"
    const val FALLBACK_PREFS_NAME = "zex_fallback_prefs"
    
    // Pref Keys
    const val KEY_OWNER_TOKEN = "owner_token"
    const val KEY_DEVICE_TOKEN = "device_token"
    const val KEY_DEVICE_UID = "device_uid"
    const val KEY_OWNER_ID = "owner_id"
    const val KEY_OWNER_NAME = "owner_name"
    const val KEY_OWNER_EMAIL = "owner_email"
    const val KEY_OWNER_PHONE = "owner_phone"
    const val KEY_OWNER_PASSWORD = "owner_password"
    const val KEY_PIN_CODE = "pin_code"
    const val KEY_IS_SETUP_COMPLETE = "is_setup_complete"
    const val KEY_LANGUAGE = "language"
    
    // Actions
    const val ACTION_STOP_SCREAM = "com.zex.tracker.ACTION_STOP_SCREAM_AND_FINISH"
    
    // Headers
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_DEVICE_TOKEN = "X-Device-Token"
}
