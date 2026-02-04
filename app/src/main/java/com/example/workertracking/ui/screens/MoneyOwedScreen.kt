package com.example.workertracking.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workertracking.R
import com.example.workertracking.ui.viewmodel.MoneyOwedViewModel
import com.example.workertracking.data.entity.UnpaidShiftWorkerInfo
import com.example.workertracking.data.entity.UnpaidEventWorkerInfo
import com.example.workertracking.util.PaymentCalculator
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyOwedScreen(
    onNavigateBack: () -> Unit,
    onWorkerClick: (Long) -> Unit,
    viewModel: MoneyOwedViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.money_owed)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Summary card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.total_debt),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₪${String.format(Locale.US, "%.2f", uiState.totalDebt)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Toggle button to show/hide paid items
                item {
                    OutlinedButton(
                        onClick = { viewModel.toggleShowPaidItems() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.showPaidItems) 
                                stringResource(R.string.hide_paid_items) 
                            else 
                                stringResource(R.string.show_paid_items)
                        )
                    }
                }
                
                // Unpaid shifts
                if (uiState.unpaidShifts.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.unpaid_shifts),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(uiState.unpaidShifts) { unpaidShift ->
                        // Show worker's direct payment card (net amount)
                        val workerPaymentNet = max(0.0, PaymentCalculator.calculateNetPayment(
                            totalPayment = PaymentCalculator.calculateWorkerPayment(
                                payRate = unpaidShift.shiftWorker.payRate,
                                hours = unpaidShift.shiftHours,
                                isHourlyRate = unpaidShift.shiftWorker.isHourlyRate
                            ),
                            amountPaid = unpaidShift.shiftWorker.amountPaid,
                            tipAmount = unpaidShift.shiftWorker.tipAmount
                        ))

                        if (workerPaymentNet > 0 && !unpaidShift.shiftWorker.isPaid) {
                            UnpaidShiftCard(
                                unpaidShift = unpaidShift,
                                onMarkAsPaid = { viewModel.markShiftAsPaid(it) },
                                onWorkerClick = onWorkerClick,
                                isReferencePayment = false,
                                displayAmount = workerPaymentNet
                            )
                        }

                        // Show reference worker payment card if exists and not paid (net amount)
                        val referencePaymentNet = max(0.0, PaymentCalculator.calculateNetReferencePayment(
                            totalReferencePayment = PaymentCalculator.calculateReferencePayment(
                                referencePayRate = unpaidShift.shiftWorker.referencePayRate,
                                hours = unpaidShift.shiftHours,
                                isReferenceHourlyRate = unpaidShift.shiftWorker.isReferenceHourlyRate
                            ),
                            referenceAmountPaid = unpaidShift.shiftWorker.referenceAmountPaid,
                            referenceTipAmount = unpaidShift.shiftWorker.referenceTipAmount
                        ))

                        if (referencePaymentNet > 0 && !unpaidShift.shiftWorker.isReferencePaid) {
                            UnpaidShiftCard(
                                unpaidShift = unpaidShift,
                                onMarkAsPaid = { viewModel.markShiftReferenceAsPaid(it) },
                                onWorkerClick = onWorkerClick,
                                isReferencePayment = true,
                                displayAmount = referencePaymentNet
                            )
                        }
                    }
                }
                
                // Unpaid events
                if (uiState.unpaidEvents.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.unpaid_events),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(uiState.unpaidEvents) { unpaidEvent ->
                        // Show worker's direct payment card (net amount)
                        val workerPaymentNet = max(0.0, PaymentCalculator.calculateNetPayment(
                            totalPayment = PaymentCalculator.calculateWorkerPayment(
                                payRate = unpaidEvent.eventWorker.payRate,
                                hours = unpaidEvent.eventWorker.hours,
                                isHourlyRate = unpaidEvent.eventWorker.isHourlyRate
                            ),
                            amountPaid = unpaidEvent.eventWorker.amountPaid,
                            tipAmount = unpaidEvent.eventWorker.tipAmount
                        ))

                        if (workerPaymentNet > 0 && !unpaidEvent.eventWorker.isPaid) {
                            UnpaidEventCard(
                                unpaidEvent = unpaidEvent,
                                onMarkAsPaid = { viewModel.markEventAsPaid(it) },
                                onWorkerClick = onWorkerClick,
                                isReferencePayment = false,
                                displayAmount = workerPaymentNet
                            )
                        }

                        // Show reference worker payment card if exists and not paid (net amount)
                        val referencePaymentNet = max(0.0, PaymentCalculator.calculateNetReferencePayment(
                            totalReferencePayment = PaymentCalculator.calculateReferencePayment(
                                referencePayRate = unpaidEvent.eventWorker.referencePayRate,
                                hours = unpaidEvent.eventWorker.hours,
                                isReferenceHourlyRate = unpaidEvent.eventWorker.isReferenceHourlyRate
                            ),
                            referenceAmountPaid = unpaidEvent.eventWorker.referenceAmountPaid,
                            referenceTipAmount = unpaidEvent.eventWorker.referenceTipAmount
                        ))

                        if (referencePaymentNet > 0 && !unpaidEvent.eventWorker.isReferencePaid) {
                            UnpaidEventCard(
                                unpaidEvent = unpaidEvent,
                                onMarkAsPaid = { viewModel.markEventReferenceAsPaid(it) },
                                onWorkerClick = onWorkerClick,
                                isReferencePayment = true,
                                displayAmount = referencePaymentNet
                            )
                        }
                    }
                }
                
                // Paid shifts (shown only when toggled)
                if (uiState.showPaidItems) {
                    // Collect all paid shift items (from both paidShifts and unpaidShifts with paid references)
                    val hasPaidShiftItems = uiState.paidShifts.isNotEmpty() ||
                        uiState.unpaidShifts.any {
                            it.shiftWorker.isReferencePaid &&
                            PaymentCalculator.calculateReferencePayment(
                                it.shiftWorker.referencePayRate,
                                it.shiftHours,
                                it.shiftWorker.isReferenceHourlyRate
                            ) > 0
                        }

                    if (hasPaidShiftItems) {
                        item {
                            Text(
                                text = stringResource(R.string.paid_history) + " - ${stringResource(R.string.unpaid_shifts)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Show paid shifts from paidShifts collection
                        items(uiState.paidShifts) { paidShift ->
                            // Show worker's direct payment card - display amount paid (partial + tip)
                            val workerPayment = PaymentCalculator.calculateWorkerPayment(
                                payRate = paidShift.shiftWorker.payRate,
                                hours = paidShift.shiftHours,
                                isHourlyRate = paidShift.shiftWorker.isHourlyRate
                            )
                            // For paid items, show the total amount that was paid (amountPaid + tipAmount)
                            val workerAmountPaid = paidShift.shiftWorker.amountPaid + paidShift.shiftWorker.tipAmount

                            if (workerPayment > 0 && paidShift.shiftWorker.isPaid) {
                                PaidShiftCard(
                                    paidShift = paidShift,
                                    onRevokePayment = { viewModel.revokeShiftPayment(it) },
                                    onWorkerClick = onWorkerClick,
                                    isReferencePayment = false,
                                    displayAmount = if (workerAmountPaid > 0) workerAmountPaid else workerPayment
                                )
                            }

                            // Show reference worker payment card if exists and is paid
                            val referencePayment = PaymentCalculator.calculateReferencePayment(
                                referencePayRate = paidShift.shiftWorker.referencePayRate,
                                hours = paidShift.shiftHours,
                                isReferenceHourlyRate = paidShift.shiftWorker.isReferenceHourlyRate
                            )
                            // For paid items, show the total amount that was paid
                            val referenceAmountPaid = paidShift.shiftWorker.referenceAmountPaid + paidShift.shiftWorker.referenceTipAmount

                            if (referencePayment > 0 && paidShift.shiftWorker.isReferencePaid) {
                                PaidShiftCard(
                                    paidShift = paidShift,
                                    onRevokePayment = { viewModel.revokeShiftReferencePayment(it) },
                                    onWorkerClick = onWorkerClick,
                                    isReferencePayment = true,
                                    displayAmount = if (referenceAmountPaid > 0) referenceAmountPaid else referencePayment
                                )
                            }
                        }

                        // Show paid reference payments from unpaidShifts (where worker is unpaid but reference is paid)
                        items(uiState.unpaidShifts.filter { shift ->
                            val refPayment = PaymentCalculator.calculateReferencePayment(
                                shift.shiftWorker.referencePayRate,
                                shift.shiftHours,
                                shift.shiftWorker.isReferenceHourlyRate
                            )
                            shift.shiftWorker.isReferencePaid && refPayment > 0
                        }) { unpaidShift ->
                            val referencePayment = PaymentCalculator.calculateReferencePayment(
                                referencePayRate = unpaidShift.shiftWorker.referencePayRate,
                                hours = unpaidShift.shiftHours,
                                isReferenceHourlyRate = unpaidShift.shiftWorker.isReferenceHourlyRate
                            )
                            // For paid items, show the total amount that was paid
                            val referenceAmountPaid = unpaidShift.shiftWorker.referenceAmountPaid + unpaidShift.shiftWorker.referenceTipAmount
                            PaidShiftCard(
                                paidShift = unpaidShift,
                                onRevokePayment = { viewModel.revokeShiftReferencePayment(it) },
                                onWorkerClick = onWorkerClick,
                                isReferencePayment = true,
                                displayAmount = if (referenceAmountPaid > 0) referenceAmountPaid else referencePayment
                            )
                        }
                    }
                }

                // Paid events (shown only when toggled)
                if (uiState.showPaidItems) {
                    // Collect all paid event items (from both paidEvents and unpaidEvents with paid references)
                    val hasPaidEventItems = uiState.paidEvents.isNotEmpty() ||
                        uiState.unpaidEvents.any {
                            it.eventWorker.isReferencePaid &&
                            PaymentCalculator.calculateReferencePayment(
                                it.eventWorker.referencePayRate,
                                it.eventWorker.hours,
                                it.eventWorker.isReferenceHourlyRate
                            ) > 0
                        }

                    if (hasPaidEventItems) {
                        item {
                            Text(
                                text = stringResource(R.string.paid_history) + " - ${stringResource(R.string.unpaid_events)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Show paid events from paidEvents collection
                        items(uiState.paidEvents) { paidEvent ->
                            // Show worker's direct payment card - display amount paid (partial + tip)
                            val workerPayment = PaymentCalculator.calculateWorkerPayment(
                                payRate = paidEvent.eventWorker.payRate,
                                hours = paidEvent.eventWorker.hours,
                                isHourlyRate = paidEvent.eventWorker.isHourlyRate
                            )
                            // For paid items, show the total amount that was paid (amountPaid + tipAmount)
                            val workerAmountPaid = paidEvent.eventWorker.amountPaid + paidEvent.eventWorker.tipAmount

                            if (workerPayment > 0 && paidEvent.eventWorker.isPaid) {
                                PaidEventCard(
                                    paidEvent = paidEvent,
                                    onRevokePayment = { viewModel.revokeEventPayment(it) },
                                    onWorkerClick = onWorkerClick,
                                    isReferencePayment = false,
                                    displayAmount = if (workerAmountPaid > 0) workerAmountPaid else workerPayment
                                )
                            }

                            // Show reference worker payment card if exists and is paid
                            val referencePayment = PaymentCalculator.calculateReferencePayment(
                                referencePayRate = paidEvent.eventWorker.referencePayRate,
                                hours = paidEvent.eventWorker.hours,
                                isReferenceHourlyRate = paidEvent.eventWorker.isReferenceHourlyRate
                            )
                            // For paid items, show the total amount that was paid
                            val referenceAmountPaid = paidEvent.eventWorker.referenceAmountPaid + paidEvent.eventWorker.referenceTipAmount

                            if (referencePayment > 0 && paidEvent.eventWorker.isReferencePaid) {
                                PaidEventCard(
                                    paidEvent = paidEvent,
                                    onRevokePayment = { viewModel.revokeEventReferencePayment(it) },
                                    onWorkerClick = onWorkerClick,
                                    isReferencePayment = true,
                                    displayAmount = if (referenceAmountPaid > 0) referenceAmountPaid else referencePayment
                                )
                            }
                        }

                        // Show paid reference payments from unpaidEvents (where worker is unpaid but reference is paid)
                        items(uiState.unpaidEvents.filter { event ->
                            val refPayment = PaymentCalculator.calculateReferencePayment(
                                event.eventWorker.referencePayRate,
                                event.eventWorker.hours,
                                event.eventWorker.isReferenceHourlyRate
                            )
                            event.eventWorker.isReferencePaid && refPayment > 0
                        }) { unpaidEvent ->
                            val referencePayment = PaymentCalculator.calculateReferencePayment(
                                referencePayRate = unpaidEvent.eventWorker.referencePayRate,
                                hours = unpaidEvent.eventWorker.hours,
                                isReferenceHourlyRate = unpaidEvent.eventWorker.isReferenceHourlyRate
                            )
                            // For paid items, show the total amount that was paid
                            val referenceAmountPaid = unpaidEvent.eventWorker.referenceAmountPaid + unpaidEvent.eventWorker.referenceTipAmount
                            PaidEventCard(
                                paidEvent = unpaidEvent,
                                onRevokePayment = { viewModel.revokeEventReferencePayment(it) },
                                onWorkerClick = onWorkerClick,
                                isReferencePayment = true,
                                displayAmount = if (referenceAmountPaid > 0) referenceAmountPaid else referencePayment
                            )
                        }
                    }
                }
                
                if (uiState.unpaidShifts.isEmpty() && uiState.unpaidEvents.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.no_outstanding_payments),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.all_payments_up_to_date),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnpaidShiftCard(
    unpaidShift: UnpaidShiftWorkerInfo,
    onMarkAsPaid: (Long) -> Unit,
    onWorkerClick: (Long) -> Unit,
    isReferencePayment: Boolean = false,
    displayAmount: Double
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isReferencePayment) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val workerId = unpaidShift.shiftWorker.workerId
                    if (workerId != null) {
                        TextButton(
                            onClick = { onWorkerClick(workerId) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Column {
                                Text(
                                    text = unpaidShift.workerName ?: "(Deleted Worker)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isReferencePayment) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                                if (isReferencePayment) {
                                    Text(
                                        text = "עובד מפנה",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    } else {
                        Column {
                            Text(
                                text = "(Deleted Worker)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isReferencePayment) {
                                Text(
                                    text = "עובד מפנה",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    Text(
                        text = unpaidShift.projectName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = dateFormat.format(Date(unpaidShift.shiftDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${unpaidShift.startTime} - ${unpaidShift.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isReferencePayment) {
                            "תשלום הפניה: ₪${String.format(Locale.US, "%.2f", displayAmount)}"
                        } else {
                            "₪${String.format(Locale.US, "%.2f", displayAmount)}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isReferencePayment) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                    )
                    FilledTonalButton(
                        onClick = { onMarkAsPaid(unpaidShift.shiftWorker.id) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.mark_as_paid))
                    }
                }
            }
        }
    }
}

@Composable
private fun UnpaidEventCard(
    unpaidEvent: UnpaidEventWorkerInfo,
    onMarkAsPaid: (Long) -> Unit,
    onWorkerClick: (Long) -> Unit,
    isReferencePayment: Boolean = false,
    displayAmount: Double
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isReferencePayment) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val workerId = unpaidEvent.eventWorker.workerId
                    if (workerId != null) {
                        TextButton(
                            onClick = { onWorkerClick(workerId) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Column {
                                Text(
                                    text = unpaidEvent.workerName ?: "(Deleted Worker)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isReferencePayment) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                                if (isReferencePayment) {
                                    Text(
                                        text = "עובד מפנה",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    } else {
                        Column {
                            Text(
                                text = "(Deleted Worker)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isReferencePayment) {
                                Text(
                                    text = "עובד מפנה",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    Text(
                        text = unpaidEvent.eventName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = dateFormat.format(Date(unpaidEvent.eventDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${unpaidEvent.eventWorker.hours} שעות",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isReferencePayment) {
                            "תשלום הפניה: ₪${String.format(Locale.US, "%.2f", displayAmount)}"
                        } else {
                            "₪${String.format(Locale.US, "%.2f", displayAmount)}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isReferencePayment) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                    )
                    FilledTonalButton(
                        onClick = { onMarkAsPaid(unpaidEvent.eventWorker.id) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.mark_as_paid))
                    }
                }
            }
        }
    }
}

@Composable
private fun PaidShiftCard(
    paidShift: UnpaidShiftWorkerInfo,
    onRevokePayment: (Long) -> Unit,
    onWorkerClick: (Long) -> Unit,
    isReferencePayment: Boolean = false,
    displayAmount: Double
) {
    var showRevokeDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isReferencePayment) 
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val workerId = paidShift.shiftWorker.workerId
                    if (workerId != null) {
                        TextButton(
                            onClick = { onWorkerClick(workerId) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Column {
                                Text(
                                    text = paidShift.workerName ?: "(Deleted Worker)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isReferencePayment) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                                if (isReferencePayment) {
                                    Text(
                                        text = "עובד מפנה",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    } else {
                        Column {
                            Text(
                                text = "(Deleted Worker)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isReferencePayment) {
                                Text(
                                    text = "עובד מפנה",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    Text(
                        text = paidShift.projectName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = dateFormat.format(Date(paidShift.shiftDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${paidShift.startTime} - ${paidShift.endTime}",
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
                        text = if (isReferencePayment) {
                            "תשלום הפניה: ₪${String.format(Locale.US, "%.2f", displayAmount)}"
                        } else {
                            "₪${String.format(Locale.US, "%.2f", displayAmount)}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedButton(
                        onClick = { showRevokeDialog = true },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.revoke_payment))
                    }
                }
            }
        }
    }

    if (showRevokeDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.revoke_payment)) },
            text = { Text(stringResource(R.string.revoke_payment_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRevokePayment(paidShift.shiftWorker.id)
                    }
                ) {
                    Text(stringResource(R.string.revoke_payment))
                }
            },
            dismissButton = {
                TextButton(onClick = { }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun PaidEventCard(
    paidEvent: UnpaidEventWorkerInfo,
    onRevokePayment: (Long) -> Unit,
    onWorkerClick: (Long) -> Unit,
    isReferencePayment: Boolean = false,
    displayAmount: Double
) {
    var showRevokeDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isReferencePayment) 
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val workerId = paidEvent.eventWorker.workerId
                    if (workerId != null) {
                        TextButton(
                            onClick = { onWorkerClick(workerId) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Column {
                                Text(
                                    text = paidEvent.workerName ?: "(Deleted Worker)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isReferencePayment) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                                if (isReferencePayment) {
                                    Text(
                                        text = "עובד מפנה",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    } else {
                        Column {
                            Text(
                                text = "(Deleted Worker)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isReferencePayment) {
                                Text(
                                    text = "עובד מפנה",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    Text(
                        text = paidEvent.eventName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = dateFormat.format(Date(paidEvent.eventDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${paidEvent.eventWorker.hours} שעות",
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
                        text = if (isReferencePayment) {
                            "תשלום הפניה: ₪${String.format(Locale.US, "%.2f", displayAmount)}"
                        } else {
                            "₪${String.format(Locale.US, "%.2f", displayAmount)}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedButton(
                        onClick = { showRevokeDialog = true },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.revoke_payment))
                    }
                }
            }
        }
    }

    if (showRevokeDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.revoke_payment)) },
            text = { Text(stringResource(R.string.revoke_payment_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRevokePayment(paidEvent.eventWorker.id)
                    }
                ) {
                    Text(stringResource(R.string.revoke_payment))
                }
            },
            dismissButton = {
                TextButton(onClick = { }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}