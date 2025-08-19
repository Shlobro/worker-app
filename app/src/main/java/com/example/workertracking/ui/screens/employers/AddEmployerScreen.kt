package com.example.workertracking.ui.screens.employers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.unit.dp
import com.example.workertracking.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmployerScreen(
    onNavigateBack: () -> Unit,
    onSaveEmployer: (String, String) -> Unit
) {
    var employerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var phoneNumberError by remember { mutableStateOf<String?>(null) }
    
    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.length == 10 && phone.all { it.isDigit() }
    }
    
    class PhoneNumberVisualTransformation : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            val digitsOnly = text.text.filter { it.isDigit() }.take(10)
            val formatted = when {
                digitsOnly.length <= 3 -> digitsOnly
                else -> "${digitsOnly.substring(0, 3)}-${digitsOnly.substring(3)}"
            }
            
            val offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    val digitsBeforeOffset = text.text.take(offset).count { it.isDigit() }
                    return when {
                        digitsBeforeOffset <= 3 -> digitsBeforeOffset
                        else -> digitsBeforeOffset + 1 // +1 for the dash
                    }
                }
                
                override fun transformedToOriginal(offset: Int): Int {
                    return when {
                        offset <= 3 -> offset
                        offset == 4 -> 3 // dash position maps to end of first 3 digits
                        else -> offset - 1 // account for the dash
                    }
                }
            }
            
            return TransformedText(AnnotatedString(formatted), offsetMapping)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_employer)) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = employerName,
                onValueChange = { employerName = it },
                label = { Text(stringResource(R.string.employer_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { input ->
                    val digitsOnly = input.filter { it.isDigit() }.take(10)
                    phoneNumber = digitsOnly
                    phoneNumberError = if (digitsOnly.isNotEmpty() && digitsOnly.length != 10) {
                        "פורמט מספר טלפון: xxx-xxxxxxx"
                    } else {
                        null
                    }
                },
                label = { Text(stringResource(R.string.phone_number)) },
                placeholder = { Text("0501234567") },
                visualTransformation = PhoneNumberVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneNumberError != null,
                supportingText = phoneNumberError?.let { { Text(it) } }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (employerName.isNotBlank() && phoneNumber.isNotBlank() && isValidPhoneNumber(phoneNumber)) {
                        val formattedPhone = "${phoneNumber.substring(0, 3)}-${phoneNumber.substring(3)}"
                        onSaveEmployer(employerName, formattedPhone)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = employerName.isNotBlank() && phoneNumber.isNotBlank() && isValidPhoneNumber(phoneNumber) && phoneNumberError == null
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}