package com.example.workertracking.ui.screens.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.workertracking.R
import com.example.workertracking.data.entity.Event
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    event: Event?,
    onNavigateBack: () -> Unit,
    onUpdateEvent: (String, Date, String, String, String, Double) -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    class TimeInputVisualTransformation : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            val digitsOnly = text.text.filter { it.isDigit() }.take(4)
            val formatted = when {
                digitsOnly.isEmpty() -> ""
                digitsOnly.length == 1 -> digitsOnly
                digitsOnly.length == 2 -> digitsOnly
                digitsOnly.length == 3 -> {
                    // For 3-digit input, check if it makes sense as HMM (e.g., "800" -> "08:00")
                    // But if the first digit would create invalid minutes (like "180" -> "1:80" or "999" -> "9:99"), 
                    // treat it as incomplete 4-digit input instead
                    val firstDigitHour = digitsOnly.substring(0, 1).toIntOrNull() ?: 0
                    val remainingMinutes = digitsOnly.substring(1).toIntOrNull() ?: 0
                    
                    if (firstDigitHour <= 9 && remainingMinutes <= 59) {
                        // Valid 3-digit format: H:MM (only hours 0-2 are valid for 3-digit)
                        val hours = digitsOnly.substring(0, 1).padStart(2, '0')
                        val minutes = digitsOnly.substring(1)
                        "$hours:$minutes"
                    } else {
                        // Invalid as 3-digit, show as incomplete 4-digit input
                        digitsOnly
                    }
                }
                digitsOnly.length >= 4 -> {
                    // Handle 4-digit input like "0800" or "1800" -> "08:00" or "18:00"
                    val hours = digitsOnly.substring(0, 2)
                    val minutes = digitsOnly.substring(2)
                    val h = hours.toIntOrNull() ?: 0
                    val m = minutes.toIntOrNull() ?: 0
                    if (h <= 23 && m <= 59) {
                        "$hours:$minutes"
                    } else {
                        ""
                    }
                }
                else -> digitsOnly
            }
            
            val offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    val digitsBeforeOffset = text.text.take(offset).count { it.isDigit() }
                    return when {
                        formatted.contains(":") -> {
                            // Has colon formatting
                            when {
                                digitsBeforeOffset <= 2 -> digitsBeforeOffset
                                else -> minOf(digitsBeforeOffset + 1, formatted.length) // +1 for colon, but cap at formatted length
                            }
                        }
                        else -> {
                            // No colon, direct mapping
                            minOf(digitsBeforeOffset, formatted.length)
                        }
                    }
                }
                
                override fun transformedToOriginal(offset: Int): Int {
                    return when {
                        formatted.contains(":") -> {
                            // Has colon formatting
                            when {
                                offset <= 2 -> offset
                                offset == 3 -> 2 // colon position maps to end of hours
                                else -> offset - 1 // account for the colon
                            }
                        }
                        else -> {
                            // No colon, direct mapping
                            offset
                        }
                    }
                }
            }
            
            return TransformedText(AnnotatedString(formatted), offsetMapping)
        }
    }
    
    fun calculateHours(start: String, end: String): String {
        if ((start.length != 3 && start.length != 4) || (end.length != 3 && end.length != 4) || 
            !start.all { it.isDigit() } || !end.all { it.isDigit() }) {
            return ""
        }
        
        try {
            // Pad to 4 digits if needed
            val startPadded = start.padStart(4, '0')
            val endPadded = end.padStart(4, '0')
            
            val startHour = startPadded.substring(0, 2).toInt()
            val startMinute = startPadded.substring(2, 4).toInt()
            val endHour = endPadded.substring(0, 2).toInt()
            val endMinute = endPadded.substring(2, 4).toInt()
            
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
            return if (calculatedHours == calculatedHours.toInt().toDouble()) {
                calculatedHours.toInt().toString()
            } else {
                String.format("%.1f", calculatedHours)
            }
        } catch (e: Exception) {
            return ""
        }
    }
    
    // State variables initialized after function definitions
    var eventName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }
    var income by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var isAutoCalculate by remember { mutableStateOf(true) }
    
    // Initialize state when event becomes available
    LaunchedEffect(event) {
        event?.let { eventData ->
            eventName = eventData.name
            selectedDate = eventData.date
            // Extract digits from time format (e.g., "08:30" -> "0830")
            startTime = eventData.startTime.filter { it.isDigit() }
            endTime = eventData.endTime.filter { it.isDigit() }
            hours = eventData.hours
            income = eventData.income.toString()
            
            // Determine if we should auto-calculate based on whether current hours match calculated hours
            isAutoCalculate = if (startTime.length == 4 && endTime.length == 4 && eventData.hours.isNotBlank()) {
                val calculatedHours = calculateHours(startTime, endTime)
                calculatedHours == eventData.hours
            } else {
                true
            }
        }
    }
    
    // Auto-calculate hours when start or end time changes
    LaunchedEffect(startTime, endTime, isAutoCalculate) {
        if (isAutoCalculate) {
            hours = calculateHours(startTime, endTime)
        }
    }
    
    if (event == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Event not found")
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_event)) },
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
                    val digitsOnly = input.filter { it.isDigit() }.take(4)
                    // Validate input based on length
                    when (digitsOnly.length) {
                        3 -> {
                            // For 3-digit input, only allow valid H:MM patterns (0-9 for first digit, 0-59 for minutes)
                            val firstDigit = digitsOnly.substring(0, 1).toIntOrNull() ?: 0
                            val minutes = digitsOnly.substring(1).toIntOrNull() ?: 0
                            if (firstDigit > 9 || minutes > 59) return@OutlinedTextField
                        }
                        4 -> {
                            // For 4-digit input, validate as HH:MM
                            val hours = digitsOnly.substring(0, 2).toIntOrNull() ?: 0
                            val minutes = digitsOnly.substring(2).toIntOrNull() ?: 0
                            if (hours > 23 || minutes > 59) return@OutlinedTextField
                        }
                    }
                    startTime = digitsOnly
                },
                label = { Text("שעת התחלה") },
                visualTransformation = TimeInputVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("800 או 0800") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("הקלד 3-4 ספרות (למשל: 800 או 0800 עבור 08:00)") }
            )
            
            OutlinedTextField(
                value = endTime,
                onValueChange = { input ->
                    val digitsOnly = input.filter { it.isDigit() }.take(4)
                    // Validate input based on length
                    when (digitsOnly.length) {
                        3 -> {
                            // For 3-digit input, only allow valid H:MM patterns (0-9 for first digit, 0-59 for minutes)
                            val firstDigit = digitsOnly.substring(0, 1).toIntOrNull() ?: 0
                            val minutes = digitsOnly.substring(1).toIntOrNull() ?: 0
                            if (firstDigit > 9 || minutes > 59) return@OutlinedTextField
                        }
                        4 -> {
                            // For 4-digit input, validate as HH:MM
                            val hours = digitsOnly.substring(0, 2).toIntOrNull() ?: 0
                            val minutes = digitsOnly.substring(2).toIntOrNull() ?: 0
                            if (hours > 23 || minutes > 59) return@OutlinedTextField
                        }
                    }
                    endTime = digitsOnly
                },
                label = { Text("שעת סיום") },
                visualTransformation = TimeInputVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("1700 או 800") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("הקלד 3-4 ספרות (למשל: 1700 או 800 עבור 17:00)") }
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (eventName.isNotBlank() && (startTime.length == 3 || startTime.length == 4) && (endTime.length == 3 || endTime.length == 4)) {
                        val incomeValue = income.toDoubleOrNull() ?: 0.0
                        // Pad times to 4 digits and format
                        val startPadded = startTime.padStart(4, '0')
                        val endPadded = endTime.padStart(4, '0')
                        val formattedStartTime = "${startPadded.substring(0, 2)}:${startPadded.substring(2)}"
                        val formattedEndTime = "${endPadded.substring(0, 2)}:${endPadded.substring(2)}"
                        onUpdateEvent(eventName, selectedDate, formattedStartTime, formattedEndTime, hours, incomeValue)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = eventName.isNotBlank() && (startTime.length == 3 || startTime.length == 4) && (endTime.length == 3 || endTime.length == 4)
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