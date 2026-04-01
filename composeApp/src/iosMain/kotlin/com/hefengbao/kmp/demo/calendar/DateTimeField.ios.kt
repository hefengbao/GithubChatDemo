package com.hefengbao.kmp.demo.calendar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/** iOS fallback: editable text field that parses "YYYY-MM-DD HH:mm". */
@Composable
actual fun DateTimeField(
    label: String,
    valueMillis: Long,
    timeZone: TimeZone,
    onValueChange: (Long) -> Unit,
    modifier: Modifier,
) {
    var text by remember(valueMillis) { mutableStateOf(formatMillis(valueMillis, timeZone)) }
    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            text = input
            parseMillisFromString(input, timeZone)?.let(onValueChange)
        },
        label = { Text("$label (YYYY-MM-DD HH:mm)") },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
    )
}

/** iOS fallback: editable text field that parses "YYYY-MM-DD". */
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
    var text by remember(valueMillis) {
        mutableStateOf(
            "${localDt.year.pad(4)}-${(localDt.month.ordinal + 1).pad()}-${localDt.day.pad()}"
        )
    }
    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            text = input
            parseDateOnly(input)?.let { date ->
                onValueChange(date.atStartOfDayIn(timeZone).toEpochMilliseconds())
            }
        },
        label = { Text("$label (YYYY-MM-DD)") },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
    )
}
