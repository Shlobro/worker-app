package com.example.workertracking.ui.screens.workers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workertracking.R
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.data.entity.Project
import com.example.workertracking.data.entity.Event
import com.example.workertracking.data.entity.UnpaidShiftWorkerInfo
import com.example.workertracking.data.entity.UnpaidEventWorkerInfo
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDetailScreen(
    worker: Worker?,
    referenceWorker: Worker? = null,
    projects: List<Project> = emptyList(),
    events: List<Event> = emptyList(),
    isLoading: Boolean,
    unpaidShifts: List<UnpaidShiftWorkerInfo> = emptyList(),
    unpaidEvents: List<UnpaidEventWorkerInfo> = emptyList(),
    allShifts: List<UnpaidShiftWorkerInfo> = emptyList(),
    allEvents: List<UnpaidEventWorkerInfo> = emptyList(),
    totalOwed: Double = 0.0,
    onNavigateBack: () -> Unit,
    onEditWorker: () -> Unit = {},
    onDeleteWorker: () -> Unit = {},
    onViewPhotos: () -> Unit = {},
    onMarkShiftAsPaid: (Long) -> Unit = {},
    onMarkEventAsPaid: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(worker?.name ?: stringResource(R.string.workers_title))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    if (worker != null) {
                        IconButton(onClick = onEditWorker) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_worker)
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete_worker),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (worker != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.worker_name),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = worker.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.clickable {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${worker.phoneNumber}")
                                    }
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = stringResource(R.string.call_worker),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.phone_number),
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = worker.phoneNumber,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            if (worker.referenceId != null && referenceWorker != null) {
                                Text(
                                    text = stringResource(R.string.worker_of, referenceWorker.name),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
                
                // Money owed section
                if (totalOwed > 0 || unpaidShifts.isNotEmpty() || unpaidEvents.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.outstanding_amount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                
                                Text(
                                    text = "₪${String.format("%.2f", totalOwed)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                
                                if (unpaidShifts.isNotEmpty()) {
                                    Text(
                                        text = stringResource(R.string.unpaid_shifts),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    
                                    unpaidShifts.forEach { unpaidShift ->
                                        WorkerDebtCard(
                                            type = "shift",
                                            projectName = unpaidShift.projectName,
                                            date = unpaidShift.shiftDate,
                                            amount = if (unpaidShift.shiftWorker.isHourlyRate) {
                                                unpaidShift.shiftWorker.payRate * unpaidShift.shiftHours
                                            } else {
                                                unpaidShift.shiftWorker.payRate
                                            },
                                            onMarkAsPaid = { onMarkShiftAsPaid(unpaidShift.shiftWorker.id) }
                                        )
                                    }
                                }
                                
                                if (unpaidEvents.isNotEmpty()) {
                                    Text(
                                        text = stringResource(R.string.unpaid_events),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    
                                    unpaidEvents.forEach { unpaidEvent ->
                                        WorkerDebtCard(
                                            type = "event",
                                            projectName = unpaidEvent.eventName,
                                            date = unpaidEvent.eventDate,
                                            amount = unpaidEvent.eventWorker.hours * unpaidEvent.eventWorker.payRate,
                                            onMarkAsPaid = { onMarkEventAsPaid(unpaidEvent.eventWorker.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onViewPhotos() }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = stringResource(R.string.worker_photos),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = stringResource(R.string.worker_photos),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(R.string.photo_count, worker.photoUris.size),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Work History section
                if (allShifts.isNotEmpty() || allEvents.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.work_history),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // All shifts history
                    if (allShifts.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.all_shifts),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        
                        items(allShifts) { shiftInfo ->
                            WorkerHistoryCard(
                                type = "shift",
                                projectName = shiftInfo.projectName,
                                date = shiftInfo.shiftDate,
                                amount = if (shiftInfo.shiftWorker.isHourlyRate) {
                                    shiftInfo.shiftWorker.payRate * shiftInfo.shiftHours
                                } else {
                                    shiftInfo.shiftWorker.payRate
                                },
                                isPaid = shiftInfo.shiftWorker.isPaid,
                                onMarkAsPaid = if (!shiftInfo.shiftWorker.isPaid) {
                                    { onMarkShiftAsPaid(shiftInfo.shiftWorker.id) }
                                } else null
                            )
                        }
                    }
                    
                    // All events history
                    if (allEvents.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.all_events),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        
                        items(allEvents) { eventInfo ->
                            WorkerHistoryCard(
                                type = "event",
                                projectName = eventInfo.eventName,
                                date = eventInfo.eventDate,
                                amount = eventInfo.eventWorker.hours * eventInfo.eventWorker.payRate,
                                isPaid = eventInfo.eventWorker.isPaid,
                                onMarkAsPaid = if (!eventInfo.eventWorker.isPaid) {
                                    { onMarkEventAsPaid(eventInfo.eventWorker.id) }
                                } else null
                            )
                        }
                    }
                } else {
                    item {
                        Text(
                            text = stringResource(R.string.no_work_history),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.worker_not_found),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_confirmation_title)) },
            text = { 
                Text(
                    stringResource(R.string.delete_worker_message, worker?.name ?: ""),
                    color = MaterialTheme.colorScheme.error
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteWorker()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.confirm_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel_delete))
                }
            }
        )
    }
}

@Composable
private fun WorkerDebtCard(
    type: String,
    projectName: String,
    date: Long,
    amount: Double,
    onMarkAsPaid: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = projectName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateFormat.format(Date(date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₪${String.format("%.2f", amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                FilledTonalButton(
                    onClick = onMarkAsPaid,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.mark_as_paid),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkerHistoryCard(
    type: String,
    projectName: String,
    date: Long,
    amount: Double,
    isPaid: Boolean,
    onMarkAsPaid: (() -> Unit)? = null
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaid) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = projectName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateFormat.format(Date(date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isPaid) {
                    Text(
                        text = stringResource(R.string.paid),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₪${String.format("%.2f", amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPaid) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                if (!isPaid && onMarkAsPaid != null) {
                    FilledTonalButton(
                        onClick = onMarkAsPaid,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.mark_as_paid),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}