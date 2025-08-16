package com.example.workertracking.ui.screens.shifts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftDetailScreen(
    shift: Shift,
    shiftWorkers: List<Pair<ShiftWorker, Worker>>,
    allWorkers: List<Worker>,
    onNavigateBack: () -> Unit,
    onAddWorkerToShift: (Long, Long, Boolean, Double) -> Unit,
    onRemoveWorkerFromShift: (Long, Long) -> Unit,
    onUpdateWorkerPayment: (ShiftWorker) -> Unit
) {
    var showAddWorkerDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    val filteredWorkers = allWorkers.filter { worker ->
        worker.name.contains(searchQuery, ignoreCase = true) &&
        shiftWorkers.none { it.second.id == worker.id }
    }
    
    val totalCost = shiftWorkers.sumOf { (shiftWorker, _) ->
        if (shiftWorker.isHourlyRate) {
            shiftWorker.payRate * shift.hours
        } else {
            shiftWorker.payRate
        }
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
                    IconButton(onClick = { showAddWorkerDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "הוסף עובד"
                        )
                    }
                }
            )
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
                            Text(
                                text = "לחץ על + כדי להוסיף עובדים",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                items(shiftWorkers) { (shiftWorker, worker) ->
                    ShiftWorkerCard(
                        shiftWorker = shiftWorker,
                        worker = worker,
                        shiftHours = shift.hours,
                        onRemove = { onRemoveWorkerFromShift(shift.id, worker.id) },
                        onUpdate = onUpdateWorkerPayment
                    )
                }
            }
        }
    }
    
    if (showAddWorkerDialog) {
        AddWorkerToShiftDialog(
            workers = filteredWorkers,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onDismiss = { 
                showAddWorkerDialog = false
                searchQuery = ""
            },
            onAddWorker = { workerId, isHourly, payRate ->
                onAddWorkerToShift(shift.id, workerId, isHourly, payRate)
                showAddWorkerDialog = false
                searchQuery = ""
            }
        )
    }
}

@Composable
private fun ShiftWorkerCard(
    shiftWorker: ShiftWorker,
    worker: Worker,
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
                Text(
                    text = "סכום כולל: ${String.format("%.2f", totalPayment)} ש\"ח",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Row {
                TextButton(onClick = { showEditDialog = true }) {
                    Text("ערוך")
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "הסר עובד",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    if (showEditDialog) {
        EditWorkerPaymentDialog(
            shiftWorker = shiftWorker,
            onDismiss = { showEditDialog = false },
            onUpdate = { updated ->
                onUpdate(updated)
                showEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWorkerToShiftDialog(
    workers: List<Worker>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAddWorker: (Long, Boolean, Double) -> Unit
) {
    var selectedWorker by remember { mutableStateOf<Worker?>(null) }
    var isHourlyRate by remember { mutableStateOf(true) }
    var payRate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("הוסף עובד למשמרת") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("חפש עובדים") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(workers) { worker ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { selectedWorker = worker }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
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
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = worker.phoneNumber,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                if (selectedWorker != null) {
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
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedWorker?.let { worker ->
                        val rate = payRate.toDoubleOrNull()
                        if (rate != null && rate > 0) {
                            onAddWorker(worker.id, isHourlyRate, rate)
                        }
                    }
                },
                enabled = selectedWorker != null && 
                         payRate.toDoubleOrNull() != null && 
                         payRate.toDoubleOrNull()!! > 0
            ) {
                Text("הוסף")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ביטול")
            }
        }
    )
}

@Composable
private fun EditWorkerPaymentDialog(
    shiftWorker: ShiftWorker,
    onDismiss: () -> Unit,
    onUpdate: (ShiftWorker) -> Unit
) {
    var isHourlyRate by remember { mutableStateOf(shiftWorker.isHourlyRate) }
    var payRate by remember { mutableStateOf(shiftWorker.payRate.toString()) }

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
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val rate = payRate.toDoubleOrNull()
                    if (rate != null && rate > 0) {
                        onUpdate(
                            shiftWorker.copy(
                                isHourlyRate = isHourlyRate,
                                payRate = rate
                            )
                        )
                    }
                },
                enabled = payRate.toDoubleOrNull() != null && payRate.toDoubleOrNull()!! > 0
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