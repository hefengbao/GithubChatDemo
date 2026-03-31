package com.hefengbao.kmp.demo.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hefengbao.kmp.demo.calendar.domain.Event
import com.hefengbao.kmp.demo.calendar.repo.EventRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/** Epoch-millis lower bound covering all reasonable calendar events (~292 million years ago). */
private const val QUERY_MIN_TIMESTAMP = Long.MIN_VALUE / 2

/** Epoch-millis upper bound covering all reasonable calendar events (~292 million years ahead). */
private const val QUERY_MAX_TIMESTAMP = Long.MAX_VALUE / 2

/**
 * Minimal calendar screen for validating the end-to-end repository path.
 * Shows all events and provides a button to insert a sample event.
 */
@Composable
fun CalendarScreen(
    repository: EventRepository,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    val events by repository
        .observeInRange(QUERY_MIN_TIMESTAMP, QUERY_MAX_TIMESTAMP)
        .collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "日程管理",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                scope.launch {
                    val now = Clock.System.now().toEpochMilliseconds()
                    repository.upsert(
                        Event(
                            id = "event-$now",
                            title = "示例事件",
                            description = "由 KMP Demo 自动创建",
                            startAtEpochMillis = now,
                            endAtEpochMillis = now + 3_600_000L,
                            createdAtEpochMillis = now,
                            updatedAtEpochMillis = now,
                        )
                    )
                }
            }
        ) {
            Text("新建示例事件")
        }

        Spacer(Modifier.height(8.dp))

        if (events.isEmpty()) {
            Text(
                text = "暂无事件，点击上方按钮新建",
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
                    EventItem(event)
                }
            }
        }
    }
}

@Composable
private fun EventItem(event: Event, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                )
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
}
