package com.example.expensetracker.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.viewmodel.DateFilter
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.example.expensetracker.viewmodel.SortOrder

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(viewModel: ExpenseViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var sortExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Dashboard", fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateFilter.entries.forEach { filter ->
                AssistChip(
                    onClick = { viewModel.setDateFilter(filter) },
                    label = { Text(filter.name) }
                )
            }
        }

        ExposedDropdownMenuBox(expanded = sortExpanded, onExpandedChange = { sortExpanded = it }) {
            OutlinedTextField(
                readOnly = true,
                value = state.sortOrder.name,
                onValueChange = {},
                label = { Text("Sort") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                SortOrder.entries.forEach {
                    DropdownMenuItem(
                        text = { Text(it.name) },
                        onClick = {
                            viewModel.setSortOrder(it)
                            sortExpanded = false
                        }
                    )
                }
            }
        }

        SummaryCard("Total Spending", "$${"%.2f".format(state.summary.total)}")
        SummaryCard("Average Daily", "$${"%.2f".format(state.summary.averageDaily)}")

        state.summary.categoryTotals.forEach { (category, total) ->
            SummaryCard(category, "$${"%.2f".format(total)}")
        }

        PieSpendingChart(state.summary.categoryTotals)
        BarSpendingChart(state.trend.map { it.date.dayOfMonth.toString() to it.amount })
        LineTrendChart(state.trend.mapIndexed { index, point -> index.toFloat() to point.amount.toFloat() })
    }
}

@Composable
private fun SummaryCard(title: String, value: String) {
    Card(colors = CardDefaults.cardColors(), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title)
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}
