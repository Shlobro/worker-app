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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
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
    unpaidReferenceShifts: List<UnpaidShiftWorkerInfo> = emptyList(),
    unpaidReferenceEvents: List<UnpaidEventWorkerInfo> = emptyList(),
    totalReferenceOwed: Double = 0.0,
    paidShifts: List<UnpaidShiftWorkerInfo> = emptyList(),
    paidEvents: List<UnpaidEventWorkerInfo> = emptyList(),
    showPaidItems: Boolean = false,
    dateFilter: Pair<Date?, Date?> = Pair(null, null),
    onNavigateBack: () -> Unit,
    onEditWorker: () -> Unit = {},
    onDeleteWorker: () -> Unit = {},
    onViewPhotos: () -> Unit = {},
    onMarkShiftAsPaid: (Long) -> Unit = {},
    onMarkEventAsPaid: (Long) -> Unit = {},
    onRevokeShiftPayment: (Long) -> Unit = {},
    onRevokeEventPayment: (Long) -> Unit = {},
    onMarkAllAsPaid: () -> Unit = {},
    onToggleShowPaidItems: () -> Unit = {},
    onDateRangeSelected: (Date?, Date?) -> Unit = { _, _ -> },
    onClearDateFilter: () -> Unit = {},
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
                                
                                if (totalOwed > 0) {
                                    Button(
                                        onClick = onMarkAllAsPaid,
                                        modifier = Modifier.padding(top = 8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.mark_all_as_paid),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                                
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
                                            amount = if (unpaidEvent.eventWorker.isHourlyRate) {
                                                unpaidEvent.eventWorker.hours * unpaidEvent.eventWorker.payRate
                                            } else {
                                                unpaidEvent.eventWorker.payRate
                                            },
                                            onMarkAsPaid = { onMarkEventAsPaid(unpaidEvent.eventWorker.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Toggle button to show/hide paid items
                item {
                    OutlinedButton(
                        onClick = { onToggleShowPaidItems() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showPaidItems) 
                                stringResource(R.string.hide_paid_items) 
                            else 
                                stringResource(R.string.show_paid_items)
                        )
                    }
                }
                
                // Reference payments owed TO this worker section
                // Filter to check if there are actual reference payments with amounts > 0
                val hasReferenceShiftsWithPayment = unpaidReferenceShifts.any { unpaidShift ->
                    (unpaidShift.shiftWorker.referencePayRate ?: 0.0) * unpaidShift.shiftHours > 0
                }
                val hasReferenceEventsWithPayment = unpaidReferenceEvents.any { unpaidEvent ->
                    (unpaidEvent.eventWorker.referencePayRate ?: 0.0) * unpaidEvent.eventWorker.hours > 0
                }
                
                if (totalReferenceOwed > 0 || hasReferenceShiftsWithPayment || hasReferenceEventsWithPayment) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "תשלומי הפניה",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                
                                Text(
                                    text = "₪${String.format("%.2f", totalReferenceOwed)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                
                                if (unpaidReferenceShifts.isNotEmpty()) {
                                    // Filter out shifts with zero reference payment
                                    val shiftsWithPayment = unpaidReferenceShifts.filter { unpaidShift ->
                                        (unpaidShift.shiftWorker.referencePayRate ?: 0.0) * unpaidShift.shiftHours > 0
                                    }
                                    
                                    if (shiftsWithPayment.isNotEmpty()) {
                                        Text(
                                            text = "משמרות בהפניה",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        
                                        shiftsWithPayment.forEach { unpaidShift ->
                                            WorkerDebtCard(
                                                type = "reference_shift",
                                                projectName = unpaidShift.projectName,
                                                date = unpaidShift.shiftDate,
                                                amount = (unpaidShift.shiftWorker.referencePayRate ?: 0.0) * unpaidShift.shiftHours,
                                                onMarkAsPaid = { onMarkShiftAsPaid(unpaidShift.shiftWorker.id) },
                                                isReference = true
                                            )
                                        }
                                    }
                                }
                                
                                if (unpaidReferenceEvents.isNotEmpty()) {
                                    // Filter out events with zero reference payment
                                    val eventsWithPayment = unpaidReferenceEvents.filter { unpaidEvent ->
                                        (unpaidEvent.eventWorker.referencePayRate ?: 0.0) * unpaidEvent.eventWorker.hours > 0
                                    }
                                    
                                    if (eventsWithPayment.isNotEmpty()) {
                                        Text(
                                            text = "אירועים בהפניה",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        
                                        eventsWithPayment.forEach { unpaidEvent ->
                                            WorkerDebtCard(
                                                type = "reference_event",
                                                projectName = unpaidEvent.eventName,
                                                date = unpaidEvent.eventDate,
                                                amount = (unpaidEvent.eventWorker.referencePayRate ?: 0.0) * unpaidEvent.eventWorker.hours,
                                                onMarkAsPaid = { onMarkEventAsPaid(unpaidEvent.eventWorker.id) },
                                                isReference = true
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Paid shifts and events section (shown only when toggled)
                if (showPaidItems) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.paid_history),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                
                                if (paidShifts.isNotEmpty()) {
                                    Text(
                                        text = stringResource(R.string.unpaid_shifts) + " (${stringResource(R.string.paid)})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    
                                    paidShifts.forEach { paidShift ->
                                        WorkerPaidCard(
                                            type = "shift",
                                            projectName = paidShift.projectName,
                                            date = paidShift.shiftDate,
                                            amount = if (paidShift.shiftWorker.isHourlyRate) {
                                                paidShift.shiftWorker.payRate * paidShift.shiftHours
                                            } else {
                                                paidShift.shiftWorker.payRate
                                            },
                                            onRevokePayment = { onRevokeShiftPayment(paidShift.shiftWorker.id) }
                                        )
                                    }
                                }
                                
                                if (paidEvents.isNotEmpty()) {
                                    Text(
                                        text = stringResource(R.string.unpaid_events) + " (${stringResource(R.string.paid)})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    
                                    paidEvents.forEach { paidEvent ->
                                        WorkerPaidCard(
                                            type = "event",
                                            projectName = paidEvent.eventName,
                                            date = paidEvent.eventDate,
                                            amount = if (paidEvent.eventWorker.isHourlyRate) {
                                                paidEvent.eventWorker.hours * paidEvent.eventWorker.payRate
                                            } else {
                                                paidEvent.eventWorker.payRate
                                            },
                                            onRevokePayment = { onRevokeEventPayment(paidEvent.eventWorker.id) }
                                        )
                                    }
                                }

                                // Show empty state if no paid items
                                if (paidShifts.isEmpty() && paidEvents.isEmpty()) {
                                    Text(
                                        text = "אין פריטים ששולמו עדיין",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Photo album section
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
                
                // Date filter section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.work_history),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        DateFilterChip(
                            startDate = dateFilter.first,
                            endDate = dateFilter.second,
                            onDateRangeSelected = onDateRangeSelected,
                            onClearFilter = onClearDateFilter
                        )
                    }
                }
                
                // Work History section
                if (allShifts.isNotEmpty() || allEvents.isNotEmpty()) {
                    
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
                                amount = (if (shiftInfo.shiftWorker.isHourlyRate) {
                                    shiftInfo.shiftWorker.payRate * shiftInfo.shiftHours
                                } else {
                                    shiftInfo.shiftWorker.payRate
                                }) + ((shiftInfo.shiftWorker.referencePayRate ?: 0.0) * shiftInfo.shiftHours),
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
                                amount = (if (eventInfo.eventWorker.isHourlyRate) {
                                    eventInfo.eventWorker.hours * eventInfo.eventWorker.payRate
                                } else {
                                    eventInfo.eventWorker.payRate
                                }) + ((eventInfo.eventWorker.referencePayRate ?: 0.0) * eventInfo.eventWorker.hours),
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
    onMarkAsPaid: () -> Unit,
    isReference: Boolean = false
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
private fun WorkerPaidCard(
    type: String,
    projectName: String,
    date: Long,
    amount: Double,
    onRevokePayment: () -> Unit
) {
    var showRevokeDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
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
                Text(
                    text = stringResource(R.string.paid),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₪${String.format("%.2f", amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedButton(
                    onClick = { showRevokeDialog = true },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.revoke_payment),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
    
    if (showRevokeDialog) {
        AlertDialog(
            onDismissRequest = { showRevokeDialog = false },
            title = { Text(stringResource(R.string.revoke_payment)) },
            text = { Text(stringResource(R.string.revoke_payment_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRevokePayment()
                        showRevokeDialog = false
                    }
                ) {
                    Text(stringResource(R.string.revoke_payment))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevokeDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun DateFilterChip(
    startDate: Date?,
    endDate: Date?,
    onDateRangeSelected: (Date?, Date?) -> Unit,
    onClearFilter: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yy", Locale.getDefault()) }
    
    val filterText = when {
        startDate != null && endDate != null -> {
            "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
        }
        startDate != null -> {
            "מ- ${dateFormat.format(startDate)}"
        }
        endDate != null -> {
            "עד ${dateFormat.format(endDate)}"
        }
        else -> stringResource(R.string.filter_by_date)
    }
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (startDate != null || endDate != null) {
            IconButton(
                onClick = onClearFilter,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.clear_filter),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        FilterChip(
            onClick = { showDatePicker = true },
            label = {
                Text(
                    text = filterText,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            selected = startDate != null || endDate != null,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        )
    }
    
    if (showDatePicker) {
        DateRangePickerDialog(
            startDate = startDate,
            endDate = endDate,
            onDateRangeSelected = { start, end ->
                onDateRangeSelected(start, end)
                showDatePicker = false
            },
            onDismiss = {
                showDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    startDate: Date?,
    endDate: Date?,
    onDateRangeSelected: (Date?, Date?) -> Unit,
    onDismiss: () -> Unit
) {
    var isSelectingStartDate by remember { mutableStateOf(true) }
    var tempStartDate by remember { mutableStateOf(startDate) }
    var tempEndDate by remember { mutableStateOf(endDate) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (isSelectingStartDate) startDate?.time else endDate?.time
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isSelectingStartDate) {
                    TextButton(
                        onClick = {
                            tempStartDate = datePickerState.selectedDateMillis?.let { Date(it) }
                            isSelectingStartDate = false
                            // Reset the picker for end date
                            datePickerState.selectedDateMillis = tempEndDate?.time
                        }
                    ) {
                        Text(stringResource(R.string.next))
                    }
                } else {
                    TextButton(
                        onClick = {
                            isSelectingStartDate = true
                            datePickerState.selectedDateMillis = tempStartDate?.time
                        }
                    ) {
                        Text(stringResource(R.string.back))
                    }
                    TextButton(
                        onClick = {
                            tempEndDate = datePickerState.selectedDateMillis?.let { Date(it) }
                            onDateRangeSelected(tempStartDate, tempEndDate)
                        }
                    ) {
                        Text(stringResource(R.string.apply))
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = {
            Column {
                Text(
                    text = if (isSelectingStartDate) 
                        stringResource(R.string.select_start_date) 
                    else 
                        stringResource(R.string.select_end_date)
                )
                if (tempStartDate != null || tempEndDate != null) {
                    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
                    Text(
                        text = when {
                            tempStartDate != null && tempEndDate != null -> 
                                "${dateFormat.format(tempStartDate!!)} - ${dateFormat.format(tempEndDate!!)}"
                            tempStartDate != null -> 
                                "מ- ${dateFormat.format(tempStartDate!!)}"
                            tempEndDate != null -> 
                                "עד ${dateFormat.format(tempEndDate!!)}"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.height(400.dp)
            )
        }
    )
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