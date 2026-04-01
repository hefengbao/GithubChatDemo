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
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

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
    onBack: (() -> Unit)? = null,
) {
    var screen: Screen by remember { mutableStateOf(Screen.DayList) }

    when (val s = screen) {
        is Screen.DayList -> DayListScreen(
            repository = repository,
            onEventClick = { screen = Screen.EventDetail(it) },
            onNewEvent = { screen = Screen.EventEdit(null) },
            onBack = onBack,
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
    onBack: (() -> Unit)? = null,
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
        if (onBack != null) {
            TextButton(onClick = onBack) { Text("← 返回首页") }
            Spacer(Modifier.height(4.dp))
        }
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
    var allDay by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Use epoch millis directly so picker composables can round-trip without string parsing
    val nowMs = remember { Clock.System.now().toEpochMilliseconds() }
    var startAtMillis by remember { mutableStateOf(nowMs) }
    var endAtMillis by remember { mutableStateOf(nowMs + 3_600_000L) }  // default +1 hour

    LaunchedEffect(eventId) {
        if (eventId != null) {
            val e = repository.getById(eventId)
            if (e != null) {
                originalEvent = e
                title = e.title
                description = e.description
                startAtMillis = e.startAtEpochMillis
                endAtMillis = e.endAtEpochMillis
                allDay = e.allDay
            }
        }
        // For new events the remember-defaults above are already correct
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
        Spacer(Modifier.height(4.dp))

        if (allDay) {
            // All-day events only need a date selection
            DateField(
                label = "日期",
                valueMillis = startAtMillis,
                timeZone = tz,
                onValueChange = { startAtMillis = it },
            )
        } else {
            // Non-all-day events need a date + time for both start and end
            DateTimeField(
                label = "开始时间",
                valueMillis = startAtMillis,
                timeZone = tz,
                onValueChange = { startAtMillis = it },
            )
            Spacer(Modifier.height(8.dp))
            DateTimeField(
                label = "结束时间",
                valueMillis = endAtMillis,
                timeZone = tz,
                onValueChange = { endAtMillis = it },
            )
        }
        Spacer(Modifier.height(8.dp))

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

                    val saveMs = Clock.System.now().toEpochMilliseconds()

                    val startMs: Long
                    val endMs: Long
                    if (allDay) {
                        // Derive the selected local date and compute day-range boundaries
                        val date = Instant.fromEpochMilliseconds(startAtMillis)
                            .toLocalDateTime(tz).date
                        startMs = date.atStartOfDayIn(tz).toEpochMilliseconds()
                        endMs = date.plus(1, DateTimeUnit.DAY)
                            .atStartOfDayIn(tz).toEpochMilliseconds() - 1
                    } else {
                        startMs = startAtMillis
                        endMs = endAtMillis
                        if (endMs < startMs) {
                            errorMsg = "结束时间不能早于开始时间"
                            return@Button
                        }
                    }

                    errorMsg = null
                    val id = eventId ?: "event-$saveMs"
                    val createdAt = originalEvent?.createdAtEpochMillis ?: saveMs

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
                                updatedAtEpochMillis = saveMs,
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
