package com.example.workertracking.ui.screens.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.workertracking.R
import com.example.workertracking.data.entity.IncomeType
import com.example.workertracking.data.entity.Project
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProjectScreen(
    project: Project?,
    onNavigateBack: () -> Unit,
    onUpdateProject: (String, String, Date, IncomeType, Double) -> Unit
) {
    var projectName by remember { mutableStateOf(project?.name ?: "") }
    var projectLocation by remember { mutableStateOf(project?.location ?: "") }
    var selectedDate by remember { mutableStateOf(project?.startDate ?: Date()) }
    var selectedIncomeType by remember { mutableStateOf(project?.incomeType ?: IncomeType.DAILY) }
    var incomeAmount by remember { mutableStateOf(project?.incomeAmount?.toString() ?: "") }
    var expanded by remember { mutableStateOf(false) }
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
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = getIncomeTypeDisplayName(selectedIncomeType),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text(stringResource(R.string.income_type)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    IncomeType.values().forEach { incomeType ->
                        DropdownMenuItem(
                            text = { Text(getIncomeTypeDisplayName(incomeType)) },
                            onClick = {
                                selectedIncomeType = incomeType
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = incomeAmount,
                onValueChange = { incomeAmount = it },
                label = { Text("${getIncomeTypeDisplayName(selectedIncomeType)} (${stringResource(R.string.currency_symbol)})") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (projectName.isNotBlank() && projectLocation.isNotBlank() && incomeAmount.isNotBlank()) {
                        val amount = incomeAmount.toDoubleOrNull() ?: 0.0
                        onUpdateProject(projectName, projectLocation, selectedDate, selectedIncomeType, amount)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = projectName.isNotBlank() && projectLocation.isNotBlank() && incomeAmount.isNotBlank()
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

@Composable
private fun getIncomeTypeDisplayName(incomeType: IncomeType): String {
    return when (incomeType) {
        IncomeType.DAILY -> stringResource(R.string.daily_rate)
        IncomeType.WEEKLY -> stringResource(R.string.weekly_rate)
        IncomeType.HOURLY -> stringResource(R.string.hourly_rate)
        IncomeType.MONTHLY -> stringResource(R.string.monthly_rate)
        IncomeType.FIXED -> stringResource(R.string.fixed_amount)
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