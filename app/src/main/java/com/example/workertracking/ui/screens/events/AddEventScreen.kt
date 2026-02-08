package com.example.workertracking.ui.screens.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.AccessTime
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
import com.example.workertracking.ui.components.TimePickerDialog
import com.example.workertracking.ui.components.formatTime
import com.example.workertracking.ui.components.parseTimeString
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
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("17:00") }
    var hours by remember { mutableStateOf("") }
    var income by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var isAutoCalculate by remember { mutableStateOf(true) }
    var selectedEmployer by remember { mutableStateOf<Employer?>(null) }
    var showEmployerSelector by remember { mutableStateOf(false) }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun calculateHours(start: String, end: String): String {
        return try {
            val startParts = start.split(":")
            val endParts = end.split(":")

            if (startParts.size != 2 || endParts.size != 2) return ""

            val startHour = startParts[0].toInt()
            val startMinute = startParts[1].toInt()
            val endHour = endParts[0].toInt()
            val endMinute = endParts[1].toInt()

            // Validate time ranges
            if (startHour > 23 || startMinute > 59 || endHour > 23 || endMinute > 59) {
                return ""
            }

            var totalMinutes = (endHour * 60 + endMinute) - (startHour * 60 + startMinute)

            // Handle overnight shifts
            if (totalMinutes < 0) {
                totalMinutes += 24 * 60
            }

            val calculatedHours = totalMinutes / 60.0
            if (calculatedHours == calculatedHours.toInt().toDouble()) {
                calculatedHours.toInt().toString()
            } else {
                String.format(Locale.US, "%.1f", calculatedHours)
            }
        } catch (_: Exception) {
            ""
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
                onValueChange = { },
                label = { Text("שעת התחלה") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showStartTimePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "בחר שעת התחלה"
                        )
                    }
                }
            )

            OutlinedTextField(
                value = endTime,
                onValueChange = { },
                label = { Text("שעת סיום") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showEndTimePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "בחר שעת סיום"
                        )
                    }
                }
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = hours.isNotEmpty() && (hours.toDoubleOrNull()?.let { it <= 0 } != false),
                    supportingText = {
                        if (hours.isNotEmpty() && (hours.toDoubleOrNull()?.let { it <= 0 } != false)) {
                            Text("חובה להזין שעות תקינות גדולות מ-0")
                        }
                    }
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
            
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { showEmployerSelector = true }
            ) {
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
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
                        }
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
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
                    val hoursValue = hours.toDoubleOrNull()
                    if (eventName.isNotBlank() && hoursValue != null && hoursValue > 0) {
                        val incomeValue = income.toDoubleOrNull() ?: 0.0
                        onSaveEvent(eventName, selectedDate, startTime, endTime, hours, incomeValue, selectedEmployer?.id)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = eventName.isNotBlank() &&
                         hours.toDoubleOrNull()?.let { it > 0 } == true
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
    
    // Start Time Picker Dialog
    if (showStartTimePicker) {
        val parsedTime = parseTimeString(startTime)
        TimePickerDialog(
            initialHour = parsedTime?.first ?: 8,
            initialMinute = parsedTime?.second ?: 0,
            onTimeSelected = { hour, minute ->
                startTime = formatTime(hour, minute)
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false },
            title = "בחר שעת התחלה"
        )
    }

    // End Time Picker Dialog
    if (showEndTimePicker) {
        val parsedTime = parseTimeString(endTime)
        TimePickerDialog(
            initialHour = parsedTime?.first ?: 17,
            initialMinute = parsedTime?.second ?: 0,
            onTimeSelected = { hour, minute ->
                endTime = formatTime(hour, minute)
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false },
            title = "בחר שעת סיום"
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time
        )

        fun closeDialog() {
            showDatePicker = false
        }

        DatePickerDialog(
            onDateSelected = { dateMillis ->
                dateMillis?.let { selectedDate = Date(it) }
            },
            onDismiss = ::closeDialog,
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
                onDismiss()
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