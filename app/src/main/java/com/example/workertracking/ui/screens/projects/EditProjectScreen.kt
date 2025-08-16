package com.example.workertracking.ui.screens.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.workertracking.R
import com.example.workertracking.data.entity.Project
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProjectScreen(
    project: Project?,
    onNavigateBack: () -> Unit,
    onUpdateProject: (String, String, Date) -> Unit
) {
    var projectName by remember { mutableStateOf("") }
    var projectLocation by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    
    // Update fields when project data becomes available
    LaunchedEffect(project) {
        project?.let {
            projectName = it.name
            projectLocation = it.location
            selectedDate = it.startDate
        }
    }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    if (project == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Project not found")
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_project)) },
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (projectName.isNotBlank() && projectLocation.isNotBlank()) {
                        onUpdateProject(projectName, projectLocation, selectedDate)
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