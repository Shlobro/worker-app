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
import com.example.workertracking.ui.components.EditEventWorkerDialog
import com.example.workertracking.ui.components.EditPaymentDialog
import com.example.workertracking.ui.components.PaymentDialog
import com.example.workertracking.ui.viewmodel.EventWorkerWithName
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event?,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    eventWorkers: List<EventWorkerWithName> = emptyList(),
    allWorkers: List<Worker> = emptyList(),
    totalCost: Double = 0.0,
    onEditEvent: () -> Unit = {},
    onDeleteEvent: () -> Unit = {},
    onAddWorkerToEvent: (Long, Long, Double, Boolean, Double, Double?, Boolean) -> Unit = { _, _, _, _, _, _, _ -> },
    onRemoveWorker: (EventWorker) -> Unit = {},
    onUpdatePayment: (Long, Boolean, Double, Double) -> Unit = { _, _, _, _ -> },
    onUpdateReferencePayment: (Long, Boolean, Double, Double) -> Unit = { _, _, _, _ -> },
    onUpdateWorker: (EventWorker) -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddWorkerDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Payment Dialog State
    var showPaymentDialog by remember { mutableStateOf<EventWorker?>(null) }
    var showEditPaymentDialog by remember { mutableStateOf<EventWorker?>(null) }
    var showEditWorkerDialog by remember { mutableStateOf<EventWorker?>(null) }
    
    // Reference Payment Dialog State
    var showReferencePaymentDialog by remember { mutableStateOf<EventWorker?>(null) }
    var showReferenceEditPaymentDialog by remember { mutableStateOf<EventWorker?>(null) }
    
    var paymentDialogTotalDue by remember { mutableStateOf(0.0) }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    val workersById = remember(allWorkers) { allWorkers.associateBy { it.id } }

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
                                    text = "₪${String.format(Locale.getDefault(), "%.2f", eventData.income)}",
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
                                    text = "₪${String.format(Locale.getDefault(), "%.2f", totalCost)}",
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
                                    text = "₪${String.format(Locale.getDefault(), "%.2f", kotlin.math.abs(profit))}",
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
                            verticalArrangement = Arrangement.spacedBy(0.dp)
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

                            HorizontalDivider(
                                modifier = Modifier.padding(bottom = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            if (eventWorkers.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.no_workers_assigned),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                eventWorkers.forEach { workerWithName ->
                                    // Calculate worker payment
                                    val workerPayment = if (workerWithName.eventWorker.isHourlyRate) {
                                        workerWithName.eventWorker.hours * workerWithName.eventWorker.payRate
                                    } else {
                                        workerWithName.eventWorker.payRate
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

                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.End
                                            ) {
                                                Text(
                                                    text = "₪${String.format(Locale.getDefault(), "%.2f", workerPayment)}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium
                                                )

                                                // Payment Status Display
                                                if (workerWithName.eventWorker.isPaid) {
                                                    TextButton(
                                                        onClick = {
                                                            paymentDialogTotalDue = workerPayment
                                                            showEditPaymentDialog = workerWithName.eventWorker
                                                        },
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text(
                                                            text = stringResource(R.string.paid),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color(0xFF4CAF50),
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                } else if (workerWithName.eventWorker.amountPaid > 0) {
                                                    // Partial Payment
                                                    TextButton(
                                                        onClick = {
                                                            paymentDialogTotalDue = workerPayment
                                                            showEditPaymentDialog = workerWithName.eventWorker
                                                        },
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text(
                                                            text = stringResource(R.string.partial_paid_format, String.format(Locale.getDefault(), "%.2f", workerWithName.eventWorker.amountPaid)),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color(0xFFFFA000), // Orange for partial
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                } else {
                                                    TextButton(
                                                        onClick = {
                                                            paymentDialogTotalDue = workerPayment
                                                            showPaymentDialog = workerWithName.eventWorker
                                                        },
                                                        colors = ButtonDefaults.textButtonColors(
                                                            contentColor = MaterialTheme.colorScheme.primary
                                                        )
                                                    ) {
                                                        Text(
                                                            text = stringResource(R.string.mark_as_paid),
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                }
                                            }

                                            Row {
                                                IconButton(
                                                    onClick = { showEditWorkerDialog = workerWithName.eventWorker }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Edit Worker",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
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
                                    }
                                    if (workerWithName != eventWorkers.last()) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Reference Payments Card (separate from workers)
                    val referencePayments = eventWorkers.mapNotNull { workerWithName ->
                        workerWithName.eventWorker.referencePayRate?.let { refRate ->
                            val worker = workersById[workerWithName.eventWorker.workerId]
                            worker?.referenceId?.let { referenceId ->
                                workersById[referenceId]?.let { refWorker ->
                                    val commissionAmount = if (workerWithName.eventWorker.isReferenceHourlyRate) {
                                        refRate * workerWithName.eventWorker.hours
                                    } else {
                                        refRate
                                    }
                                    Triple(refWorker, workerWithName.eventWorker, commissionAmount)
                                }
                            }
                        }
                    }

                    if (referencePayments.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.reference_payments_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )

                                referencePayments.forEachIndexed { index, (referenceWorker, eventWorker, commissionAmount) ->
                                    val referredWorker = workersById[eventWorker.workerId]

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = referenceWorker.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = stringResource(R.string.for_worker_format, referredWorker?.name ?: stringResource(R.string.unknown_worker)),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = "₪${String.format(Locale.getDefault(), "%.2f", commissionAmount)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.secondary
                                            )

                                            if (eventWorker.isReferencePaid) {
                                                TextButton(
                                                    onClick = {
                                                        paymentDialogTotalDue = commissionAmount
                                                        showReferenceEditPaymentDialog = eventWorker
                                                    },
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text(
                                                        text = stringResource(R.string.paid),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color(0xFF4CAF50),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            } else if (eventWorker.referenceAmountPaid > 0) {
                                                TextButton(
                                                    onClick = {
                                                        paymentDialogTotalDue = commissionAmount
                                                        showReferenceEditPaymentDialog = eventWorker
                                                    },
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text(
                                                        text = stringResource(R.string.partial_paid_format, String.format(Locale.getDefault(), "%.2f", eventWorker.referenceAmountPaid)),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color(0xFFFFA000),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            } else {
                                                TextButton(
                                                    onClick = {
                                                        paymentDialogTotalDue = commissionAmount
                                                        showReferencePaymentDialog = eventWorker
                                                    },
                                                    colors = ButtonDefaults.textButtonColors(
                                                        contentColor = Color(0xFF4CAF50)
                                                    )
                                                ) {
                                                    Text(
                                                        text = stringResource(R.string.mark_as_paid),
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (index < referencePayments.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Payment Dialog (Main Worker)
    if (showPaymentDialog != null) {
        PaymentDialog(
            totalAmount = paymentDialogTotalDue,
            onConfirm = { isFullPayment, amount, tip ->
                val amountToPay = if (isFullPayment) paymentDialogTotalDue else amount
                onUpdatePayment(showPaymentDialog!!.id, isFullPayment, amountToPay, tip)
            },
            onDismiss = { }
        )
    }

    // Edit Payment Dialog (Main Worker)
    if (showEditPaymentDialog != null) {
        EditPaymentDialog(
            currentPaidAmount = showEditPaymentDialog!!.amountPaid,
            currentTipAmount = showEditPaymentDialog!!.tipAmount,
            totalDue = paymentDialogTotalDue,
            isPaid = showEditPaymentDialog!!.isPaid,
            onConfirm = { isPaid, amount, tip ->
                onUpdatePayment(showEditPaymentDialog!!.id, isPaid, amount, tip)
            },
            onDismiss = { }
        )
    }
    
    // Reference Payment Dialog
    if (showReferencePaymentDialog != null) {
        PaymentDialog(
            totalAmount = paymentDialogTotalDue,
            onConfirm = { isFullPayment, amount, tip ->
                val amountToPay = if (isFullPayment) paymentDialogTotalDue else amount
                onUpdateReferencePayment(showReferencePaymentDialog!!.id, isFullPayment, amountToPay, tip)
            },
            onDismiss = { }
        )
    }

    // Edit Reference Payment Dialog
    if (showReferenceEditPaymentDialog != null) {
        EditPaymentDialog(
            currentPaidAmount = showReferenceEditPaymentDialog!!.referenceAmountPaid,
            currentTipAmount = showReferenceEditPaymentDialog!!.referenceTipAmount,
            totalDue = paymentDialogTotalDue,
            isPaid = showReferenceEditPaymentDialog!!.isReferencePaid,
            onConfirm = { isPaid, amount, tip ->
                onUpdateReferencePayment(showReferenceEditPaymentDialog!!.id, isPaid, amount, tip)
            },
            onDismiss = { }
        )
    }
    
    // Edit Worker Dialog
    if (showEditWorkerDialog != null) {
        val worker = allWorkers.find { it.id == showEditWorkerDialog!!.workerId }
        val referenceWorker = worker?.referenceId?.let { refId ->
            allWorkers.find { it.id == refId }
        }
        
        if (worker != null) {
            EditEventWorkerDialog(
                eventWorker = showEditWorkerDialog!!,
                worker = worker,
                referenceWorker = referenceWorker,
                onDismiss = { },
                onConfirm = { updatedEventWorker ->
                    onUpdateWorker(updatedEventWorker)
                }
            )
        }
    }
    
    // Add Worker Dialog
    if (showAddWorkerDialog && event != null) {
        AddWorkerDialog(
            workers = filteredWorkers,
            allWorkers = allWorkers,
            searchQuery = searchQuery,
            onSearchQueryChange = { },
            onDismiss = {
            },
            onAddWorker = { workerId, isHourlyRate, payRate, refPayRate, isRefHourly ->
                onAddWorkerToEvent(event.id, workerId, event.hours.toDoubleOrNull() ?: 0.0, isHourlyRate, payRate, refPayRate, isRefHourly)
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
            onDismissRequest = { },
            title = {
                Text(stringResource(R.string.delete_confirmation_title))
            },
            text = {
                Text(stringResource(R.string.delete_event_message, event.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
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
                    onClick = { }
                ) {
                    Text(stringResource(R.string.cancel_delete))
                }
            }
        )
    }
}