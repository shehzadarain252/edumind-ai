package com.edumind.ai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edumind.ai.data.SubjectCategories
import com.edumind.ai.ui.components.SectionTitle
import com.edumind.ai.ui.viewmodel.PlannerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Study planner: add tasks with subject, date and duration; check them off. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(viewModel: PlannerViewModel, navController: NavHostController) {
    val tasks by viewModel.tasks.collectAsState()
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf(SubjectCategories.first().name) }
    var subjectMenuOpen by remember { mutableStateOf(false) }
    var duration by remember { mutableStateOf(30) }
    var durationMenuOpen by remember { mutableStateOf(false) }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var datePickerOpen by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)

    val dateText = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(Date(dateMillis))

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionTitle("Study Planner", "Plan your study sessions and tick them off")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("What will you study?") },
            placeholder = { Text("e.g. Revise Algebra — Chapter 5") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                OutlinedButton(onClick = { subjectMenuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(subject)
                }
                DropdownMenu(expanded = subjectMenuOpen, onDismissRequest = { subjectMenuOpen = false }) {
                    SubjectCategories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = { subject = category.name; subjectMenuOpen = false },
                        )
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                OutlinedButton(onClick = { durationMenuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("$duration min")
                }
                DropdownMenu(expanded = durationMenuOpen, onDismissRequest = { durationMenuOpen = false }) {
                    listOf(15, 30, 45, 60, 90, 120).forEach { minutes ->
                        DropdownMenuItem(
                            text = { Text("$minutes min") },
                            onClick = { duration = minutes; durationMenuOpen = false },
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = { datePickerOpen = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Date: $dateText")
        }
        if (datePickerOpen) {
            DatePickerDialog(
                onDismissRequest = { datePickerOpen = false },
                confirmButton = {
                    TextButton(onClick = {
                        dateState.selectedDateMillis?.let { dateMillis = it }
                        datePickerOpen = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { datePickerOpen = false }) { Text("Cancel") }
                },
            ) {
                DatePicker(state = dateState)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                viewModel.addTask(title, subject, dateMillis, duration)
                title = ""
            },
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Add Study Task") }

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Your tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        if (tasks.isEmpty()) {
            Text(
                text = "No tasks yet. Add your first study session above.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(tasks) { task ->
                    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = { viewModel.toggleDone(task) }) {
                                Icon(
                                    imageVector = if (task.done) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                                    contentDescription = "Mark done",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (task.done) FontWeight.Normal else FontWeight.SemiBold,
                                )
                                Text(
                                    text = "${task.subject} • ${SimpleDateFormat("EEE, dd MMM", Locale.US).format(Date(task.dateMillis))} • ${task.durationMinutes} min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = { viewModel.deleteTask(task) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete task")
                            }
                        }
                    }
                }
            }
        }
    }
}
