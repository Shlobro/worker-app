package com.example.workertracking.ui.screens.employers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.workertracking.R
import com.example.workertracking.data.entity.Employer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEmployerScreen(
    employer: Employer?,
    onNavigateBack: () -> Unit,
    onUpdateEmployer: (String, String) -> Unit
) {
    var employerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var phoneNumberError by remember { mutableStateOf<String?>(null) }
    
    fun isValidPhoneNumber(phone: String): Boolean {
        val pattern = Regex("^\\d{3}-\\d{7}$")
        return pattern.matches(phone)
    }
    
    fun formatPhoneNumber(input: String): String {
        val digitsOnly = input.filter { it.isDigit() }.take(10)
        return when {
            digitsOnly.length <= 3 -> digitsOnly
            else -> "${digitsOnly.substring(0, 3)}-${digitsOnly.substring(3)}"
        }
    }
    
    // Update fields when employer data becomes available
    LaunchedEffect(employer) {
        employer?.let {
            employerName = it.name
            phoneNumber = it.phoneNumber
        }
    }
    
    if (employer == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Employer not found")
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_employer)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = employerName,
                onValueChange = { employerName = it },
                label = { Text(stringResource(R.string.employer_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { input ->
                    val formatted = formatPhoneNumber(input)
                    phoneNumber = formatted
                    phoneNumberError = if (formatted.isNotEmpty() && !isValidPhoneNumber(formatted)) {
                        "פורמט מספר טלפון: xxx-xxxxxxx"
                    } else {
                        null
                    }
                },
                label = { Text(stringResource(R.string.phone_number)) },
                placeholder = { Text("050-1234567") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneNumberError != null,
                supportingText = phoneNumberError?.let { { Text(it) } }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (employerName.isNotBlank() && phoneNumber.isNotBlank() && isValidPhoneNumber(phoneNumber)) {
                        onUpdateEmployer(employerName, phoneNumber)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = employerName.isNotBlank() && phoneNumber.isNotBlank() && isValidPhoneNumber(phoneNumber) && phoneNumberError == null
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}