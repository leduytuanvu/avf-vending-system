package com.avf.vending.feature.admin.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerScreen(
    viewModel: LogViewerViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.entries.size) {
        if (state.entries.isNotEmpty()) listState.animateScrollToItem(state.entries.lastIndex)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Viewer") },
                actions = {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear logs")
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0D0D0D)),
        ) {
            items(state.entries) { entry ->
                Text(
                    text = entry,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                    ),
                    color = logEntryColor(entry),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                )
            }
        }
    }
}

private fun logEntryColor(entry: String): Color = when {
    entry.contains(" E ") || entry.contains("ERROR") -> Color(0xFFFF453A)
    entry.contains(" W ") || entry.contains("WARN") -> Color(0xFFFFD60A)
    entry.contains(" D ") || entry.contains("DEBUG") -> Color(0xFF98C4FF)
    else -> Color(0xFFCCCCCC)
}
