package com.hefengbao.kmp.demo.calendar

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun DateTimeField(
    label: String,
    valueMillis: Long,
    timeZone: TimeZone,
    onValueChange: (Long) -> Unit,
    modifier: Modifier,
) {
    val localDt = remember(valueMillis, timeZone) {
        Instant.fromEpochMilliseconds(valueMillis).toLocalDateTime(timeZone)
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    // Stores the UTC-midnight millis chosen in the date step, to be combined with the time step
    var pickedDateUtcMillis by remember { mutableStateOf<Long?>(null) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) showDatePicker = true
    }

    OutlinedTextField(
        value = formatMillis(valueMillis, timeZone),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        interactionSource = interactionSource,
    )

    if (showDatePicker) {
        // DatePicker works in UTC: convert current local date → UTC midnight millis for initialisation
        val initialUtcDateMillis = remember(valueMillis, timeZone) {
            localDt.date.atStartOfDayIn(kotlinx.datetime.TimeZone.UTC).toEpochMilliseconds()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialUtcDateMillis,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickedDateUtcMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                        showTimePicker = true
                    },
                    enabled = datePickerState.selectedDateMillis != null,
                ) { Text("下一步") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = localDt.hour,
            initialMinute = localDt.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择时间") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val utcMs = pickedDateUtcMillis ?: return@TextButton
                        // Derive the local date from the UTC-midnight millis that DatePicker returned
                        val utcLdt = Instant.fromEpochMilliseconds(utcMs)
                            .toLocalDateTime(kotlinx.datetime.TimeZone.UTC)
                        val newMillis = LocalDateTime(
                            year = utcLdt.year,
                            month = utcLdt.month,
                            day = utcLdt.day,
                            hour = timePickerState.hour,
                            minute = timePickerState.minute,
                        ).toInstant(timeZone).toEpochMilliseconds()
                        onValueChange(newMillis)
                        showTimePicker = false
                    },
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("取消") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun DateField(
    label: String,
    valueMillis: Long,
    timeZone: TimeZone,
    onValueChange: (Long) -> Unit,
    modifier: Modifier,
) {
    val localDt = remember(valueMillis, timeZone) {
        Instant.fromEpochMilliseconds(valueMillis).toLocalDateTime(timeZone)
    }

    var showDatePicker by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) showDatePicker = true
    }

    OutlinedTextField(
        value = "${localDt.year.pad(4)}-${(localDt.month.ordinal + 1).pad()}-${localDt.day.pad()}",
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        interactionSource = interactionSource,
    )

    if (showDatePicker) {
        val initialUtcDateMillis = remember(valueMillis, timeZone) {
            localDt.date.atStartOfDayIn(kotlinx.datetime.TimeZone.UTC).toEpochMilliseconds()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialUtcDateMillis,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val utcMs = datePickerState.selectedDateMillis ?: return@TextButton
                        // Derive the local date and return start-of-day millis in user's timezone
                        val utcLdt = Instant.fromEpochMilliseconds(utcMs)
                            .toLocalDateTime(kotlinx.datetime.TimeZone.UTC)
                        val pickedDate = utcLdt.date
                        onValueChange(pickedDate.atStartOfDayIn(timeZone).toEpochMilliseconds())
                        showDatePicker = false
                    },
                    enabled = datePickerState.selectedDateMillis != null,
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
