package com.example.workertracking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.workertracking.ui.theme.*
import com.example.workertracking.ui.components.StatusType
import com.example.workertracking.ui.components.Spacing
import com.example.workertracking.ui.components.FinancialDisplay
import java.text.NumberFormat
import java.util.*

/**
 * Standard card with consistent styling across the app - Now uses AppCard from SharedComponents
 */
@Composable
fun StandardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    // Use the new AppCard component for consistency
    if (onClick != null) {
        AppCard(
            modifier = modifier,
            onClick = onClick
        ) {
            Column(
            modifier = Modifier.padding(Spacing.md),
                content = content
            )
        }
    } else {
        AppCard(
            modifier = modifier
        ) {
            Column(
                modifier = Modifier.padding(Spacing.md),
                content = content
            )
        }
    }
}

/**
 * Worker card with consistent styling
 */
@Composable
fun WorkerCard(
    workerName: String,
    phoneNumber: String,
    totalOwed: Double = 0.0,
    referenceWorkerName: String? = null,
    isReferencePayment: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onPhoneClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    StandardCard(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isReferencePayment) 
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Worker name with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isReferencePayment) Icons.Default.Star else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isReferencePayment) Amber40 else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = workerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Phone number
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { onPhoneClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = phoneNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Reference worker info if applicable
                referenceWorkerName?.let { refName ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "מפנה: $refName",
                        style = MaterialTheme.typography.bodySmall,
                        color = Amber40
                    )
                }

                // Total owed if > 0
                if (totalOwed > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "חוב: ${formatCurrency(totalOwed)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = actions
            )
        }
    }
}

/**
 * Project card with consistent styling
 */
@Composable
fun ProjectCard(
    projectName: String,
    location: String,
    status: String,
    totalIncome: Double = 0.0,
    totalExpenses: Double = 0.0,
    profit: Double = totalIncome - totalExpenses,
    startDate: String? = null,
    endDate: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    StandardCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Project name
                Text(
                    text = projectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Location
                if (location.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Status chip
                StatusChip(
                    text = status,
                    status = when (status) {
                        "פעיל" -> StatusType.SUCCESS
                        "הושלם" -> StatusType.INFO
                        "מושהה" -> StatusType.WARNING
                        else -> StatusType.INFO
                    }
                )

                // Financial summary
                Spacer(modifier = Modifier.height(8.dp))
                FinancialSummary(
                    income = totalIncome,
                    expenses = totalExpenses,
                    profit = profit,
                    compact = true
                )
            }

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = actions
            )
        }
    }
}

/**
 * Event card with consistent styling
 */
@Composable
fun EventCard(
    eventName: String,
    date: String,
    startTime: String? = null,
    endTime: String? = null,
    hours: Double? = null,
    totalCost: Double = 0.0,
    paymentAmount: Double = 0.0,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    StandardCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Event name
                Text(
                    text = eventName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Time range if available
                if (startTime != null && endTime != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$startTime - $endTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Hours if available
                hours?.let { h ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "שעות: ${String.format("%.1f", h)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Financial info
                if (totalCost > 0 || paymentAmount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (totalCost > 0) {
                            Text(
                                text = "עלות: ${formatCurrency(totalCost)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Error
                            )
                        }
                        if (paymentAmount > 0) {
                            Text(
                                text = "הכנסה: ${formatCurrency(paymentAmount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Success
                            )
                        }
                    }
                }
            }

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = actions
            )
        }
    }
}
@Composable
fun StatusChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Financial summary component
 */
@Composable
fun FinancialSummary(
    income: Double,
    expenses: Double,
    profit: Double,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textStyle = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium

    Column(modifier = modifier) {
        if (!compact) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "הכנסה:",
                    style = textStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatCurrency(income),
                    style = textStyle,
                    color = Success,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "הוצאות:",
                    style = textStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatCurrency(expenses),
                    style = textStyle,
                    color = Error,
                    fontWeight = FontWeight.Medium
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (compact) "רווח:" else "רווח נטו:",
                style = textStyle,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatCurrency(profit),
                style = textStyle,
                color = if (profit >= 0) Success else Error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}