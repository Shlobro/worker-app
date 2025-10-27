package com.example.workertracking.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember

import androidx.compose.ui.unit.dp
import com.example.workertracking.ui.theme.Amber40
import com.example.workertracking.ui.theme.Error as ThemeError
import com.example.workertracking.ui.theme.Success
import com.example.workertracking.ui.theme.Warning
import java.text.NumberFormat
import java.util.Locale

// Unified spacing values based on 8dp grid system
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

// Unified elevation values
object Elevation {
    val card = 4.dp
    val elevatedCard = 8.dp
    val floating = 12.dp
}

// Unified corner radius values
object CornerRadius {
    val small = 4.dp
    val medium = 8.dp
    val large = 12.dp
    val xl = 16.dp
}

// Financial amount formatting
@Composable
fun formatCurrency(amount: Double): String {
    val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("he", "IL"))
    return format.format(amount)
}

// Reusable Card Component with consistent styling
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val elevation = if (onClick != null) Elevation.elevatedCard else Elevation.card
    
    if (onClick != null) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(CornerRadius.large),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            onClick = onClick
        ) {
            content()
        }
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(CornerRadius.large),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            content()
        }
    }
}

// Status Chip Component for consistent status indicators
@Composable
fun StatusChip(
    text: String,
    status: StatusType,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (status) {
        StatusType.SUCCESS -> com.example.workertracking.ui.theme.Success.copy(alpha = 0.1f)
        StatusType.WARNING -> com.example.workertracking.ui.theme.Warning.copy(alpha = 0.1f)
        StatusType.ERROR -> ThemeError.copy(alpha = 0.1f)
        StatusType.INFO -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    }
    
    val textColor = when (status) {
        StatusType.SUCCESS -> com.example.workertracking.ui.theme.Success
        StatusType.WARNING -> com.example.workertracking.ui.theme.Warning
        StatusType.ERROR -> ThemeError
        StatusType.INFO -> MaterialTheme.colorScheme.primary
    }
    
    val icon = when (status) {
        StatusType.SUCCESS -> Icons.Filled.CheckCircle
        StatusType.WARNING -> Icons.Filled.Warning
        StatusType.ERROR -> Icons.Filled.Error
        StatusType.INFO -> Icons.Filled.Info
    }
    
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = textColor
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
}

// Financial Display Component with consistent styling
@Composable
fun FinancialDisplay(
    label: String,
    amount: Double,
    modifier: Modifier = Modifier,
    isPositive: Boolean? = null
) {
    val amountColor = when {
        isPositive == true -> Success
        isPositive == false -> ThemeError
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.titleMedium,
            color = amountColor,
            fontWeight = FontWeight.Bold
        )
    }
}

// Empty State Component
@Composable
fun EmptyState(
    title: String,
    description: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = Spacing.md),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(Spacing.sm))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

// Loading Skeleton Component
@Composable
fun LoadingSkeleton(
    modifier: Modifier = Modifier,
    height: Dp = 80.dp
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = 0.3f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 200),
        label = "skeleton_alpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(CornerRadius.large))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = animatedAlpha))
    )
}

// Progress Indicator with consistent styling
@Composable
fun AppProgressIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// Section Header Component
@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(CornerRadius.small))
                    .clickableWithRipple { onActionClick() }
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
            )
        }
    }
}

// Info Row Component for consistent data display
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Enhanced Divider Component
@Composable
fun AppDivider(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    )
}

// Status Type Enum
enum class StatusType {
    SUCCESS, WARNING, ERROR, INFO
}

// Extension for clickable with ripple effect
@Composable
fun Modifier.clickableWithRipple(onClick: () -> Unit): Modifier = this.then(
    clickable(
        interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
        indication = LocalIndication.current,
        onClick = onClick
    )
)
