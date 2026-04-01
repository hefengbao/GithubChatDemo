package com.hefengbao.kmp.demo.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.TimeZone

/**
 * A field that lets the user pick a date and time.
 * Android: opens Material3 DatePickerDialog followed by a TimePicker dialog.
 * Other platforms: falls back to an editable text field (YYYY-MM-DD HH:mm).
 */
@Composable
expect fun DateTimeField(
    label: String,
    valueMillis: Long,
    timeZone: TimeZone,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
)

/**
 * A field that lets the user pick a date only (for all-day events).
 * Android: opens Material3 DatePickerDialog.
 * Other platforms: falls back to an editable text field (YYYY-MM-DD).
 */
@Composable
expect fun DateField(
    label: String,
    valueMillis: Long,
    timeZone: TimeZone,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
)
