package com.example.workertracking.ui.screens.projects

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.workertracking.data.entity.Shift
import com.example.workertracking.data.entity.ProjectStatus
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProjectDetailScreen(
    project: Project?,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onAddShift: () -> Unit,
    modifier: Modifier = Modifier,
    shifts: List<Shift> = emptyList(),
    totalIncome: Double = 0.0,
    totalPayments: Double = 0.0,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEditProject: () -> Unit = {},
    onDeleteProject: () -> Unit = {},
    onShiftClick: (Long) -> Unit = {},
    onDeleteShift: (Shift) -> Unit = {},
    onAddIncome: () -> Unit = {},
    onIncomeHistoryClick: () -> Unit = {},
    onCloseProject: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var shiftToDelete by remember { mutableStateOf<Shift?>(null) }
    var showCloseDialog by remember { mutableStateOf(false) }
    var showDeleteProjectDialog by remember { mutableStateOf(false) }
    val locale = Locale.getDefault()
    val moneyFormatter = remember(locale) {
        NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }
    
    val filteredShifts = remember(shifts, searchQuery) {
        if (searchQuery.isBlank()) {
            shifts
        } else {
            // TODO: Filter shifts by date or worker name when available
            shifts
        }
    }

    Scaffold(
        modifier = modifier,
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
                },
                actions = {
                    if (project != null) {
                        IconButton(onClick = onEditProject) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_project)
                            )
                        }
                        IconButton(onClick = { showDeleteProjectDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete_project),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                                        text = "סטטוס",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = when (project.status) {
                                            ProjectStatus.ACTIVE -> "פעיל"
                                            ProjectStatus.CLOSED -> "סגור"
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = when (project.status) {
                                            ProjectStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                                            ProjectStatus.CLOSED -> MaterialTheme.colorScheme.error
                                        }
                                    )
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
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
                                
                                if (project.endDate != null) {
                                    Column {
                                        Text(
                                            text = "תאריך סיום",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = DateFormat.getDateInstance().format(project.endDate),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Financial Summary Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onIncomeHistoryClick() }
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
                                Column {
                                    Text(
                                        text = "סיכום כספי",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "לחץ לצפייה בהיסטוריית הכנסות",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = onAddIncome,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("הוסף הכנסה")
                                    }
                                    if (project.status == ProjectStatus.ACTIVE) {
                                        Button(
                                            onClick = { showCloseDialog = true },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("סגור פרויקט")
                                        }
                                    }
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "סה\"כ הכנסות",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${moneyFormatter.format(totalIncome)} ש\"ח",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Column {
                                    Text(
                                        text = "סה\"כ תשלומים",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${moneyFormatter.format(totalPayments)} ש\"ח",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                
                                Column {
                                    Text(
                                        text = "רווח נקי",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val profit = totalIncome - totalPayments
                                    Text(
                                        text = "${moneyFormatter.format(profit)} ש\"ח",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (profit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
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
                        label = { Text("חפש משמרות") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "משמרות",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Button(onClick = onAddShift) {
                            Text("הוסף משמרת")
                        }
                    }
                }
                
                items(filteredShifts) { shift ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onShiftClick(shift.id) },
                                onLongClick = { shiftToDelete = shift }
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (shift.name.isNotBlank()) {
                                Text(
                                    text = shift.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = DateFormat.getDateInstance().format(shift.date),
                                    style = if (shift.name.isNotBlank()) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
                                    fontWeight = if (shift.name.isNotBlank()) FontWeight.Normal else FontWeight.Bold
                                )
                                Text(
                                    text = "${shift.startTime} (${shift.hours}h)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Text(
                                text = "לחץ לצפייה בפרטי התשלום",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                if (filteredShifts.isEmpty() && shifts.isNotEmpty() && searchQuery.isNotBlank()) {
                    item {
                        Text(
                            text = stringResource(R.string.no_data),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                if (shifts.isEmpty()) {
                    item {
                        Text(
                            text = "אין משמרות עדיין",
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
    
    // Delete confirmation dialog
    shiftToDelete?.let { shift ->
        AlertDialog(
            onDismissRequest = { shiftToDelete = null },
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
                        onDeleteShift(shift)
                        shiftToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.confirm_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { shiftToDelete = null }) {
                    Text(stringResource(R.string.cancel_delete))
                }
            }
        )
    }
    
    // Close project confirmation dialog
    if (showCloseDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text("סגירת פרויקט") },
            text = { 
                Text(
                    "האם אתה בטוח שברצונך לסגור את הפרויקט? לא ניתן יהיה לבטל פעולה זו.",
                    color = MaterialTheme.colorScheme.error
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCloseProject()
                        showCloseDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("סגור פרויקט")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDialog = false }) {
                    Text("ביטול")
                }
            }
        )
    }
    
    // Delete project confirmation dialog
    if (showDeleteProjectDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteProjectDialog = false },
            title = { Text(stringResource(R.string.delete_confirmation_title)) },
            text = { 
                Text(
                    stringResource(R.string.delete_project_message, project?.name ?: ""),
                    color = MaterialTheme.colorScheme.error
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProject()
                        showDeleteProjectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.confirm_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteProjectDialog = false }) {
                    Text(stringResource(R.string.cancel_delete))
                }
            }
        )
    }
}
