package com.example.workertracking.ui.screens.workers

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
import com.example.workertracking.data.entity.Worker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkerScreen(
    worker: Worker?,
    availableWorkers: List<Worker> = emptyList(),
    onNavigateBack: () -> Unit,
    onUpdateWorker: (String, String, Long?) -> Unit
) {
    var workerName by remember { mutableStateOf(worker?.name ?: "") }
    var phoneNumber by remember { mutableStateOf(worker?.phoneNumber ?: "") }
    var selectedReferenceWorker by remember { 
        mutableStateOf(availableWorkers.find { it.id == worker?.referenceId })
    }
    var expanded by remember { mutableStateOf(false) }
    
    if (worker == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Worker not found")
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_worker)) },
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
                onValueChange = { phoneNumber = it },
                label = { Text(stringResource(R.string.phone_number)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedReferenceWorker?.name ?: stringResource(R.string.no_reference),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text(stringResource(R.string.reference_worker)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.no_reference)) },
                        onClick = {
                            selectedReferenceWorker = null
                            expanded = false
                        }
                    )
                    // Filter out the current worker from the reference options
                    availableWorkers.filter { it.id != worker.id }.forEach { availableWorker ->
                        DropdownMenuItem(
                            text = { Text(availableWorker.name) },
                            onClick = {
                                selectedReferenceWorker = availableWorker
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (workerName.isNotBlank() && phoneNumber.isNotBlank()) {
                        onUpdateWorker(workerName, phoneNumber, selectedReferenceWorker?.id)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = workerName.isNotBlank() && phoneNumber.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}