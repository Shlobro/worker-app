package com.example.workertracking.ui.screens.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkerToEventScreen(
    eventId: Long,
    eventName: String,
    availableWorkers: List<Worker>,
    onNavigateBack: () -> Unit,
    onAddWorkerToEvent: (Long, Long, Double, Double) -> Unit
) {
    var selectedWorker by remember { mutableStateOf<Worker?>(null) }
    var hours by remember { mutableStateOf("") }
    var payRate by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("הוסף עובד ל$eventName") },
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableWorkers) { worker ->
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
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "סה\"כ: ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val totalPayment = (hours.toDoubleOrNull() ?: 0.0) * (payRate.toDoubleOrNull() ?: 0.0)
                    Text(
                        text = "${totalPayment} ${stringResource(R.string.currency_symbol)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Button(
                    onClick = {
                        selectedWorker?.let { worker ->
                            val eventHours = hours.toDoubleOrNull()
                            val rate = payRate.toDoubleOrNull()
                            if (eventHours != null && rate != null && eventHours > 0 && rate > 0) {
                                onAddWorkerToEvent(eventId, worker.id, eventHours, rate)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedWorker != null && 
                              hours.toDoubleOrNull() != null && 
                              hours.toDoubleOrNull()!! > 0 &&
                              payRate.toDoubleOrNull() != null && 
                              payRate.toDoubleOrNull()!! > 0
                ) {
                    Text("הוסף עובד לאירוע")
                }
            }
        }
    }
}