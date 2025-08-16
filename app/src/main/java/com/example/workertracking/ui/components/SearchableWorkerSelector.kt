package com.example.workertracking.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.workertracking.R
import com.example.workertracking.data.entity.Worker

@Composable
fun SearchableWorkerSelector(
    workers: List<Worker>,
    onWorkerSelected: (Worker?) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredWorkers = remember(workers, searchQuery) {
        if (searchQuery.isBlank()) {
            workers
        } else {
            workers.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.reference_worker),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.search_workers)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn {
                    item {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.no_reference)) },
                            modifier = Modifier.clickable {
                                onWorkerSelected(null)
                            }
                        )
                        HorizontalDivider()
                    }
                    
                    items(filteredWorkers) { worker ->
                        ListItem(
                            headlineContent = { Text(worker.name) },
                            supportingContent = { Text(worker.phoneNumber) },
                            modifier = Modifier.clickable {
                                onWorkerSelected(worker)
                            }
                        )
                        HorizontalDivider()
                    }
                    
                    if (filteredWorkers.isEmpty() && searchQuery.isNotBlank()) {
                        item {
                            Text(
                                text = "No workers found",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}