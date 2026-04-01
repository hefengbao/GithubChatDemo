package com.hefengbao.kmp.demo.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hefengbao.kmp.demo.calendar.domain.Event
import com.hefengbao.kmp.demo.calendar.repo.EventRepository
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.atStartOfDayIn

// ── Navigation state ─────────────────────────────────────────────────────────

private sealed interface Screen {
    data object DayList : Screen
    data class EventDetail(val id: String) : Screen
    data class EventEdit(val id: String?) : Screen  // null means new event
}

// ── Root composable ───────────────────────────────────────────────────────────

/**
 * Calendar feature root: internally routes between DayList, EventDetail and EventEdit screens
 * without requiring an external navigation library.
 */
@Composable
fun CalendarScreen(
    repository: EventRepository,
    modifier: Modifier = Modifier,
) {
    var screen: Screen by remember { mutableStateOf(Screen.DayList) }

    when (val s = screen) {
        is Screen.DayList -> DayListScreen(
            repository = repository,
            onEventClick = { screen = Screen.EventDetail(it) },
            onNewEvent = { screen = Screen.EventEdit(null) },
            modifier = modifier,
        )

        is Screen.EventDetail -> EventDetailScreen(
            repository = repository,
            eventId = s.id,
            onEdit = { screen = Screen.EventEdit(s.id) },
            onDeleted = { screen = Screen.DayList },
            onBack = { screen = Screen.DayList },
            modifier = modifier,
        )

        is Screen.EventEdit -> EventEditScreen(
            repository = repository,
            eventId = s.id,
            onSaved = { screen = Screen.DayList },
            onCancel = { screen = Screen.DayList },
            modifier = modifier,
        )
    }
}

// ── DayListScreen ─────────────────────────────────────────────────────────────

@Composable
private fun DayListScreen(
    repository: EventRepository,
    onEventClick: (String) -> Unit,
    onNewEvent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tz = TimeZone.currentSystemDefault()
    var selectedDate by remember {
        mutableStateOf(Clock.System.now().toLocalDateTime(tz).date)
    }

    val rangeStart = remember(selectedDate) {
        selectedDate.atStartOfDayIn(tz).toEpochMilliseconds()
    }
    val rangeEnd = remember(selectedDate) {
        selectedDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz).toEpochMilliseconds()
    }

    val events by repository.observeInRange(rangeStart, rangeEnd)
        .collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "日程管理",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(Modifier.height(8.dp))

        // Date navigation row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = {
                selectedDate = selectedDate.minus(1, DateTimeUnit.DAY)
            }) {
                Text("< 前一天")
            }
            Text(
                text = selectedDate.toString(),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(onClick = {
                selectedDate = selectedDate.plus(1, DateTimeUnit.DAY)
            }) {
                Text("后一天 >")
            }
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = onNewEvent,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("+ 新建事件")
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        if (events.isEmpty()) {
            Text(
                text = "当日暂无事件，点击上方按钮新建",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(events, key = { it.id }) { event ->
                    EventListItem(event = event, onClick = { onEventClick(event.id) })
                }
            }
        }
    }
}

