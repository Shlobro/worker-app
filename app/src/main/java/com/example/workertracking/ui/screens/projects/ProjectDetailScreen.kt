package com.example.workertracking.ui.screens.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workertracking.R
import com.example.workertracking.data.entity.Project
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.data.entity.Shift
import com.example.workertracking.data.entity.IncomeType
import java.text.DateFormat
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    project: Project?,
    workers: List<Worker> = emptyList(),
    shifts: List<Shift> = emptyList(),
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredWorkers = remember(workers, searchQuery) {
        if (searchQuery.isBlank()) {
            workers
        } else {
            workers.filter { worker ->
                worker.name.contains(searchQuery, ignoreCase = true) ||
                worker.phoneNumber.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    val filteredShifts = remember(shifts, searchQuery) {
        if (searchQuery.isBlank()) {
            shifts
        } else {
            shifts.filter { shift ->
                // TODO: Filter shifts when available
                true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(project?.name ?: stringResource(R.string.projects_title))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (project != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = project.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.project_location),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = project.location,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                
                                Column {
                                    Text(
                                        text = stringResource(R.string.start_date),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = DateFormat.getDateInstance().format(project.startDate),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                            
                            HorizontalDivider()
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.income_type),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = when (project.incomeType) {
                                            IncomeType.DAILY -> "יומי"
                                            IncomeType.WEEKLY -> "שבועי"
                                            IncomeType.HOURLY -> "שעתי"
                                            IncomeType.MONTHLY -> "חודשי"
                                            IncomeType.FIXED -> "קבוע"
                                        },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                
                                Column {
                                    Text(
                                        text = stringResource(R.string.amount),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = "${NumberFormat.getCurrencyInstance(Locale("he", "IL")).format(project.incomeAmount)}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
                
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text(stringResource(R.string.search_workers)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                item {
                    Text(
                        text = stringResource(R.string.workers_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(filteredWorkers) { worker ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
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
                        }
                    }
                }
                
                if (filteredWorkers.isEmpty() && searchQuery.isNotBlank()) {
                    item {
                        Text(
                            text = stringResource(R.string.no_data),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Project not found",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}