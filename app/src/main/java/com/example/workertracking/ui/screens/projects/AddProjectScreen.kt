package com.example.workertracking.ui.screens.projects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.workertracking.R
import com.example.workertracking.data.entity.Employer
import com.example.workertracking.ui.components.SearchableEmployerSelector
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectScreen(
    availableEmployers: List<Employer> = emptyList(),
    onNavigateBack: () -> Unit,
    onSaveProject: (String, String, Date, Long?) -> Unit
) {
    var projectName by remember { mutableStateOf("") }
    var projectLocation by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedEmployer by remember { mutableStateOf<Employer?>(null) }
    var showEmployerSelector by remember { mutableStateOf(false) }
    
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_project)) },
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
                value = projectName,
                onValueChange = { projectName = it },
                label = { Text(stringResource(R.string.project_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = projectLocation,
                onValueChange = { projectLocation = it },
                label = { Text(stringResource(R.string.project_location)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = dateFormatter.format(selectedDate),
                onValueChange = { },
                label = { Text(stringResource(R.string.start_date)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.select_date)
                        )
                    }
                }
            )
            
            Box {
                OutlinedTextField(
                    value = selectedEmployer?.name ?: stringResource(R.string.no_employer),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text(stringResource(R.string.select_employer)) },
                    trailingIcon = {
                        if (selectedEmployer != null) {
                            IconButton(onClick = { selectedEmployer = null }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear selection")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showEmployerSelector = true }
                )
            }
            
            if (showEmployerSelector) {
                SearchableEmployerSelector(
                    employers = availableEmployers,
                    onEmployerSelected = { employer ->
                        selectedEmployer = employer
                        showEmployerSelector = false
                    },
                    onDismiss = { showEmployerSelector = false }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (projectName.isNotBlank() && projectLocation.isNotBlank()) {
                        onSaveProject(projectName, projectLocation, selectedDate, selectedEmployer?.id)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = projectName.isNotBlank() && projectLocation.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time
        )
        
        DatePickerDialog(
            onDateSelected = { dateMillis ->
                dateMillis?.let {
                    selectedDate = Date(it)
                }
                showDatePicker = false
            },
            onDismiss = {
                showDatePicker = false
            },
            datePickerState = datePickerState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    datePickerState: DatePickerState
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
            }) {
                Text(stringResource(R.string.select))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        text = {
            DatePicker(state = datePickerState)
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(16.dp)
    )
}

