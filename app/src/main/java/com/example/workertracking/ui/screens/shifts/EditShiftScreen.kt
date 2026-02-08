package com.example.workertracking.ui.screens.shifts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.workertracking.R
import com.example.workertracking.data.entity.Shift
import com.example.workertracking.ui.components.TimePickerDialog
import com.example.workertracking.ui.components.formatTime
import com.example.workertracking.ui.components.parseTimeString
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShiftScreen(
    shift: Shift?,
    projectName: String,
    onNavigateBack: () -> Unit,
    onUpdateShift: (String, Date, String, String, Double) -> Unit
) {
    var shiftName by remember { mutableStateOf(shift?.name ?: "") }
    var selectedDate by remember { mutableStateOf(shift?.date ?: Date()) }
    var startTime by remember { mutableStateOf(shift?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(shift?.endTime ?: "16:00") }
    var hours by remember { mutableStateOf(shift?.hours?.toString() ?: "") }
    var isManualHours by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    if (shift == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Shift not found")
        }
        return
    }

    // Calculate hours between two times (format: "HH:mm")
    fun calculateHours(start: String, end: String): Double? {
        return try {
            val startParts = start.split(":")
            val endParts = end.split(":")

            if (startParts.size != 2 || endParts.size != 2) return null

            val startHour = startParts[0].toInt()
            val startMinute = startParts[1].toInt()
            val endHour = endParts[0].toInt()
            val endMinute = endParts[1].toInt()

            // Validate time ranges
            if (startHour > 23 || startMinute > 59 || endHour > 23 || endMinute > 59) {
                return null
            }

            val startTotalMinutes = startHour * 60 + startMinute
            var endTotalMinutes = endHour * 60 + endMinute

            // Handle shifts that cross midnight
            if (endTotalMinutes <= startTotalMinutes) {
                endTotalMinutes += 24 * 60
            }

            val diffMinutes = endTotalMinutes - startTotalMinutes
            diffMinutes / 60.0
        } catch (e: Exception) {
            null
        }
    }

    // Update hours automatically when start or end time changes (only if not manually set)
    LaunchedEffect(startTime, endTime) {
        if (!isManualHours) {
            calculateHours(startTime, endTime)?.let { calculatedHours ->
                hours = if (calculatedHours == calculatedHours.toInt().toDouble()) {
                    calculatedHours.toInt().toString()
                } else {
                    String.format(Locale.US, "%.1f", calculatedHours)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ערוך משמרת - $projectName") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "פרטי המשמרת:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = shiftName,
                onValueChange = { shiftName = it },
                label = { Text("שם המשמרת") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("למשל: משמרת בוקר, משמרת ערב") }
            )

            OutlinedTextField(
                value = dateFormatter.format(selectedDate),
                onValueChange = { },
                label = { Text("תאריך") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "בחר תאריך"
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
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = hours,
                    onValueChange = {
                        hours = it
                        isManualHours = true // Mark as manually edited
                    },
                    label = { Text("מספר שעות") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    supportingText = {
                        if (!isManualHours) {
                            Text("מחושב אוטומטית")
                        } else {
                            Text("נערך ידנית")
                        }
                    }
                )

                if (isManualHours) {
                    TextButton(
                        onClick = {
                            isManualHours = false
                            // Recalculate hours
                            calculateHours(startTime, endTime)?.let { calculatedHours ->
                                hours = if (calculatedHours == calculatedHours.toInt().toDouble()) {
                                    calculatedHours.toInt().toString()
                                } else {
                                    String.format(Locale.US, "%.1f", calculatedHours)
                                }
                            }
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text("חשב אוטומטית")
                    }
                }
            }

            Button(
                onClick = {
                    val shiftHours = hours.toDoubleOrNull()
                    if (shiftName.isNotBlank() && shiftHours != null && shiftHours > 0) {
                        onUpdateShift(shiftName, selectedDate, startTime, endTime, shiftHours)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = shiftName.isNotBlank() &&
                        hours.toDoubleOrNull() != null &&
                        hours.toDoubleOrNull()!! > 0
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
            initialHour = parsedTime?.first ?: 16,
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
