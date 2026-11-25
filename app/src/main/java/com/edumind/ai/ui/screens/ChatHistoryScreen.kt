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
import com.edumind.ai.ui.components.SectionTitle
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** AI chat history: past conversations, expandable to read them, deletable. */
@Composable
fun ChatHistoryScreen(container: AppContainer, navController: NavHostController) {
    val scope = rememberCoroutineScope()
    val conversations by container.chatDao.getConversations().collectAsState(initial = emptyList())
    var expandedId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionTitle("AI Chat History", "Your past conversations with EduMind AI")
        Spacer(modifier = Modifier.height(16.dp))
        if (conversations.isEmpty()) {
            Text(
                text = "No conversations yet. Ask EduMind AI a question and it will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(conversations) { conversation ->
                    val messages by container.chatDao
                        .getMessages(conversation.id)
                        .collectAsState(initial = emptyList())
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedId = if (expandedId == conversation.id) null else conversation.id
                            },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = conversation.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
                                            .format(Date(conversation.lastUpdated)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        container.chatDao.deleteConversationMessages(conversation.id)
                                        container.chatDao.deleteConversation(conversation.id)
                                    }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete conversation")
                                }
                            }
                            if (expandedId == conversation.id) {
                                Spacer(modifier = Modifier.height(12.dp))
                                messages.forEach { message ->
                                    Text(
                                        text = (if (message.role == "USER") "You: " else "EduMind AI: ") + message.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 4.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
