package com.example.workertracking.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.workertracking.R
import java.util.*

/**
 * A modern time picker dialog using Material Design 3 TimePicker component.
 * Provides an intuitive clock-based interface for selecting hours and minutes.
 *
 * @param initialHour Initial hour (0-23)
 * @param initialMinute Initial minute (0-59)
 * @param onTimeSelected Callback when time is confirmed, receives hour and minute
 * @param onDismiss Callback when dialog is dismissed
 * @param title Optional title for the dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int = 8,
    initialMinute: Int = 0,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
    title: String = "בחר שעה"
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(timePickerState.hour, timePickerState.minute)
            }) {
                Text(stringResource(R.string.select))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    )
}

/**
 * Extension function to parse time string in format "HH:mm" to hour and minute components
 */
fun parseTimeString(timeString: String): Pair<Int, Int>? {
    return try {
        if (timeString.contains(":")) {
            val parts = timeString.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                if (hour in 0..23 && minute in 0..59) {
                    Pair(hour, minute)
                } else null
            } else null
        } else null
    } catch (e: Exception) {
        null
    }
}

/**
 * Extension function to format hour and minute to "HH:mm" string
 */
fun formatTime(hour: Int, minute: Int): String {
    return String.format(Locale.US, "%02d:%02d", hour, minute)
}
