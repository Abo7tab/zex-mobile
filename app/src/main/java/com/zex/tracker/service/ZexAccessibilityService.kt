package com.zex.tracker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.ui.screens.scream.ScreamActivity

class ZexAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        this.serviceInfo = info
        ZexLogger.i("ZexAccessibilityService", "Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: ""
            val className = event.className?.toString() ?: ""

            if (packageName == "android" || packageName == "com.android.systemui") {
                if (ServiceController.isStolen || ServiceController.isScreaming) {
                    ZexLogger.w("ZexAccessibilityService", "Intercepted System Dialog while locked/stolen/screaming!")
                    val closeDialogs = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                    sendBroadcast(closeDialogs)
                    val intent = Intent(this, ScreamActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    override fun onInterrupt() {
        ZexLogger.w("ZexAccessibilityService", "Accessibility Service Interrupted")
    }
}