@Composable
private fun EventListItem(event: Event, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleSmall,
            )
            if (event.allDay) {
                Text(
                    text = "全天",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                val tz = TimeZone.currentSystemDefault()
                Text(
                    text = "${formatMillis(event.startAtEpochMillis, tz)} – ${formatMillis(event.endAtEpochMillis, tz)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (event.description.isNotBlank()) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── EventDetailScreen ─────────────────────────────────────────────────────────

@Composable
private fun EventDetailScreen(
    repository: EventRepository,
    eventId: String,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var event by remember { mutableStateOf<Event?>(null) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(eventId) {
        loading = true
        event = repository.getById(eventId)
        loading = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        TextButton(onClick = onBack) { Text("← 返回列表") }

        Spacer(Modifier.height(8.dp))

        if (loading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        val e = event
        if (e == null) {
            Text("事件不存在或已被删除。", color = MaterialTheme.colorScheme.error)
            return@Column
        }

        val tz = TimeZone.currentSystemDefault()

        Text(text = e.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        if (e.description.isNotBlank()) {
            Text(text = e.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
        }

        if (e.allDay) {
            Text("全天事件", style = MaterialTheme.typography.bodyMedium)
        } else {
            Text(
                text = "开始：${formatMillis(e.startAtEpochMillis, tz)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "结束：${formatMillis(e.endAtEpochMillis, tz)}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onEdit) { Text("编辑") }
            OutlinedButton(
                onClick = {
                    scope.launch {
                        repository.softDelete(eventId)
                        onDeleted()
                    }
                },
            ) {
                Text("删除", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ── EventEditScreen ───────────────────────────────────────────────────────────

@Composable
private fun EventEditScreen(
    repository: EventRepository,
    eventId: String?,  // null = new event
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val tz = TimeZone.currentSystemDefault()

    var originalEvent by remember { mutableStateOf<Event?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startAtStr by remember { mutableStateOf("") }
    var endAtStr by remember { mutableStateOf("") }
    var allDay by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(eventId) {
        if (eventId != null) {
            val e = repository.getById(eventId)
            if (e != null) {
                originalEvent = e
                title = e.title
                description = e.description
                startAtStr = formatMillis(e.startAtEpochMillis, tz)
                endAtStr = formatMillis(e.endAtEpochMillis, tz)
                allDay = e.allDay
            }
        } else {
            val now = Clock.System.now().toLocalDateTime(tz)
            startAtStr = formatLocalDateTime(now)
            val endHour = (now.hour + 1).coerceAtMost(23)
            endAtStr = formatLocalDateTime(
                LocalDateTime(now.year, now.month, now.day, endHour, 0),
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        TextButton(onClick = onCancel) { Text("← 取消") }
        Spacer(Modifier.height(8.dp))

        Text(
            text = if (eventId == null) "新建事件" else "编辑事件",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("标题（必填）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("描述") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
        )
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = allDay, onCheckedChange = { allDay = it })
            Spacer(Modifier.width(4.dp))
            Text("全天事件", style = MaterialTheme.typography.bodyMedium)
        }

        if (!allDay) {
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = startAtStr,
                onValueChange = { startAtStr = it },
                label = { Text("开始时间 (YYYY-MM-DD HH:mm)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = endAtStr,
                onValueChange = { endAtStr = it },
                label = { Text("结束时间 (YYYY-MM-DD HH:mm)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
        }

        if (allDay) {
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = startAtStr.substringBefore(' '),
                onValueChange = { startAtStr = it },
                label = { Text("日期 (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
        }

        errorMsg?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(4.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onCancel) { Text("取消") }
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMsg = "标题不能为空"
                        return@Button
                    }

                    val nowMs = Clock.System.now().toEpochMilliseconds()

                    val startMs: Long
                    val endMs: Long
                    if (allDay) {
                        val date = parseDateOnly(startAtStr) ?: run {
                            errorMsg = "请输入有效的日期 (YYYY-MM-DD)"
                            return@Button
                        }
                        startMs = date.atStartOfDayIn(tz).toEpochMilliseconds()
                        endMs = date.plus(1, DateTimeUnit.DAY)
                            .atStartOfDayIn(tz).toEpochMilliseconds() - 1
                    } else {
                        startMs = parseMillisFromString(startAtStr, tz) ?: run {
                            errorMsg = "开始时间格式错误，请使用 YYYY-MM-DD HH:mm"
                            return@Button
                        }
                        endMs = parseMillisFromString(endAtStr, tz) ?: run {
                            errorMsg = "结束时间格式错误，请使用 YYYY-MM-DD HH:mm"
                            return@Button
                        }
                        if (endMs < startMs) {
                            errorMsg = "结束时间不能早于开始时间"
                            return@Button
                        }
                    }

                    errorMsg = null
                    val id = eventId ?: "event-$nowMs"
                    val createdAt = originalEvent?.createdAtEpochMillis ?: nowMs

                    scope.launch {
                        repository.upsert(
                            Event(
                                id = id,
                                title = title.trim(),
                                description = description.trim(),
                                startAtEpochMillis = startMs,
                                endAtEpochMillis = endMs,
                                allDay = allDay,
                                createdAtEpochMillis = createdAt,
                                updatedAtEpochMillis = nowMs,
                            ),
                        )
                        onSaved()
                    }
                },
            ) {
                Text("保存")
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Format an epoch-millis timestamp as "YYYY-MM-DD HH:mm" in [tz]. */
private fun formatMillis(millis: Long, tz: TimeZone): String {
    val dt = Instant.fromEpochMilliseconds(millis).toLocalDateTime(tz)
    return formatLocalDateTime(dt)
}

/** Format a [LocalDateTime] as "YYYY-MM-DD HH:mm". */
private fun formatLocalDateTime(dt: LocalDateTime): String =
    "${dt.year.pad(4)}-${(dt.month.ordinal + 1).pad()}-${dt.day.pad()} ${dt.hour.pad()}:${dt.minute.pad()}"

/** Parse "YYYY-MM-DD HH:mm" → epoch millis, or null on failure. */
private fun parseMillisFromString(str: String, tz: TimeZone): Long? = try {
    val trimmed = str.trim()
    val spaceIdx = trimmed.indexOf(' ')
    if (spaceIdx < 0) return null
    val datePart = trimmed.substring(0, spaceIdx).split("-")
    val timePart = trimmed.substring(spaceIdx + 1).split(":")
    if (datePart.size < 3 || timePart.size < 2) return null
    LocalDateTime(
        year = datePart[0].toInt(),
        month = Month(datePart[1].toInt()),
        day = datePart[2].toInt(),
        hour = timePart[0].toInt(),
        minute = timePart[1].toInt(),
    ).toInstant(tz).toEpochMilliseconds()
} catch (_: Exception) {
    null
}

/** Parse "YYYY-MM-DD" or the date portion of "YYYY-MM-DD HH:mm" → [LocalDate], or null on failure. */
private fun parseDateOnly(str: String): LocalDate? = try {
    val datePart = str.trim().substringBefore(' ').split("-")
    if (datePart.size < 3) null
    else LocalDate(datePart[0].toInt(), datePart[1].toInt(), datePart[2].toInt())
} catch (_: Exception) {
    null
}

private fun Int.pad(length: Int = 2): String = toString().padStart(length, '0')
