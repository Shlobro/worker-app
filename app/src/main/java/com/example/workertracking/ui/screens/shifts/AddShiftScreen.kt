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
    allWorkers: List<Worker>,
    onNavigateBack: () -> Unit,
    onSaveShift: (Long, Long, Date, String, Double, Double) -> Unit
) {
    var selectedWorker by remember { mutableStateOf<Worker?>(null) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var startTime by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }
    var payRate by remember { mutableStateOf("") }
    
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
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
                text = "בחר עובד:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            LazyColumn(
                modifier = Modifier.weight(0.4f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allWorkers) { worker ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedWorker = worker }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedWorker?.id == worker.id,
                                onClick = { selectedWorker = worker }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = worker.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "טלפון: ${worker.phoneNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            if (selectedWorker != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("שעת התחלה (HH:MM)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        placeholder = { Text("08:00") }
                    )
                    
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { hours = it },
                        label = { Text("מספר שעות") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = payRate,
                        onValueChange = { payRate = it },
                        label = { Text("שכר שעתי (${stringResource(R.string.currency_symbol)})") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    
                    Button(
                        onClick = {
                            selectedWorker?.let { worker ->
                                val shiftHours = hours.toDoubleOrNull()
                                val rate = payRate.toDoubleOrNull()
                                if (shiftHours != null && rate != null && 
                                    startTime.isNotBlank() && shiftHours > 0 && rate > 0) {
                                    onSaveShift(projectId, worker.id, selectedDate, startTime, shiftHours, rate)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedWorker != null && 
                                  startTime.isNotBlank() && 
                                  hours.toDoubleOrNull() != null && 
                                  hours.toDoubleOrNull()!! > 0 &&
                                  payRate.toDoubleOrNull() != null && 
                                  payRate.toDoubleOrNull()!! > 0
                    ) {
                        Text("שמור משמרת")
                    }
                }
            }
        }
    }
}