package com.zex.tracker.core.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * ZexLogger
 * Centralized logging utility for ZEX App.
 * Wraps Timber and forwards warnings/errors to Crashlytics.
 */
object ZexLogger {

    /**
     * Log debug message
     */
    fun d(tag: String, message: String, vararg args: Any?) {
        Timber.tag(tag).d(message, *args)
    }

    /**
     * Log info message
     */
    fun i(tag: String, message: String, vararg args: Any?) {
        Timber.tag(tag).i(message, *args)
    }

    /**
     * Log warning message and optionally record throwable to Crashlytics
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(tag).w(throwable, message)
        } else {
            Timber.tag(tag).w(message)
        }
    }

    /**
     * Log error message and record to Crashlytics
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(tag).e(throwable, message)
            FirebaseCrashlytics.getInstance().recordException(throwable)
        } else {
            Timber.tag(tag).e(message)
            FirebaseCrashlytics.getInstance().log("ERROR: $tag - $message")
        }
    }

    /**
     * Force log a critical crash/error to Crashlytics with a custom key.
     */
    fun crash(message: String, throwable: Throwable) {
        Timber.e(throwable, "CRITICAL CRASH: $message")
        FirebaseCrashlytics.getInstance().setCustomKey("critical", true)
        FirebaseCrashlytics.getInstance().log("CRITICAL: $message")
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    /**
     * Set user ID for Crashlytics tracking
     */
    fun setUserId(userId: String) {
        FirebaseCrashlytics.getInstance().setUserId(userId)
    }
}

/**
 * Custom Timber Tree for reporting non-debug logs to Crashlytics
 */
class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == android.util.Log.VERBOSE || priority == android.util.Log.DEBUG) {
            return
        }

        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log("$tag: $message")
        
        if (t != null) {
            if (priority == android.util.Log.ERROR || priority == android.util.Log.WARN) {
                crashlytics.recordException(t)
            }
        }
    }
}
