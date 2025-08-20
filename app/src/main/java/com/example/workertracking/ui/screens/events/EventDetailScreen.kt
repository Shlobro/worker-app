package com.example.workertracking.ui.screens.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workertracking.R
import com.example.workertracking.data.entity.Event
import com.example.workertracking.data.entity.EventWorker
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.ui.components.AddWorkerDialog
import com.example.workertracking.ui.viewmodel.EventWorkerWithName
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event?,
    eventWorkers: List<EventWorkerWithName> = emptyList(),
    allWorkers: List<Worker> = emptyList(),
    totalCost: Double = 0.0,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onEditEvent: () -> Unit = {},
    onDeleteEvent: () -> Unit = {},
    onAddWorkerToEvent: (Long, Long, Double, Boolean, Double, Double?) -> Unit = { _, _, _, _, _, _ -> }, // eventId, workerId, hours, isHourlyRate, payRate, refPayRate
    onRemoveWorker: (EventWorker) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddWorkerDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    val filteredWorkers = allWorkers.filter { worker ->
        worker.name.contains(searchQuery, ignoreCase = true) &&
        eventWorkers.none { it.eventWorker.workerId == worker.id }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = event?.name ?: stringResource(R.string.events_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (event != null) {
                        IconButton(onClick = onEditEvent) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit)
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            event?.let { eventData ->
                val profit = eventData.income - totalCost
                val profitColor = if (profit >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Event Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.event_details),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.date),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = dateFormatter.format(eventData.date),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.time),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${eventData.startTime} - ${eventData.endTime}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.hours),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = eventData.hours,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    // Financial Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.financial_summary),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.total_income),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₪${String.format("%.2f", eventData.income)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.worker_payments),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₪${String.format("%.2f", totalCost)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFF44336)
                                )
                            }
                            
                            HorizontalDivider()
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (profit >= 0) stringResource(R.string.profit) else stringResource(R.string.loss),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "₪${String.format("%.2f", kotlin.math.abs(profit))}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = profitColor
                                )
                            }
                        }
                    }
                    
                    // Workers Card
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.nav_workers),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { showAddWorkerDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = stringResource(R.string.add_worker)
                                    )
                                }
                            }
                            
                            if (eventWorkers.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.no_workers_assigned),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                eventWorkers.forEach { workerWithName ->
                                    // Calculate total payment including reference payment
                                    val workerPayment = if (workerWithName.eventWorker.isHourlyRate) {
                                        workerWithName.eventWorker.hours * workerWithName.eventWorker.payRate
                                    } else {
                                        workerWithName.eventWorker.payRate
                                    }
                                    val referencePayment = workerWithName.eventWorker.referencePayRate?.let { refRate ->
                                        workerWithName.eventWorker.hours * refRate
                                    } ?: 0.0
                                    val totalPayment = workerPayment + referencePayment
                                    
                                    // Find reference worker if exists
                                    val worker = allWorkers.find { it.id == workerWithName.eventWorker.workerId }
                                    val referenceWorker = worker?.referenceId?.let { refId ->
                                        allWorkers.find { it.id == refId }
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = workerWithName.workerName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = if (workerWithName.eventWorker.isHourlyRate) {
                                                    "${workerWithName.eventWorker.hours} שעות • ₪${workerWithName.eventWorker.payRate}/שעה"
                                                } else {
                                                    "${workerWithName.eventWorker.hours} שעות • ₪${workerWithName.eventWorker.payRate} (סכום קבוע)"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            
                                            // Show reference payment if exists
                                            if (workerWithName.eventWorker.referencePayRate != null && referenceWorker != null) {
                                                Text(
                                                    text = "תשלום מפנה (${referenceWorker.name}): ₪${workerWithName.eventWorker.referencePayRate}/שעה",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "₪${String.format("%.2f", totalPayment)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            IconButton(
                                                onClick = { onRemoveWorker(workerWithName.eventWorker) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.remove_worker),
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                    if (workerWithName != eventWorkers.last()) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Add Worker Dialog
    if (showAddWorkerDialog && event != null) {
        AddWorkerDialog(
            workers = filteredWorkers,
            allWorkers = allWorkers,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onDismiss = { 
                showAddWorkerDialog = false
                searchQuery = ""
            },
            onAddWorker = { workerId, isHourlyRate, payRate, refPayRate ->
                onAddWorkerToEvent(event.id, workerId, event.hours.toDoubleOrNull() ?: 0.0, isHourlyRate, payRate, refPayRate)
                showAddWorkerDialog = false
                searchQuery = ""
            },
            title = "הוסף עובד לאירוע",
            showPaymentType = true, // Events now support both hourly and fixed amounts
            showHours = false, // Use event's hours automatically
            eventHours = event.hours.toDoubleOrNull() ?: 0.0 // Pass event duration
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog && event != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(stringResource(R.string.delete_confirmation_title))
            },
            text = {
                Text(stringResource(R.string.delete_event_message, event.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteEvent()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.confirm_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(stringResource(R.string.cancel_delete))
                }
            }
        )
    }
}