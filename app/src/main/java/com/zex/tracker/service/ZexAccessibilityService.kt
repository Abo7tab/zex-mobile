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

            // Settings Interceptor: Block Force Stop / Uninstall / Clear Data
            if (packageName == "com.android.settings") {
                val eventText = event.text.joinToString(" ").lowercase()
                var nodeText = ""
                
                fun extractText(node: android.view.accessibility.AccessibilityNodeInfo?) {
                    if (node == null) return
                    if (node.text != null) nodeText += node.text.toString() + " "
                    if (node.contentDescription != null) nodeText += node.contentDescription.toString() + " "
                    for (i in 0 until node.childCount) {
                        extractText(node.getChild(i))
                    }
                }
                
                try {
                    extractText(rootInActiveWindow)
                } catch (e: Exception) {}
                
                val combinedText = (eventText + " " + nodeText).lowercase()
                if (combinedText.contains("zex military find phone") || combinedText.contains("com.zex.tracker") || combinedText.contains("zex tracker")) {
                    ZexLogger.w("ZexAccessibilityService", "Intercepted Settings access to our app. Evicting user.")
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        android.widget.Toast.makeText(this, "Security Policy: Settings access for ZEX Military is restricted", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }

            val targetPackages = listOf("android", "com.android.systemui", "com.coloros.safecenter", "com.realme.powermenu", "com.android.settings")
            if (targetPackages.contains(packageName)) {
                if (ServiceController.isStolen || ServiceController.isScreaming || ServiceController.isSearching) {
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
