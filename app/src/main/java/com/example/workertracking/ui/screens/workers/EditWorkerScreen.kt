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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.workertracking.R
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.ui.components.SearchableWorkerSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkerScreen(
    worker: Worker?,
    availableWorkers: List<Worker> = emptyList(),
    onNavigateBack: () -> Unit,
    onUpdateWorker: (String, String, Long?) -> Unit
) {
    var workerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedReferenceWorker by remember { mutableStateOf<Worker?>(null) }
    var showWorkerSelector by remember { mutableStateOf(false) }
    
    // Update fields when worker data becomes available
    LaunchedEffect(worker, availableWorkers) {
        worker?.let {
            workerName = it.name
            phoneNumber = it.phoneNumber
            selectedReferenceWorker = availableWorkers.find { availableWorker -> 
                availableWorker.id == it.referenceId 
            }
        }
    }
    
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
                    workers = availableWorkers.filter { it.id != worker.id },
                    onWorkerSelected = { selectedWorker ->
                        selectedReferenceWorker = selectedWorker
                        showWorkerSelector = false
                    },
                    onDismiss = { showWorkerSelector = false }
                )
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
