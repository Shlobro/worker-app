package com.example.workertracking.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workertracking.R
import com.example.workertracking.ui.viewmodel.MoneyOwedViewModel
import com.example.workertracking.data.entity.UnpaidShiftWorkerInfo
import com.example.workertracking.data.entity.UnpaidEventWorkerInfo
import java.text.SimpleDateFormat
import java.util.*

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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                                text = "₪${String.format("%.2f", uiState.totalDebt)}",
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
                            imageVector = Icons.Default.List,
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
                        // Show worker's direct payment card
                        val workerPayment = if (unpaidShift.shiftWorker.isHourlyRate) {
                            unpaidShift.shiftWorker.payRate * unpaidShift.shiftHours
                        } else {
                            unpaidShift.shiftWorker.payRate
                        }
                        
                        if (workerPayment > 0) {
                            UnpaidShiftCard(
                                unpaidShift = unpaidShift,
                                onMarkAsPaid = { viewModel.markShiftAsPaid(it) },
                                onWorkerClick = onWorkerClick,
                                isReferencePayment = false,
                                displayAmount = workerPayment
                            )
                        }
                        
                        // Show reference worker payment card if exists
                        val referencePayment = unpaidShift.shiftWorker.referencePayRate?.let { refRate ->
                            refRate * unpaidShift.shiftHours
                        } ?: 0.0
                        
                        if (referencePayment > 0) {
                            UnpaidShiftCard(
                                unpaidShift = unpaidShift,
                                onMarkAsPaid = { viewModel.markShiftAsPaid(it) },
                                onWorkerClick = onWorkerClick,
                                isReferencePayment = true,
                                displayAmount = referencePayment
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
                        // Show worker's direct payment card
                        val workerPayment = if (unpaidEvent.eventWorker.isHourlyRate) {
                            unpaidEvent.eventWorker.hours * unpaidEvent.eventWorker.payRate
                        } else {
                            unpaidEvent.eventWorker.payRate
                        }
                        
                        if (workerPayment > 0) {
                            UnpaidEventCard(
                                unpaidEvent = unpaidEvent,
                                onMarkAsPaid = { viewModel.markEventAsPaid(it) },
                                onWorkerClick = onWorkerClick,
                                isReferencePayment = false,
                                displayAmount = workerPayment
                            )
                        }
                        
                        // Show reference worker payment card if exists
                        val referencePayment = unpaidEvent.eventWorker.referencePayRate?.let { refRate ->
                            refRate * unpaidEvent.eventWorker.hours
                        } ?: 0.0
                        
                        if (referencePayment > 0) {
                            UnpaidEventCard(
                                unpaidEvent = unpaidEvent,
                                onMarkAsPaid = { viewModel.markEventAsPaid(it) },
                                onWorkerClick = onWorkerClick,
                                isReferencePayment = true,
                                displayAmount = referencePayment
                            )
                        }
                    }
                }
                
                // Paid shifts (shown only when toggled)
                if (uiState.showPaidItems && uiState.paidShifts.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.paid_history) + " - ${stringResource(R.string.unpaid_shifts)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    items(uiState.paidShifts) { paidShift ->
                        // Show worker's direct payment card
                        val workerPayment = if (paidShift.shiftWorker.isHourlyRate) {
                            paidShift.shiftWorker.payRate * paidShift.shiftHours
                        } else {
                            paidShift.shiftWorker.payRate
                        }
                        
                        if (workerPayment > 0) {
                            PaidShiftCard(
                                paidShift = paidShift,
                                onRevokePayment = { viewModel.revokeShiftPayment(it) },
                                onWorkerClick = onWorkerClick,
                                isReferencePayment = false,
                                displayAmount = workerPayment
                            )
                        }
                        
                        // Show reference worker payment card if exists
                        val referencePayment = paidShift.shiftWorker.referencePayRate?.let { refRate ->
                            refRate * paidShift.shiftHours
                        } ?: 0.0
                        
                        if (referencePayment > 0) {
                            PaidShiftCard(
                                paidShift = paidShift,
                                onRevokePayment = { viewModel.revokeShiftPayment(it) },
                                onWorkerClick = onWorkerClick,
                                isReferencePayment = true,
                                displayAmount = referencePayment
                            )
                        }
                    }
                }

                // Paid events (shown only when toggled)
                if (uiState.showPaidItems && uiState.paidEvents.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.paid_history) + " - ${stringResource(R.string.unpaid_events)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    items(uiState.paidEvents) { paidEvent ->
                        // Show worker's direct payment card
                        val workerPayment = if (paidEvent.eventWorker.isHourlyRate) {
                            paidEvent.eventWorker.hours * paidEvent.eventWorker.payRate
                        } else {
                            paidEvent.eventWorker.payRate
                        }
                        
                        if (workerPayment > 0) {
                            PaidEventCard(
                                paidEvent = paidEvent,
                                onRevokePayment = { viewModel.revokeEventPayment(it) },
                                onWorkerClick = onWorkerClick,
                                isReferencePayment = false,
                                displayAmount = workerPayment
                            )
                        }
                        
                        // Show reference worker payment card if exists
                        val referencePayment = paidEvent.eventWorker.referencePayRate?.let { refRate ->
                            refRate * paidEvent.eventWorker.hours
                        } ?: 0.0
                        
                        if (referencePayment > 0) {
                            PaidEventCard(
                                paidEvent = paidEvent,
                                onRevokePayment = { viewModel.revokeEventPayment(it) },
                                onWorkerClick = onWorkerClick,
                                isReferencePayment = true,
                                displayAmount = referencePayment
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
                    TextButton(
                        onClick = { onWorkerClick(unpaidShift.shiftWorker.workerId) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column {
                            Text(
                                text = unpaidShift.workerName,
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
                            "תשלום הפניה: ₪${String.format("%.2f", displayAmount)}"
                        } else {
                            "₪${String.format("%.2f", displayAmount)}"
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
                    TextButton(
                        onClick = { onWorkerClick(unpaidEvent.eventWorker.workerId) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column {
                            Text(
                                text = unpaidEvent.workerName,
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
                            "תשלום הפניה: ₪${String.format("%.2f", displayAmount)}"
                        } else {
                            "₪${String.format("%.2f", displayAmount)}"
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
                    TextButton(
                        onClick = { onWorkerClick(paidShift.shiftWorker.workerId) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column {
                            Text(
                                text = paidShift.workerName,
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
                            "תשלום הפניה: ₪${String.format("%.2f", displayAmount)}"
                        } else {
                            "₪${String.format("%.2f", displayAmount)}"
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
            onDismissRequest = { showRevokeDialog = false },
            title = { Text(stringResource(R.string.revoke_payment)) },
            text = { Text(stringResource(R.string.revoke_payment_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRevokePayment(paidShift.shiftWorker.id)
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
                    TextButton(
                        onClick = { onWorkerClick(paidEvent.eventWorker.workerId) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column {
                            Text(
                                text = paidEvent.workerName,
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
                            "תשלום הפניה: ₪${String.format("%.2f", displayAmount)}"
                        } else {
                            "₪${String.format("%.2f", displayAmount)}"
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
            onDismissRequest = { showRevokeDialog = false },
            title = { Text(stringResource(R.string.revoke_payment)) },
            text = { Text(stringResource(R.string.revoke_payment_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRevokePayment(paidEvent.eventWorker.id)
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