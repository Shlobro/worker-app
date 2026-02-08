package com.example.workertracking.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.workertracking.data.entity.Worker
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkerDialog(
    workers: List<Worker>,
    allWorkers: List<Worker>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAddWorker: (Long, Boolean, Double, Double?, Boolean) -> Unit, // workerId, isHourly, payRate, refPayRate, isRefHourly
    title: String = "הוסף עובד",
    showPaymentType: Boolean = true, // For shifts, false for events (always hourly)
    showHours: Boolean = false, // For events that need custom hours
    eventHours: Double? = null, // Event hours to use if not showing hours input
    onAddWorkerWithHours: ((Long, Double, Double, Double?, Boolean) -> Unit)? = null // For events with hours (workerId, hours, payRate, refPayRate, isRefHourly)
) {
    var selectedWorker by remember { mutableStateOf<Worker?>(null) }
    var isHourlyRate by remember { mutableStateOf(true) }
    var payRate by remember { mutableStateOf("") }
    var referencePayRate by remember { mutableStateOf("") }
    var isReferenceHourlyRate by remember { mutableStateOf(true) }
    var hours by remember { mutableStateOf("") }
    val workersById = remember(allWorkers) { allWorkers.associateBy { it.id } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            val worker = selectedWorker
            if (worker == null) {
                // Phase 1: Worker selection
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = { Text("חפש " +
                                "עובדים") },
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
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
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
                                        worker.referenceId?.let { referenceId ->
                                            val referenceWorker = workersById[referenceId]
                                            referenceWorker?.let { ref ->
                                                Text(
                                                    text = "עובד מפנה: ${ref.name}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Phase 2: Payment configuration (scrollable)
                Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Selected worker card with change option
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = worker.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    worker.referenceId?.let { referenceId ->
                                        val referenceWorker = workersById[referenceId]
                                        referenceWorker?.let { ref ->
                                            Text(
                                                text = "עובד מפנה: ${ref.name}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                                IconButton(onClick = {
                                    referencePayRate = ""
                                    isReferenceHourlyRate = true
                                }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "שנה עובד",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        // Payment type selection (only for shifts)
                        if (showPaymentType) {
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
                        }

                        // Hours input (only for events with showHours = true)
                        if (showHours) {
                            OutlinedTextField(
                                value = hours,
                                onValueChange = { hours = it },
                                label = { Text("מספר שעות") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                        } else if (eventHours != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "שעות האירוע:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$eventHours שעות",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Pay rate input
                        OutlinedTextField(
                            value = payRate,
                            onValueChange = { payRate = it },
                            label = {
                                Text(
                                    if (showPaymentType && !isHourlyRate) "סכום גלובלי (ש\"ח)"
                                    else "שכר שעתי (ש\"ח)"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )

                        // Reference payment fields
                        worker.referenceId?.let { referenceId ->
                            val referenceWorker = workersById[referenceId]
                            referenceWorker?.let { refWorker ->
                                HorizontalDivider()
                                Text(
                                    text = "תשלום לעובד מפנה: ${refWorker.name}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        onClick = { isReferenceHourlyRate = true },
                                        label = { Text("שכר שעתי") },
                                        selected = isReferenceHourlyRate
                                    )
                                    FilterChip(
                                        onClick = { isReferenceHourlyRate = false },
                                        label = { Text("סכום קבוע") },
                                        selected = !isReferenceHourlyRate
                                    )
                                }
                                OutlinedTextField(
                                    value = referencePayRate,
                                    onValueChange = { referencePayRate = it },
                                    label = {
                                        Text(
                                            if (isReferenceHourlyRate) "שכר " +
                                                    "שעתי לעובד " +
                                                    "מפנה (ש\"ח)"
                                            else "" +
                                                    "סכום קבוע לעובד מפנה (ש\"ח)"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                            }
                        }

                        // Total payment calculation
                        val rate = payRate.toDoubleOrNull() ?: 0.0
                        val refRate = if (worker.referenceId != null && referencePayRate.isNotBlank()) {
                            referencePayRate.toDoubleOrNull() ?: 0.0
                        } else 0.0

                        val hoursToUse = if (showHours) {
                            hours.toDoubleOrNull() ?: 0.0
                        } else {
                            eventHours ?: 0.0
                        }

                        val workerPayment = if (showPaymentType && !isHourlyRate) {
                            rate
                        } else {
                            rate * hoursToUse
                        }
                        val referencePayment = if (isReferenceHourlyRate) {
                            refRate * hoursToUse
                        } else {
                            refRate
                        }
                        val totalPayment = workerPayment + referencePayment

                        if (rate > 0 && hoursToUse > 0) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "סה\"כ תשלום:",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "${String.format(Locale.getDefault(), "%.2f", totalPayment)} ש\"ח",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                }
            }
        },
        confirmButton = {
            val basicValidation1 = selectedWorker != null &&
                    payRate.toDoubleOrNull() != null &&
                    payRate.toDoubleOrNull()!! > 0 &&
                    (selectedWorker?.referenceId == null ||
                            (referencePayRate.isNotBlank() && referencePayRate.toDoubleOrNull() != null))
            TextButton(
                onClick = {
                    selectedWorker?.let { worker ->
                        val rate = payRate.toDoubleOrNull()
                        val refRate =
                            if (worker.referenceId != null && referencePayRate.isNotBlank()) {
                                referencePayRate.toDoubleOrNull()
                            } else null

                        if (rate != null && rate > 0 &&
                            (worker.referenceId == null || refRate != null)
                        ) {

                            if (showHours && onAddWorkerWithHours != null) {
                                // Event with custom hours
                                val workerHours = hours.toDoubleOrNull()
                                if (workerHours != null && workerHours > 0) {
                                    onAddWorkerWithHours(
                                        worker.id,
                                        workerHours,
                                        rate,
                                        refRate,
                                        isReferenceHourlyRate
                                    )
                                }
                            } else if (!showHours && eventHours != null && onAddWorkerWithHours != null) {
                                // Event with predefined hours
                                onAddWorkerWithHours(
                                    worker.id,
                                    eventHours,
                                    rate,
                                    refRate,
                                    isReferenceHourlyRate
                                )
                            } else {
                                // Shift
                                onAddWorker(
                                    worker.id,
                                    isHourlyRate,
                                    rate,
                                    refRate,
                                    isReferenceHourlyRate
                                )
                            }
                        }
                    }
                },
                enabled = if (showHours) {
                    basicValidation1 && hours.toDoubleOrNull() != null && hours.toDoubleOrNull()!! > 0
                } else {
                    basicValidation1
                }
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