package com.avf.vending.feature.admin.diagnostics

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.avf.vending.ui.components.BadgeStatus
import com.avf.vending.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    viewModel: DiagnosticsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Diagnostics") }) },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Motor Test Grid", style = MaterialTheme.typography.titleMedium)
            MotorTestGrid(
                rows = state.rows,
                cols = state.cols,
                testResults = state.testResults,
                onTest = { row, col -> viewModel.testMotor(row, col) },
            )
            Divider()
            Text("Hardware Status", style = MaterialTheme.typography.titleMedium)
            state.hardwareStatus.forEach { (name, ok) ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(name, style = MaterialTheme.typography.bodyMedium)
                    StatusBadge(
                        label = if (ok) "OK" else "FAIL",
                        status = if (ok) BadgeStatus.OK else BadgeStatus.ERROR,
                    )
                }
            }
        }
    }
}

@Composable
fun MotorTestGrid(
    rows: Int,
    cols: Int,
    testResults: Map<Pair<Int, Int>, Boolean?>,
    onTest: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(cols),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.heightIn(max = 320.dp),
    ) {
        items(rows * cols) { index ->
            val row = index / cols + 1
            val col = index % cols + 1
            val result = testResults[row to col]
            val borderColor = when (result) {
                true -> MaterialTheme.colorScheme.primary
                false -> MaterialTheme.colorScheme.error
                null -> MaterialTheme.colorScheme.surfaceVariant
            }
            FilledTonalButton(
                onClick = { onTest(row, col) },
                modifier = Modifier
                    .aspectRatio(1f)
                    .border(2.dp, borderColor, MaterialTheme.shapes.small),
                contentPadding = PaddingValues(4.dp),
            ) {
                Text("$row-$col", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
