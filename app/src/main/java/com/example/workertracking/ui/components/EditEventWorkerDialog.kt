package com.example.workertracking.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.workertracking.data.entity.EventWorker
import com.example.workertracking.data.entity.Worker
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventWorkerDialog(
    eventWorker: EventWorker,
    worker: Worker,
    referenceWorker: Worker?,
    onDismiss: () -> Unit,
    onConfirm: (EventWorker) -> Unit
) {
    var isHourlyRate by remember { mutableStateOf(eventWorker.isHourlyRate) }
    var payRate by remember { mutableStateOf(eventWorker.payRate.toString()) }
    var referencePayRate by remember { mutableStateOf(eventWorker.referencePayRate?.toString() ?: "") }
    var isReferenceHourlyRate by remember { mutableStateOf(eventWorker.isReferenceHourlyRate) }
    var hours by remember { mutableStateOf(eventWorker.hours.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ערוך פרטי עובד: ${worker.name}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Payment type selection
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
                
                // Hours input
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("מספר שעות") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                // Pay rate input
                OutlinedTextField(
                    value = payRate,
                    onValueChange = { payRate = it },
                    label = {
                        Text(
                            if (!isHourlyRate) "סכום גלובלי (ש\"ח)" 
                            else "שכר שעתי (ש\"ח)"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                // Reference payment field
                if (worker.referenceId != null && referenceWorker != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "תשלום לעובד מפנה: ${referenceWorker.name}",
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
                                if (isReferenceHourlyRate) "שכר שעתי לעובד מפנה (ש\"ח)"
                                else "סכום קבוע לעובד מפנה (ש\"ח)"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
                
                // Total payment calculation
                val rate = payRate.toDoubleOrNull() ?: 0.0
                val refRate = referencePayRate.toDoubleOrNull() ?: 0.0
                val hoursVal = hours.toDoubleOrNull() ?: 0.0

                val workerPayment = if (!isHourlyRate) rate else rate * hoursVal
                val referencePayment = if (isReferenceHourlyRate) refRate * hoursVal else refRate
                val totalPayment = workerPayment + referencePayment
                
                if (rate > 0 && hoursVal > 0) {
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
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val rate = payRate.toDoubleOrNull()
                    val refRate = if (referencePayRate.isNotBlank()) referencePayRate.toDoubleOrNull() else null
                    val hoursVal = hours.toDoubleOrNull()
                    
                    if (rate != null && rate >= 0 && hoursVal != null && hoursVal > 0) {
                        val updatedEventWorker = eventWorker.copy(
                            isHourlyRate = isHourlyRate,
                            payRate = rate,
                            hours = hoursVal,
                            referencePayRate = refRate,
                            isReferenceHourlyRate = isReferenceHourlyRate
                        )
                        onConfirm(updatedEventWorker)
                    }
                },
                enabled = (payRate.toDoubleOrNull() != null && hours.toDoubleOrNull() != null)
            ) {
                Text("שמור")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ביטול")
            }
        }
    )
}
