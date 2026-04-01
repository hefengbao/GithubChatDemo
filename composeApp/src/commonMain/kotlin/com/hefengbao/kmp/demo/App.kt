package com.hefengbao.kmp.demo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hefengbao.kmp.demo.calendar.CalendarScreen
import com.hefengbao.kmp.demo.calendar.repo.EventRepository
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

import kmpdemo.composeapp.generated.resources.Res
import kmpdemo.composeapp.generated.resources.compose_multiplatform

private object Routes {
    const val HOME = "home"
    const val CALENDAR = "calendar"
}

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onNavigateToCalendar = { navController.navigate(Routes.CALENDAR) },
                )
            }
            composable(Routes.CALENDAR) {
                val repository: EventRepository = koinInject()
                CalendarScreen(
                    repository = repository,
                    onBack = { navController.navigateUp() },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    onNavigateToCalendar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showGreeting by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = { showGreeting = !showGreeting }) {
            Text("Click me!")
        }
        if (showGreeting) {
            val greeting = remember { Greeting().greet() }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(painterResource(Res.drawable.compose_multiplatform), null)
                Text("Compose: $greeting")
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onNavigateToCalendar) {
            Text("打开日程管理")
        }
    }
}
