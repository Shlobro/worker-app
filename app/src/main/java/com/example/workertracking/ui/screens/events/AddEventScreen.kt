package com.example.workertracking.ui.screens.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.workertracking.R
import com.example.workertracking.data.entity.Employer
import com.example.workertracking.ui.components.SearchableEmployerSelector
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    availableEmployers: List<Employer> = emptyList(),
    onNavigateBack: () -> Unit,
    onSaveEvent: (String, Date, String, String, String, Double, Long?) -> Unit
) {
    var eventName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }
    var income by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var isAutoCalculate by remember { mutableStateOf(true) }
    var selectedEmployer by remember { mutableStateOf<Employer?>(null) }
    var showEmployerSelector by remember { mutableStateOf(false) }
    
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    fun formatTimeInput(input: String): String {
        val digitsOnly = input.filter { it.isDigit() }
        return when {
            digitsOnly.isEmpty() -> ""
            digitsOnly.length == 1 -> digitsOnly
            digitsOnly.length == 2 -> digitsOnly
            digitsOnly.length == 3 -> "${digitsOnly.substring(0, 1)}:${digitsOnly.substring(1)}"
            digitsOnly.length >= 4 -> {
                val hoursStr = digitsOnly.substring(0, 2).padStart(2, '0')
                val minutesStr = digitsOnly.substring(2, minOf(4, digitsOnly.length)).padStart(2, '0')
                // Validate time format
                val h = hoursStr.toIntOrNull() ?: 0
                val m = minutesStr.toIntOrNull() ?: 0
                if (h <= 23 && m <= 59) {
                    "$hoursStr:$minutesStr"
                } else {
                    input.take(input.length - 1) // Remove last character if invalid
                }
            }
            else -> input
        }
    }
    
    fun calculateHours(start: String, end: String): String {
        if (start.isBlank() || end.isBlank() || !start.contains(":") || !end.contains(":")) {
            return ""
        }
        
        try {
            val startParts = start.split(":")
            val endParts = end.split(":")
            val startHour = startParts[0].toInt()
            val startMinute = startParts[1].toInt()
            val endHour = endParts[0].toInt()
            val endMinute = endParts[1].toInt()
            
            var totalMinutes = (endHour * 60 + endMinute) - (startHour * 60 + startMinute)
            
            // Handle overnight shifts
            if (totalMinutes < 0) {
                totalMinutes += 24 * 60
            }
            
            val calculatedHours = totalMinutes / 60.0
            return if (calculatedHours == calculatedHours.toInt().toDouble()) {
                calculatedHours.toInt().toString()
            } else {
                String.format("%.1f", calculatedHours)
            }
        } catch (e: Exception) {
            return ""
        }
    }
    
    // Auto-calculate hours when start or end time changes
    LaunchedEffect(startTime, endTime, isAutoCalculate) {
        if (isAutoCalculate) {
            hours = calculateHours(startTime, endTime)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_event)) },
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
                value = eventName,
                onValueChange = { eventName = it },
                label = { Text(stringResource(R.string.event_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = dateFormatter.format(selectedDate),
                onValueChange = { },
                label = { Text(stringResource(R.string.event_date)) },
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
            
            OutlinedTextField(
                value = startTime,
                onValueChange = { input ->
                    val formatted = formatTimeInput(input)
                    startTime = formatted
                },
                label = { Text("שעת התחלה") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("HH:MM") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            OutlinedTextField(
                value = endTime,
                onValueChange = { input ->
                    val formatted = formatTimeInput(input)
                    endTime = formatted
                },
                label = { Text("שעת סיום") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("HH:MM") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = hours,
                    onValueChange = { 
                        hours = it
                        isAutoCalculate = false
                    },
                    label = { Text("שעות") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("0.0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                Button(
                    onClick = {
                        isAutoCalculate = true
                        hours = calculateHours(startTime, endTime)
                    },
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text("חשב אוטומטית")
                }
            }
            
            OutlinedTextField(
                value = income,
                onValueChange = { income = it },
                label = { Text("הכנסה מהאירוע") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("0.0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
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
                    if (eventName.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()) {
                        val incomeValue = income.toDoubleOrNull() ?: 0.0
                        onSaveEvent(eventName, selectedDate, startTime, endTime, hours, incomeValue, selectedEmployer?.id)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = eventName.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()
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