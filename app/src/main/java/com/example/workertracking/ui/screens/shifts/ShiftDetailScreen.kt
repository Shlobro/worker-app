package com.example.workertracking.ui.screens.shifts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.workertracking.R
import com.example.workertracking.data.entity.Shift
import com.example.workertracking.data.entity.ShiftWorker
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.ui.components.AddWorkerDialog
import com.example.workertracking.ui.components.EditPaymentDialog
import com.example.workertracking.ui.components.PaymentDialog
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftDetailScreen(
    shift: Shift,
    shiftWorkers: List<Pair<ShiftWorker, Worker>>,
    allWorkers: List<Worker>,
    onNavigateBack: () -> Unit,
    onEditShift: () -> Unit = {},
    onDeleteShift: () -> Unit = {},
    onAddWorkerToShift: (Long, Long, Boolean, Double, Double?) -> Unit,
    onRemoveWorkerFromShift: (Long, Long) -> Unit,
    onUpdateWorkerPayment: (ShiftWorker) -> Unit,
    onUpdatePayment: (Long, Boolean, Double, Double) -> Unit = { _, _, _, _ -> }, // shiftWorkerId, isPaid, amount, tip
    onUpdateReferencePayment: (Long, Boolean, Double, Double) -> Unit = { _, _, _, _ -> } // shiftWorkerId, isPaid, amount, tip
) {
    var showAddWorkerDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showPaymentDialog by remember { mutableStateOf<ShiftWorker?>(null) }
    var showEditPaymentDialog by remember { mutableStateOf<ShiftWorker?>(null) }
    var showReferencePaymentDialog by remember { mutableStateOf<ShiftWorker?>(null) }
    var showReferenceEditPaymentDialog by remember { mutableStateOf<ShiftWorker?>(null) }
    var paymentDialogTotalDue by remember { mutableStateOf(0.0) }
    
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    val filteredWorkers = allWorkers.filter { worker ->
        worker.name.contains(searchQuery, ignoreCase = true) &&
        shiftWorkers.none { it.second.id == worker.id }
    }
    
    val totalCost = shiftWorkers.sumOf { (shiftWorker, _) ->
        val workerPayment = if (shiftWorker.isHourlyRate) {
            shiftWorker.payRate * shift.hours
        } else {
            shiftWorker.payRate
        }
        
        val referencePayment = shiftWorker.referencePayRate?.let { refRate ->
            refRate * shift.hours // Reference payments are always hourly
        } ?: 0.0
        
        workerPayment + referencePayment
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (shift.name.isNotBlank()) shift.name else "פרטי משמרת"
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
                    IconButton(onClick = onEditShift) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_shift)
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_shift),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddWorkerDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "הוסף עובד"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "פרטי המשמרת",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (shift.name.isNotBlank()) {
                            Text("שם: ${shift.name}")
                        }
                        Text("תאריך: ${dateFormatter.format(shift.date)}")
                        Text("שעת התחלה: ${shift.startTime}")
                        Text("שעת סיום: ${shift.endTime}")
                        Text("מספר שעות: ${shift.hours}")
                        Text(
                            text = "סכום כולל: ${String.format(Locale.getDefault(), "%.2f", totalCost)} ${stringResource(R.string.currency_symbol)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "עובדים במשמרת:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (shiftWorkers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "אין עובדים במשמרת זו",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showAddWorkerDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("הוסף עובד ראשון")
                            }
                        }
                    }
                }
            } else {
                items(shiftWorkers) { (shiftWorker, worker) ->
                    ShiftWorkerCard(
                        shiftWorker = shiftWorker,
                        worker = worker,
                        allWorkers = allWorkers,
                        shiftHours = shift.hours,
                        onRemove = { onRemoveWorkerFromShift(shift.id, worker.id) },
                        onUpdate = onUpdateWorkerPayment,
                        onPaymentClick = { totalDue ->
                            paymentDialogTotalDue = totalDue
                            showPaymentDialog = shiftWorker
                        },
                        onEditPaymentClick = { totalDue ->
                            paymentDialogTotalDue = totalDue
                            showEditPaymentDialog = shiftWorker
                        }
                    )
                }
            }
            
            // Show reference workers section if there are any reference payments
            val referencePayments = shiftWorkers.mapNotNull { (shiftWorker, worker) ->
                shiftWorker.referencePayRate?.let { refRate ->
                    worker.referenceId?.let { referenceId ->
                        val referenceWorker = allWorkers.find { it.id == referenceId }
                        referenceWorker?.let { refWorker ->
                            // Return: (ReferenceWorker, ShiftWorker record, Commission Amount)
                            Triple(refWorker, shiftWorker, refRate * shift.hours)
                        }
                    }
                }
            }

            if (referencePayments.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "עובדים מפנים:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(referencePayments) { (referenceWorker, shiftWorker, commissionAmount) ->
                    val referredWorker = allWorkers.find { it.id == shiftWorker.workerId }
                    ReferenceWorkerCard(
                        worker = referenceWorker,
                        referredWorkerName = referredWorker?.name,
                        shiftWorker = shiftWorker,
                        commissionAmount = commissionAmount,
                        onReferencePaymentClick = {
                            paymentDialogTotalDue = commissionAmount
                            showReferencePaymentDialog = shiftWorker
                        },
                        onReferenceEditPaymentClick = {
                            paymentDialogTotalDue = commissionAmount
                            showReferenceEditPaymentDialog = shiftWorker
                        }
                    )
                }
            }
        }
    }
    
    if (showAddWorkerDialog) {
        AddWorkerDialog(
            workers = filteredWorkers,
            allWorkers = allWorkers,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onDismiss = { 
                showAddWorkerDialog = false
                searchQuery = ""
            },
            onAddWorker = { workerId, isHourly, payRate, refPayRate ->
                onAddWorkerToShift(shift.id, workerId, isHourly, payRate, refPayRate)
                showAddWorkerDialog = false
                searchQuery = ""
            },
            title = "הוסף עובד למשמרת",
            showPaymentType = true,
            showHours = false
        )
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_confirmation_title)) },
            text = { 
                Text(
                    stringResource(R.string.delete_shift_message),
                    color = MaterialTheme.colorScheme.error
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteShift()
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

    // Payment Dialogs
    if (showPaymentDialog != null) {
        PaymentDialog(
            totalAmount = paymentDialogTotalDue,
            onConfirm = { isFullPayment, amountToPay, tip ->
                onUpdatePayment(showPaymentDialog!!.id, isFullPayment, amountToPay, tip)
                showPaymentDialog = null
            },
            onDismiss = { showPaymentDialog = null }
        )
    }

    if (showEditPaymentDialog != null) {
        EditPaymentDialog(
            currentPaidAmount = showEditPaymentDialog!!.amountPaid,
            currentTipAmount = showEditPaymentDialog!!.tipAmount,
            totalDue = paymentDialogTotalDue,
            isPaid = showEditPaymentDialog!!.isPaid,
            onConfirm = { isPaid, amount, tip ->
                onUpdatePayment(showEditPaymentDialog!!.id, isPaid, amount, tip)
                showEditPaymentDialog = null
            },
            onDismiss = { showEditPaymentDialog = null }
        )
    }

    if (showReferencePaymentDialog != null) {
        PaymentDialog(
            totalAmount = paymentDialogTotalDue,
            onConfirm = { isFullPayment, amountToPay, tip ->
                onUpdateReferencePayment(showReferencePaymentDialog!!.id, isFullPayment, amountToPay, tip)
                showReferencePaymentDialog = null
            },
            onDismiss = { showReferencePaymentDialog = null }
        )
    }

    if (showReferenceEditPaymentDialog != null) {
        EditPaymentDialog(
            currentPaidAmount = showReferenceEditPaymentDialog!!.referenceAmountPaid,
            currentTipAmount = showReferenceEditPaymentDialog!!.referenceTipAmount,
            totalDue = paymentDialogTotalDue,
            isPaid = showReferenceEditPaymentDialog!!.isReferencePaid,
            onConfirm = { isPaid, amount, tip ->
                onUpdateReferencePayment(showReferenceEditPaymentDialog!!.id, isPaid, amount, tip)
                showReferenceEditPaymentDialog = null
            },
            onDismiss = { showReferenceEditPaymentDialog = null }
        )
    }
}

@Composable
private fun ShiftWorkerCard(
    shiftWorker: ShiftWorker,
    worker: Worker,
    allWorkers: List<Worker>,
    shiftHours: Double,
    onRemove: () -> Unit,
    onUpdate: (ShiftWorker) -> Unit,
    onPaymentClick: (Double) -> Unit,
    onEditPaymentClick: (Double) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    
    val totalPayment = if (shiftWorker.isHourlyRate) {
        shiftWorker.payRate * shiftHours
    } else {
        shiftWorker.payRate
    }

    Card(
        modifier = Modifier.fillMaxWidth()
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
                    text = worker.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "טלפון: ${worker.phoneNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (shiftWorker.isHourlyRate) {
                        "שכר שעתי: ${shiftWorker.payRate} ש\"ח"
                    } else {
                        "סכום גלובלי: ${shiftWorker.payRate} ש\"ח"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "סכום: ${String.format(Locale.getDefault(), "%.2f", totalPayment)} ש\"ח",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Payment Status Display
                    if (shiftWorker.isPaid) {
                        TextButton(
                            onClick = { onEditPaymentClick(totalPayment) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.paid),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else if (shiftWorker.amountPaid > 0) {
                        // Partial Payment
                        TextButton(
                            onClick = { onEditPaymentClick(totalPayment) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "שולם חלקית: ₪${String.format(Locale.getDefault(), "%.2f", shiftWorker.amountPaid)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFFA000),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        TextButton(
                            onClick = { onPaymentClick(totalPayment) },
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
            }

            Row {
                TextButton(onClick = { showEditDialog = true }) {
                    Text("ערוך")
                }
                IconButton(
                    onClick = onRemove
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
    
    if (showEditDialog) {
        EditWorkerPaymentDialog(
            shiftWorker = shiftWorker,
            worker = worker,
            allWorkers = allWorkers,
            onDismiss = { showEditDialog = false },
            onUpdate = { updated ->
                onUpdate(updated)
                showEditDialog = false
            }
        )
    }
}


@Composable
private fun ReferenceWorkerCard(
    worker: Worker,
    referredWorkerName: String?,
    shiftWorker: ShiftWorker,
    commissionAmount: Double,
    onReferencePaymentClick: () -> Unit,
    onReferenceEditPaymentClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                    text = worker.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "עבור: ${referredWorkerName ?: "Unknown"}",
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
                        text = "₪${String.format(Locale.getDefault(), "%.2f", commissionAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    if (shiftWorker.isReferencePaid) {
                        TextButton(
                            onClick = onReferenceEditPaymentClick,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.paid),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else if (shiftWorker.referenceAmountPaid > 0) {
                        TextButton(
                            onClick = onReferenceEditPaymentClick,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "שולם חלקית: ₪${String.format(Locale.getDefault(), "%.2f", shiftWorker.referenceAmountPaid)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFFA000),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        TextButton(
                            onClick = onReferencePaymentClick,
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
        }
    }
}

@Composable
private fun EditWorkerPaymentDialog(
    shiftWorker: ShiftWorker,
    worker: Worker,
    allWorkers: List<Worker>,
    onDismiss: () -> Unit,
    onUpdate: (ShiftWorker) -> Unit
) {
    var isHourlyRate by remember { mutableStateOf(shiftWorker.isHourlyRate) }
    var payRate by remember { mutableStateOf(shiftWorker.payRate.toString()) }
    var referencePayRate by remember { mutableStateOf(shiftWorker.referencePayRate?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ערוך תשלום עובד") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { isHourlyRate = true },
                        label = { Text("שכר שעתי") },
                        selected = isHourlyRate
                    )
                    FilterChip(
                        onClick = { isHourlyRate = false },
                        label = { Text("סכום גלובלי") },
                        selected = !isHourlyRate
                    )
                }
                
                OutlinedTextField(
                    value = payRate,
                    onValueChange = { payRate = it },
                    label = { 
                        Text(if (isHourlyRate) "שכר שעתי (ש\"ח)" else "סכום גלובלי (ש\"ח)")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                // Show reference payment field if worker has a reference
                worker.referenceId?.let { referenceId ->
                    val referenceWorker = allWorkers.find { it.id == referenceId }
                    referenceWorker?.let { refWorker ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "תשלום לעובד מפנה: ${refWorker.name}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        OutlinedTextField(
                            value = referencePayRate,
                            onValueChange = { referencePayRate = it },
                            label = { 
                                Text("שכר שעתי לעובד מפנה (ש\"ח)")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val rate = payRate.toDoubleOrNull()
                    val refRate = if (worker.referenceId != null && referencePayRate.isNotBlank()) {
                        referencePayRate.toDoubleOrNull()
                    } else null
                    
                    if (rate != null && rate > 0 && 
                        (worker.referenceId == null || refRate != null)) {
                        onUpdate(
                            shiftWorker.copy(
                                isHourlyRate = isHourlyRate,
                                payRate = rate,
                                referencePayRate = refRate
                            )
                        )
                    }
                },
                enabled = payRate.toDoubleOrNull() != null && 
                         payRate.toDoubleOrNull()!! > 0 &&
                         (worker.referenceId == null || 
                          (referencePayRate.isNotBlank() && referencePayRate.toDoubleOrNull() != null))
            ) {
                Text("עדכן")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ביטול")
            }
        }
    )
}
