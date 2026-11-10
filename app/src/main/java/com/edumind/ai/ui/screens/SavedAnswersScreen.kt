package com.edumind.ai.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edumind.ai.AppContainer
import com.edumind.ai.ui.components.AnswerView
import com.edumind.ai.ui.components.SectionTitle
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Saved answers: tap to read the full answer, star to favorite, delete to remove. */
@Composable
fun SavedAnswersScreen(container: AppContainer, navController: NavHostController) {
    val scope = rememberCoroutineScope()
    val answers by container.savedAnswersDao.getAll().collectAsState(initial = emptyList())
    var expandedId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionTitle("Saved Answers", "Answers you saved from Ask AI, tools, scanner and documents")
        Spacer(modifier = Modifier.height(16.dp))
        if (answers.isEmpty()) {
            Text(
                text = "Nothing saved yet. Save an answer from Ask AI or any tool and it will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(answers) { answer ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedId = if (expandedId == answer.id) null else answer.id
                            },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = answer.question.ifBlank { "Saved answer" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = listOfNotNull(
                                            answer.subject,
                                            answer.source,
                                            SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(answer.timestamp)),
                                        ).joinToString(" • "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        container.savedAnswersDao.update(
                                            answer.copy(isFavorite = !answer.isFavorite),
                                        )
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (answer.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                        contentDescription = "Favorite",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                IconButton(onClick = {
                                    scope.launch { container.savedAnswersDao.delete(answer) }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                }
                            }
                            if (expandedId == answer.id) {
                                Spacer(modifier = Modifier.height(12.dp))
                                AnswerView(answerText = answer.answer)
                            }
                        }
                    }
                }
            }
        }
    }
}
