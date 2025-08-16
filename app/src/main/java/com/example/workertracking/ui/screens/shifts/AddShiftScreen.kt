package com.example.workertracking.ui.screens.shifts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import com.example.workertracking.R
import com.example.workertracking.data.entity.Worker
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShiftScreen(
    projectId: Long,
    projectName: String,
    onNavigateBack: () -> Unit,
    onSaveShift: (Long, String, Date, String, String, Double) -> Unit
) {
    var shiftName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var startTimeInput by remember { mutableStateOf("") }
    var endTimeInput by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }
    var isManualHours by remember { mutableStateOf(false) }
    
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    // Format time input to HH:MM format
    fun formatTimeInput(input: String): String {
        val digitsOnly = input.filter { it.isDigit() }
        return when {
            digitsOnly.isEmpty() -> ""
            digitsOnly.length == 1 -> digitsOnly
            digitsOnly.length == 2 -> digitsOnly
            digitsOnly.length == 3 -> "${digitsOnly.substring(0, 1)}:${digitsOnly.substring(1)}"
            digitsOnly.length >= 4 -> {
                val hours = digitsOnly.substring(0, 2).padStart(2, '0')
                val minutes = digitsOnly.substring(2, minOf(4, digitsOnly.length)).padStart(2, '0')
                // Validate time format
                val h = hours.toIntOrNull() ?: 0
                val m = minutes.toIntOrNull() ?: 0
                if (h <= 23 && m <= 59) {
                    "$hours:$minutes"
                } else {
                    input.take(input.length - 1) // Remove last character if invalid
                }
            }
            else -> input
        }
    }
    
    // Calculate hours between two times
    fun calculateHours(start: String, end: String): Double? {
        try {
            if (start.matches(Regex("\\d{2}:\\d{2}")) && end.matches(Regex("\\d{2}:\\d{2}"))) {
                val startParts = start.split(":")
                val endParts = end.split(":")
                
                val startHour = startParts[0].toInt()
                val startMinute = startParts[1].toInt()
                val endHour = endParts[0].toInt()
                val endMinute = endParts[1].toInt()
                
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
                title = { Text("הוסף משמרת ל$projectName") },
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
                    IconButton(onClick = { /* TODO: Date picker */ }) {
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
                    val formatted = formatTimeInput(input)
                    if (formatted.length <= 5) { // Max HH:MM
                        startTimeInput = formatted
                    }
                },
                label = { Text("שעת התחלה") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text("0800 או 08:00") },
                supportingText = { Text("הקלד 4 ספרות (למשל: 0800 עבור 08:00)") }
            )
            
            OutlinedTextField(
                value = endTimeInput,
                onValueChange = { input ->
                    val formatted = formatTimeInput(input)
                    if (formatted.length <= 5) { // Max HH:MM
                        endTimeInput = formatted
                    }
                },
                label = { Text("שעת סיום") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text("1600 או 16:00") },
                supportingText = { Text("הקלד 4 ספרות (למשל: 1600 עבור 16:00)") }
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
                        startTimeInput.isNotBlank() && 
                        endTimeInput.isNotBlank() && 
                        shiftHours > 0 &&
                        startTimeInput.matches(Regex("\\d{2}:\\d{2}")) &&
                        endTimeInput.matches(Regex("\\d{2}:\\d{2}"))) {
                        onSaveShift(projectId, shiftName, selectedDate, startTimeInput, endTimeInput, shiftHours)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = shiftName.isNotBlank() &&
                          startTimeInput.isNotBlank() && 
                          endTimeInput.isNotBlank() &&
                          hours.toDoubleOrNull() != null && 
                          hours.toDoubleOrNull()!! > 0 &&
                          startTimeInput.matches(Regex("\\d{2}:\\d{2}")) &&
                          endTimeInput.matches(Regex("\\d{2}:\\d{2}"))
            ) {
                Text("שמור משמרת")
            }
        }
    }
}