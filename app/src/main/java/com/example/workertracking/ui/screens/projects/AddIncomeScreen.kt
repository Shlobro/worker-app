package com.example.workertracking.ui.screens.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.workertracking.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeScreen(
    projectId: Long,
    projectName: String,
    onNavigateBack: () -> Unit,
    onSaveIncome: (Long, Date, String, Double, Double) -> Unit
) {
    var selectedDate by remember { mutableStateOf(Date()) }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var units by remember { mutableStateOf("1") }
    
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("הוסף הכנסה ל$projectName") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "פרטי ההכנסה:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = dateFormatter.format(selectedDate),
                onValueChange = { },
                label = { Text("תאריך") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { /* TODO: Date picker */ }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "בחר תאריך"
                        )
                    }
                }
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("תיאור") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("יום עבודה, שבוע 1, חודש ינואר...") }
            )
            
            OutlinedTextField(
                value = units,
                onValueChange = { units = it },
                label = { Text("כמות יחידות") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                supportingText = { Text("למשל: 1 יום, 2 שבועות, 8 שעות") }
            )
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("סכום ליחידה (ש\"ח)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                supportingText = { 
                    val unitsNum = units.toDoubleOrNull() ?: 1.0
                    val amountNum = amount.toDoubleOrNull() ?: 0.0
                    val total = unitsNum * amountNum
                    Text("סה\"כ: ${String.format("%.2f", total)} ש\"ח")
                }
            )
            
            Button(
                onClick = {
                    val unitsNum = units.toDoubleOrNull()
                    val amountNum = amount.toDoubleOrNull()
                    if (unitsNum != null && amountNum != null && 
                        description.isNotBlank() && 
                        unitsNum > 0 && amountNum > 0) {
                        onSaveIncome(projectId, selectedDate, description, amountNum, unitsNum)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = description.isNotBlank() && 
                          units.toDoubleOrNull() != null && 
                          units.toDoubleOrNull()!! > 0 &&
                          amount.toDoubleOrNull() != null && 
                          amount.toDoubleOrNull()!! > 0
            ) {
                Text("שמור הכנסה")
            }
        }
    }
}