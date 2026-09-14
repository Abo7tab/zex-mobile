package com.zex.tracker.core.logging

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveTerminalLogger @Inject constructor() {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    fun log(message: String) {
        val time = SimpleDateFormat("HH:mm:ssZ", Locale.US).format(Date())
        val formatted = "[$time] $message"
        val current = _logs.value.toMutableList()
        current.add(0, formatted) // Add to top
        if (current.size > 200) {
            current.removeAt(current.lastIndex)
        }
        _logs.value = current
    }
}
