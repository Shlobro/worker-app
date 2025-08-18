package com.example.workertracking.ui.screens.workers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.workertracking.R
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.ui.components.SearchableWorkerSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkerScreen(
    availableWorkers: List<Worker> = emptyList(),
    onNavigateBack: () -> Unit,
    onSaveWorker: (String, String, Long?) -> Unit
) {
    var workerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedReferenceWorker by remember { mutableStateOf<Worker?>(null) }
    var showWorkerSelector by remember { mutableStateOf(false) }
    var phoneNumberError by remember { mutableStateOf<String?>(null) }
    
    fun isValidPhoneNumber(phone: String): Boolean {
        val pattern = Regex("^\\d{3}-\\d{7}$")
        return pattern.matches(phone)
    }
    
    fun formatPhoneNumber(input: String): String {
        val digitsOnly = input.filter { it.isDigit() }
        return when {
            digitsOnly.length <= 3 -> digitsOnly
            digitsOnly.length <= 10 -> "${digitsOnly.substring(0, 3)}-${digitsOnly.substring(3)}"
            else -> "${digitsOnly.substring(0, 3)}-${digitsOnly.substring(3, 10)}"
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_worker)) },
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
                value = workerName,
                onValueChange = { workerName = it },
                label = { Text(stringResource(R.string.worker_name)) },
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
            
            Box {
                OutlinedTextField(
                    value = selectedReferenceWorker?.name ?: stringResource(R.string.no_reference),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text(stringResource(R.string.reference_worker)) },
                    trailingIcon = {
                        if (selectedReferenceWorker != null) {
                            IconButton(onClick = { selectedReferenceWorker = null }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear selection")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showWorkerSelector = true }
                )
            }
            
            if (showWorkerSelector) {
                SearchableWorkerSelector(
                    workers = availableWorkers,
                    onWorkerSelected = { worker ->
                        selectedReferenceWorker = worker
                        showWorkerSelector = false
                    },
                    onDismiss = { showWorkerSelector = false }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (workerName.isNotBlank() && phoneNumber.isNotBlank() && isValidPhoneNumber(phoneNumber)) {
                        onSaveWorker(workerName, phoneNumber, selectedReferenceWorker?.id)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = workerName.isNotBlank() && phoneNumber.isNotBlank() && isValidPhoneNumber(phoneNumber) && phoneNumberError == null
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
