package com.example.workertracking.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workertracking.R
import com.example.workertracking.data.entity.Event
import com.example.workertracking.data.entity.Project
import com.example.workertracking.ui.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onProjectClick: (Long) -> Unit = {},
    onEventClick: (Long) -> Unit = {},
    onViewAllProjects: () -> Unit = {},
    onViewAllEvents: () -> Unit = {},
    onMoneyOwedClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFilter by viewModel.dateFilter.collectAsState()
    
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                DateFilterChip(
                    startDate = dateFilter.first,
                    endDate = dateFilter.second,
                    onDateRangeSelected = { start, end ->
                        viewModel.setDateFilter(start, end)
                    },
                    onClearFilter = {
                        viewModel.clearDateFilter()
                    }
                )
            }
        }
        
        item {
            FinancialSummaryCard(
                totalIncome = uiState.totalIncome,
                totalExpenses = uiState.totalExpenses,
                netProfit = uiState.netProfit,
                isLoading = uiState.isLoading
            )
        }
        
        item {
            MoneyOwedCard(
                totalOwed = uiState.totalOwed,
                isLoading = uiState.isLoading,
                onClick = onMoneyOwedClick
            )
        }
        
        item {
            ActiveProjectsCard(
                projects = uiState.activeProjects,
                onProjectClick = onProjectClick,
                onViewAll = onViewAllProjects,
                isLoading = uiState.isLoading
            )
        }
        
        item {
            UpcomingEventsCard(
                events = uiState.upcomingEvents,
                onEventClick = onEventClick,
                onViewAll = onViewAllEvents,
                isLoading = uiState.isLoading
            )
        }
    }
}

@Composable
fun FinancialSummaryCard(
    totalIncome: Double,
    totalExpenses: Double,
    netProfit: Double,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.financial_summary),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            
            FinancialSummaryItem(
                label = stringResource(R.string.total_revenue),
                amount = formatCurrency(totalIncome),
                color = MaterialTheme.colorScheme.primary,
                icon = Icons.Default.Add
            )
            
            FinancialSummaryItem(
                label = stringResource(R.string.total_expenses),
                amount = formatCurrency(totalExpenses),
                color = MaterialTheme.colorScheme.error,
                icon = Icons.Default.Delete
            )
            
            HorizontalDivider()
            
            FinancialSummaryItem(
                label = if (netProfit >= 0) stringResource(R.string.net_profit) else stringResource(R.string.net_loss),
                amount = formatCurrency(kotlin.math.abs(netProfit)),
                color = if (netProfit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                icon = if (netProfit >= 0) Icons.Default.Add else Icons.Default.Delete,
                isTotal = true
            )
        }
    }
}

@Composable
fun FinancialSummaryItem(
    label: String,
    amount: String,
    color: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(if (isTotal) 24.dp else 20.dp)
                )
            }
            Text(
                text = label,
                style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
            )
        }
        Text(
            text = amount,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun MoneyOwedCard(
    totalOwed: Double,
    isLoading: Boolean,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = stringResource(R.string.money_owed_tracking),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Text(
                text = formatCurrency(totalOwed),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Text(
                text = stringResource(R.string.total_pending_payments),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun ActiveProjectsCard(
    projects: List<Project>,
    onProjectClick: (Long) -> Unit,
    onViewAll: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.active_projects),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (projects.isNotEmpty()) {
                    TextButton(onClick = onViewAll) {
                        Text(stringResource(R.string.view_all))
                    }
                }
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (projects.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_active_projects),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(projects) { project ->
                        ProjectSummaryCard(
                            project = project,
                            onClick = { onProjectClick(project.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingEventsCard(
    events: List<Event>,
    onEventClick: (Long) -> Unit,
    onViewAll: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = stringResource(R.string.upcoming_events),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (events.isNotEmpty()) {
                    TextButton(onClick = onViewAll) {
                        Text(stringResource(R.string.view_all))
                    }
                }
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (events.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_upcoming_events),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(events) { event ->
                        EventSummaryCard(
                            event = event,
                            onClick = { onEventClick(event.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectSummaryCard(
    project: Project,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = project.location,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.active),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EventSummaryCard(
    event: Event,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }
    
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = dateFormat.format(event.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatCurrency(event.income),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DateFilterChip(
    startDate: Date?,
    endDate: Date?,
    onDateRangeSelected: (Date?, Date?) -> Unit,
    onClearFilter: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yy", Locale.getDefault()) }
    
    val filterText = when {
        startDate != null && endDate != null -> {
            "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
        }
        startDate != null -> {
            "מ- ${dateFormat.format(startDate)}"
        }
        endDate != null -> {
            "עד ${dateFormat.format(endDate)}"
        }
        else -> stringResource(R.string.filter_by_date)
    }
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (startDate != null || endDate != null) {
            IconButton(
                onClick = onClearFilter,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.clear_filter),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        FilterChip(
            onClick = { showDatePicker = true },
            label = {
                Text(
                    text = filterText,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            selected = startDate != null || endDate != null,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        )
    }
    
    // TODO: Implement date range picker dialog
    // This would show a date range picker when showDatePicker is true
}

// Helper function to format currency
private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("he", "IL"))
    format.currency = java.util.Currency.getInstance("ILS")
    return format.format(amount).replace("ILS", "₪")
}