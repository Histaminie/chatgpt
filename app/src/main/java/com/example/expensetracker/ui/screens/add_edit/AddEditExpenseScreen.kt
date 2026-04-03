package com.example.expensetracker.ui.screens.add_edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.viewmodel.ExpenseViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(viewModel: ExpenseViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var selectedTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showDateDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Add Expense")
        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())

        ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
            OutlinedTextField(
                value = state.categories.firstOrNull { it.id == categoryId }?.name ?: "Select category",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }
            )
            ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                state.categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            categoryId = category.id
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showDateDialog = true }) { Text("Pick Date") }
            Button(onClick = { showTimeDialog = true }) { Text("Pick Time") }
        }

        Text("Selected: ${selectedTime.toDisplayDateTime()}")

        Button(
            onClick = {
                val parsed = amount.toDoubleOrNull() ?: 0.0
                val selectedCategory = categoryId ?: state.categories.firstOrNull()?.id ?: return@Button
                viewModel.addOrUpdateExpense(null, parsed, selectedCategory, selectedTime, notes)
                amount = ""
                notes = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save Expense") }

        Text("Manage Categories")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = newCategoryName, onValueChange = { newCategoryName = it }, label = { Text("New category") }, modifier = Modifier.weight(1f))
            Button(onClick = {
                if (newCategoryName.isNotBlank()) {
                    viewModel.addCategory(newCategoryName, "#29B6F6")
                    newCategoryName = ""
                }
            }) { Text("Add") }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(state.categories) { category ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(category.name)
                    IconButton(onClick = { viewModel.deleteCategory(category) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete category")
                    }
                }
            }
        }
    }

    if (showDateDialog) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = selectedTime)
        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val old = Instant.ofEpochMilli(selectedTime).atZone(ZoneId.systemDefault()).toLocalDateTime()
                        val newDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        selectedTime = LocalDateTime.of(newDate, old.toLocalTime()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    }
                    showDateDialog = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = pickerState) }
    }

    if (showTimeDialog) {
        val current = Instant.ofEpochMilli(selectedTime).atZone(ZoneId.systemDefault()).toLocalTime()
        val timeState = rememberTimePickerState(initialHour = current.hour, initialMinute = current.minute)
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val old = Instant.ofEpochMilli(selectedTime).atZone(ZoneId.systemDefault()).toLocalDate()
                    selectedTime = LocalDateTime.of(old, java.time.LocalTime.of(timeState.hour, timeState.minute))
                        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    showTimeDialog = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimeDialog = false }) { Text("Cancel") } },
            text = { TimeInput(state = timeState) }
        )
    }
}

private fun Long.toDisplayDateTime(): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
