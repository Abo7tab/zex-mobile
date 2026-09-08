package com.zex.tracker.data.remote.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.domain.model.Command
import com.zex.tracker.domain.model.CommandStatus
import com.zex.tracker.domain.model.CommandType
import com.zex.tracker.service.CommandProcessor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCommandListener @Inject constructor(
    private val prefs: SecurePrefs,
    private val commandProcessor: CommandProcessor
) {
    private val db = FirebaseDatabase.getInstance()
    private var commandRef: com.google.firebase.database.DatabaseReference? = null
    private var listener: ValueEventListener? = null

    fun startListening() {
        val uid = prefs.getString(ZexConstants.KEY_DEVICE_UID)
        if (uid.isNullOrEmpty()) return

        ZexLogger.i("Firebase", "Starting Firebase RTDB listener for $uid")
        commandRef = db.getReference("devices/$uid/pending_commands")
        
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { child ->
                    try {
                        val idStr = child.key ?: return@forEach
                        val typeStr = child.child("type").getValue(String::class.java) ?: return@forEach
                        val type = CommandType.valueOf(typeStr)
                        
                        val params = mutableMapOf<String, String>()
                        child.child("parameters").children.forEach { p ->
                            params[p.key!!] = p.getValue(String::class.java) ?: ""
                        }

                        val cmd = Command(idStr.toInt(), type, params, CommandStatus.PENDING)
                        commandProcessor.process(cmd)
                        
                        // Remove from firebase after queuing
                        child.ref.removeValue()
                    } catch (e: Exception) {
                        ZexLogger.e("Firebase", "Failed parsing command", e)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                ZexLogger.e("Firebase", "RTDB Cancelled: ${error.message}")
            }
        }
        commandRef?.addValueEventListener(listener!!)
    }

    fun stopListening() {
        listener?.let { commandRef?.removeEventListener(it) }
    }
}

