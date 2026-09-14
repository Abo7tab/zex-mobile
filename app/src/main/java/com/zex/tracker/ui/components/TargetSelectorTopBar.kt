package com.zex.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zex.tracker.data.remote.dto.DeviceDto

@Composable
fun TargetSelectorTopBar(
    devices: List<DeviceDto>,
    selectedDevice: DeviceDto?,
    onDeviceSelected: (DeviceDto) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF1E293B),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("ACTIVE TARGET DEVICE", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedDevice?.device_name?.takeIf { it.isNotBlank() } ?: selectedDevice?.device_uid ?: "No Device Selected",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f).background(Color(0xFF1E293B))
                ) {
                    if (devices.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No devices found", color = Color.White) },
                            onClick = { expanded = false }
                        )
                    } else {
                        devices.forEach { device ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(device.device_name?.takeIf { it.isNotBlank() } ?: "Unknown Device", color = Color.White, fontWeight = FontWeight.Bold)
                                        Text(device.device_uid ?: "", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    }
                                },
                                onClick = {
                                    onDeviceSelected(device)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
