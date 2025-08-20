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
import androidx.compose.material.icons.filled.Search
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
import java.text.SimpleDateFormat
import java.util.*

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
    onUpdateWorkerPayment: (ShiftWorker) -> Unit
) {
    var showAddWorkerDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
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
                            text = "סכום כולל: ${String.format("%.2f", totalCost)} ${stringResource(R.string.currency_symbol)}",
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
                        onUpdate = onUpdateWorkerPayment
                    )
                }
            }
            
            // Show reference workers section if there are any reference payments
            val referencePayments = shiftWorkers.mapNotNull { (shiftWorker, worker) ->
                shiftWorker.referencePayRate?.let { refRate ->
                    worker.referenceId?.let { referenceId ->
                        val referenceWorker = allWorkers.find { it.id == referenceId }
                        referenceWorker?.let { refWorker ->
                            Triple(refWorker, refRate, refRate * shift.hours)
                        }
                    }
                }
            }.groupBy { it.first.id }.map { (_, payments) ->
                val refWorker = payments.first().first
                val totalRefPayment = payments.sumOf { it.third }
                Pair(refWorker, totalRefPayment)
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
                
                items(referencePayments) { (referenceWorker, totalPayment) ->
                    ReferenceWorkerCard(
                        worker = referenceWorker,
                        totalPayment = totalPayment
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
}

@Composable
private fun ShiftWorkerCard(
    shiftWorker: ShiftWorker,
    worker: Worker,
    allWorkers: List<Worker>,
    shiftHours: Double,
    onRemove: () -> Unit,
    onUpdate: (ShiftWorker) -> Unit
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
                
                
                val referencePayment = shiftWorker.referencePayRate?.let { refRate ->
                    refRate * shiftHours
                } ?: 0.0
                
                Text(
                    text = "סכום: ${String.format("%.2f", totalPayment)} ש\"ח",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
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
    totalPayment: Double
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
                    text = "טלפון: ${worker.phoneNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "עובד מפנה",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Text(
                text = "סכום: ${String.format("%.2f", totalPayment)} ש\"ח",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
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