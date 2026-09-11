package com.zex.tracker.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var tab by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (uiState) {
            is SettingsUiState.Success -> {
                Toast.makeText(context, (uiState as SettingsUiState.Success).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            is SettingsUiState.Error -> {
                Toast.makeText(context, (uiState as SettingsUiState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إعدادات المالك") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("الملف الشخصي") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("كلمة المرور") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("رمز PIN") })
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val isLoading = uiState is SettingsUiState.Loading

            if (tab == 0) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("البريد الإلكتروني") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.updateProfile(name, email) }, 
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text(if (isLoading) "جاري الحفظ..." else "حفظ التغييرات")
                }
            } else if (tab == 1) {
                OutlinedTextField(value = currentPassword, onValueChange = { currentPassword = it }, label = { Text("كلمة المرور الحالية (مطلوبة)") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("كلمة المرور الجديدة") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("تأكيد كلمة المرور الجديدة") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        if (newPassword.isNotEmpty() && newPassword != confirmPassword) {
                            Toast.makeText(context, "كلمة المرور غير متطابقة", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateSecurity(currentPassword, newPassword, confirmPassword, "") 
                        }
                    }, 
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && currentPassword.isNotEmpty() && newPassword.isNotEmpty()
                ) {
                    Text(if (isLoading) "جاري التحديث..." else "تحديث كلمة المرور")
                }
            } else {
                OutlinedTextField(value = currentPassword, onValueChange = { currentPassword = it }, label = { Text("كلمة المرور الحالية (مطلوبة للتحقق)") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = pinCode, onValueChange = { pinCode = it }, label = { Text("رمز PIN (6 أرقام)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        viewModel.updateSecurity(currentPassword, "", "", pinCode) 
                    }, 
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && currentPassword.isNotEmpty() && pinCode.isNotEmpty()
                ) {
                    Text(if (isLoading) "جاري التحديث..." else "تحديث رمز PIN")
                }
            }
        }
    }
}

