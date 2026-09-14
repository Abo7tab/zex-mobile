package com.zex.tracker.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zex.tracker.core.logging.LiveTerminalLogger
import androidx.compose.foundation.shape.CutCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveAuditTerminalBottomSheet(
    logger: LiveTerminalLogger,
    onDismissRequest: () -> Unit
) {
    val logs by logger.logs.collectAsState()
    var terminalInput by remember { mutableStateOf("") }
    
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = bottomSheetState,
        containerColor = Color(0xFF0A0F16),
        contentColor = Color(0xFF00FA9A),
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp)
        ) {
            Text(
                "LIVE AUDIT TERMINAL // SEC-04",
                color = Color(0xFF00F0FF),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
            Divider(color = Color(0xFF00F0FF).copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
            
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(8.dp),
                reverseLayout = true
            ) {
                items(logs) { log ->
                    val color = if (log.contains("ERR") || log.contains("FAILED")) Color(0xFFFF3366) else Color(0xFF00FA9A)
                    Text(
                        text = log,
                        color = color,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = terminalInput,
                onValueChange = { terminalInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("SYS//INPUT >", fontFamily = FontFamily.Monospace, color = Color(0xFF00FA9A).copy(alpha = 0.5f)) },
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, color = Color(0xFF00FA9A)),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color(0xFF1E293B), unfocusedTextColor = Color(0xFF1E293B), 
                    focusedBorderColor = Color(0xFF00F0FF),
                    unfocusedBorderColor = Color(0xFF00F0FF).copy(alpha = 0.3f)
                ),
                shape = CutCornerShape(0.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
