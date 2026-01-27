package com.example.workertracking.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.workertracking.R

@Composable
fun PaymentDialog(
    totalAmount: Double,
    onConfirm: (isFullPayment: Boolean, amount: Double, tip: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var isFullPayment by remember { mutableStateOf(true) }
    var partialAmount by remember { mutableStateOf("") }
    var tipAmount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("תשלום לעובד") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("סכום לתשלום: ₪${String.format("%.2f", totalAmount)}")

                // Payment Type Selection
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = isFullPayment,
                        onClick = { 
                            isFullPayment = true 
                            error = null
                        }
                    )
                    Text("תשלום מלא")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = !isFullPayment,
                        onClick = { isFullPayment = false }
                    )
                    Text("תשלום חלקי")
                }

                if (!isFullPayment) {
                    OutlinedTextField(
                        value = partialAmount,
                        onValueChange = { 
                            partialAmount = it
                            error = null
                        },
                        label = { Text("סכום לתשלום") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = error != null
                    )
                }

                OutlinedTextField(
                    value = tipAmount,
                    onValueChange = { tipAmount = it },
                    label = { Text("טיפ (אופציונלי)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tip = tipAmount.toDoubleOrNull() ?: 0.0
                    if (isFullPayment) {
                        onConfirm(true, totalAmount, tip)
                    } else {
                        val amount = partialAmount.toDoubleOrNull()
                        if (amount == null || amount <= 0) {
                            error = "נא להזין סכום תקין"
                        } else if (amount > totalAmount) {
                            error = "הסכום לא יכול להיות גבוה מהיתרה"
                        } else {
                            onConfirm(false, amount, tip)
                        }
                    }
                }
            ) {
                Text("אישור")
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
fun EditPaymentDialog(
    currentPaidAmount: Double,
    currentTipAmount: Double,
    totalDue: Double,
    isPaid: Boolean,
    onConfirm: (isPaid: Boolean, amount: Double, tip: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf(currentPaidAmount.toString()) }
    var tip by remember { mutableStateOf(currentTipAmount.toString()) }
    var markAsPaid by remember { mutableStateOf(isPaid) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Track if user manually toggled the checkbox to avoid overriding their explicit choice excessively
    // However, for simplicity and to solve the user's specific issue ("editing amount to less"), 
    // we will prioritize the amount logic when amount changes.
    
    LaunchedEffect(amount) {
        val newAmount = amount.toDoubleOrNull()
        if (newAmount != null) {
            if (newAmount < totalDue - 0.01) { // Tolerance for float comparison
                markAsPaid = false
            } else if (newAmount >= totalDue - 0.01) {
                markAsPaid = true
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("עריכת תשלום") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("סכום לתשלום מלא: ₪${String.format("%.2f", totalDue)}")

                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it 
                        error = null
                    },
                    label = { Text("סכום ששולם") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tip,
                    onValueChange = { tip = it },
                    label = { Text("טיפ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = markAsPaid,
                        onCheckedChange = { markAsPaid = it }
                    )
                    Text("סמן כשולם במלואו")
                }

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newAmount = amount.toDoubleOrNull()
                    val newTip = tip.toDoubleOrNull() ?: 0.0
                    
                    if (newAmount == null || newAmount < 0) {
                        error = "נא להזין סכום תקין"
                    } else if (newAmount > totalDue && !markAsPaid) { 
                        // Allow paying more? Maybe, but user said check for higher amount. 
                        // "user does not input a higher amount then what needs to be payed"
                         error = "הסכום לא יכול להיות גבוה מהיתרה"
                    } else {
                        onConfirm(markAsPaid, newAmount, newTip)
                    }
                }
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
