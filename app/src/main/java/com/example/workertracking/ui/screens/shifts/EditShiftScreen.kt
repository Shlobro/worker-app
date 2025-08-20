package com.example.workertracking.ui.screens.shifts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.workertracking.R
import com.example.workertracking.data.entity.Shift
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
    var startTimeInput by remember { mutableStateOf(shift?.startTime?.filter { it.isDigit() } ?: "") }
    var endTimeInput by remember { mutableStateOf(shift?.endTime?.filter { it.isDigit() } ?: "") }
    var hours by remember { mutableStateOf(shift?.hours?.toString() ?: "") }
    var isManualHours by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    if (shift == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Shift not found")
        }
        return
    }
    
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
    
    // Calculate hours between two times (input format: digits only, e.g., "800", "0800", "1630")
    fun calculateHours(start: String, end: String): Double? {
        try {
            if ((start.length == 3 || start.length == 4) && (end.length == 3 || end.length == 4) && 
                start.all { it.isDigit() } && end.all { it.isDigit() }) {
                
                // Pad to 4 digits if needed
                val startPadded = start.padStart(4, '0')
                val endPadded = end.padStart(4, '0')
                
                val startHour = startPadded.substring(0, 2).toInt()
                val startMinute = startPadded.substring(2, 4).toInt()
                val endHour = endPadded.substring(0, 2).toInt()
                val endMinute = endPadded.substring(2, 4).toInt()
                
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
                return diffMinutes / 60.0
            }
        } catch (e: Exception) {
            // Ignore parsing errors
        }
        return null
    }
    
    // Update hours automatically when start or end time changes (only if not manually set)
    LaunchedEffect(startTimeInput, endTimeInput) {
        if (!isManualHours && startTimeInput.isNotBlank() && endTimeInput.isNotBlank()) {
            calculateHours(startTimeInput, endTimeInput)?.let { calculatedHours ->
                hours = if (calculatedHours == calculatedHours.toInt().toDouble()) {
                    calculatedHours.toInt().toString()
                } else {
                    String.format("%.1f", calculatedHours)
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
                value = startTimeInput,
                onValueChange = { input ->
                    val digitsOnly = input.filter { it.isDigit() }.take(4)
                    // Validate input based on length
                    when (digitsOnly.length) {
                        3 -> {
                            // For 3-digit input, allow all input as user might be typing 4-digit time
                            // Only validate if it could be a valid H:MM pattern
                            val firstDigit = digitsOnly.substring(0, 1).toIntOrNull() ?: 0
                            val minutes = digitsOnly.substring(1).toIntOrNull() ?: 0
                            // Allow input if first digit <= 2 (could become 2x:xx) or if minutes <= 59 (valid H:MM)
                            if (firstDigit <= 2 || minutes <= 59) {
                                startTimeInput = digitsOnly
                            }
                        }
                        4 -> {
                            // For 4-digit input, validate as HH:MM
                            val hours = digitsOnly.substring(0, 2).toIntOrNull() ?: 0
                            val minutes = digitsOnly.substring(2).toIntOrNull() ?: 0
                            if (hours <= 23 && minutes <= 59) {
                                startTimeInput = digitsOnly
                            }
                        }
                        else -> {
                            // For 1-2 digits, always allow
                            startTimeInput = digitsOnly
                        }
                    }
                },
                label = { Text("שעת התחלה") },
                visualTransformation = TimeInputVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text("800 או 0800") },
                supportingText = { Text("הקלד 3-4 ספרות (למשל: 800 או 0800 עבור 08:00)") }
            )
            
            OutlinedTextField(
                value = endTimeInput,
                onValueChange = { input ->
                    val digitsOnly = input.filter { it.isDigit() }.take(4)
                    // Validate input based on length
                    when (digitsOnly.length) {
                        3 -> {
                            // For 3-digit input, allow all input as user might be typing 4-digit time
                            // Only validate if it could be a valid H:MM pattern
                            val firstDigit = digitsOnly.substring(0, 1).toIntOrNull() ?: 0
                            val minutes = digitsOnly.substring(1).toIntOrNull() ?: 0
                            // Allow input if first digit <= 2 (could become 2x:xx) or if minutes <= 59 (valid H:MM)
                            if (firstDigit <= 2 || minutes <= 59) {
                                endTimeInput = digitsOnly
                            }
                        }
                        4 -> {
                            // For 4-digit input, validate as HH:MM
                            val hours = digitsOnly.substring(0, 2).toIntOrNull() ?: 0
                            val minutes = digitsOnly.substring(2).toIntOrNull() ?: 0
                            if (hours <= 23 && minutes <= 59) {
                                endTimeInput = digitsOnly
                            }
                        }
                        else -> {
                            // For 1-2 digits, always allow
                            endTimeInput = digitsOnly
                        }
                    }
                },
                label = { Text("שעת סיום") },
                visualTransformation = TimeInputVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text("1600 או 800") },
                supportingText = { Text("הקלד 3-4 ספרות (למשל: 1600 או 800 עבור 16:00)") }
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
                            if (startTimeInput.isNotBlank() && endTimeInput.isNotBlank()) {
                                calculateHours(startTimeInput, endTimeInput)?.let { calculatedHours ->
                                    hours = if (calculatedHours == calculatedHours.toInt().toDouble()) {
                                        calculatedHours.toInt().toString()
                                    } else {
                                        String.format("%.1f", calculatedHours)
                                    }
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
                    if (shiftName.isNotBlank() &&
                        shiftHours != null && 
                        (startTimeInput.length == 3 || startTimeInput.length == 4) && 
                        (endTimeInput.length == 3 || endTimeInput.length == 4) && 
                        shiftHours > 0) {
                        // Pad times to 4 digits and format
                        val startPadded = startTimeInput.padStart(4, '0')
                        val endPadded = endTimeInput.padStart(4, '0')
                        val formattedStartTime = "${startPadded.substring(0, 2)}:${startPadded.substring(2)}"
                        val formattedEndTime = "${endPadded.substring(0, 2)}:${endPadded.substring(2)}"
                        onUpdateShift(shiftName, selectedDate, formattedStartTime, formattedEndTime, shiftHours)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = shiftName.isNotBlank() &&
                          (startTimeInput.length == 3 || startTimeInput.length == 4) && 
                          (endTimeInput.length == 3 || endTimeInput.length == 4) &&
                          hours.toDoubleOrNull() != null && 
                          hours.toDoubleOrNull()!! > 0
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