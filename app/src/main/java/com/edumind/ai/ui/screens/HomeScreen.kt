package com.edumind.ai.ui.screens

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edumind.ai.AppContainer
import com.edumind.ai.data.StudyTools
import com.edumind.ai.data.SubjectCategories
import com.edumind.ai.ui.components.RobotMascot
import com.edumind.ai.ui.components.SectionTitle
import com.edumind.ai.ui.components.iconForKey
import com.edumind.ai.ui.navigation.Routes
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun HomeScreen(container: AppContainer, navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val streak by container.settingsRepository.dayStreak.collectAsState(initial = 0)
    val xp by container.settingsRepository.xp.collectAsState(initial = 0)
    val questions by container.settingsRepository.questionsAsked.collectAsState(initial = 0)
    val recentChats by container.chatDao.getConversations().collectAsState(initial = emptyList())
    val savedCount by container.savedAnswersDao.getAll().collectAsState(initial = emptyList())
    var question by remember { mutableStateOf("") }
    var rewardMessage by remember { mutableStateOf<String?>(null) }

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RobotMascot(size = 64.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$greeting 👋",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Ready to learn today?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // Streak / XP / questions stats.
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatChip(label = "Day Streak", value = "$streak", modifier = Modifier.weight(1f))
                StatChip(label = "XP", value = "$xp", modifier = Modifier.weight(1f))
                StatChip(label = "Questions", value = "$questions", modifier = Modifier.weight(1f))
            }
        }

        // Search.
        item {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Search subjects, tools, topics…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Routes.SUBJECTS) },
                shape = RoundedCornerShape(16.dp),
            )
        }

        // Large AI question input.
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ask EduMind AI anything",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        placeholder = { Text("Type your question… e.g. Explain photosynthesis") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        minLines = 2,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (question.isNotBlank()) {
                                container.pendingQuestion = question.trim()
                                question = ""
                                navController.navigate(Routes.ASK_AI)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Ask AI") }
                }
            }
        }

        // Camera / Upload / Gallery.
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ActionButton(
                    label = "Scan",
                    icon = Icons.Filled.CameraAlt,
                    onClick = { navController.navigate(Routes.SCANNER) },
                    modifier = Modifier.weight(1f),
                )
                ActionButton(
                    label = "Upload Doc",
                    icon = Icons.Filled.UploadFile,
                    onClick = { navController.navigate(Routes.DOCUMENTS) },
                    modifier = Modifier.weight(1f),
                )
                ActionButton(
                    label = "Gallery",
                    icon = Icons.Filled.Image,
                    onClick = { navController.navigate(Routes.GALLERY) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Popular subjects.
        item {
            SectionTitle("Popular Subjects", "Tap a subject to explore its topics")
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(SubjectCategories.take(8)) { category ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .size(width = 150.dp, height = 110.dp)
                            .clickable {
                                navController.navigate(Routes.subjectDetail(category.id))
                            },
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(
                                imageVector = iconForKey(category.iconKey),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "${category.subcategories.size} topics",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // AI study tools.
        item {
            SectionTitle("AI Study Tools", "20 tools to help you learn faster")
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StudyTools.take(6).chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { tool ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        navController.navigate(Routes.toolDetail(tool.id))
                                    },
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Icon(
                                        imageVector = iconForKey(tool.iconKey),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp),
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = tool.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Button(
                    onClick = { navController.navigate(Routes.TOOLS) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Explore All Tools") }
            }
        }

        // Recent questions.
        if (recentChats.isNotEmpty()) {
            item { SectionTitle("Recent Questions") }
            items(recentChats.take(3)) { conversation ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Routes.CHAT_HISTORY) },
                ) {
                    Text(
                        text = conversation.title,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        // Saved answers count.
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Routes.SAVED) },
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Saved Answers — ${savedCount.size} saved",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // Rewarded ad (voluntary, with a clear explanation and a real benefit).
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7E0)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Earn +50 XP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Watch a short ad if you like — it is completely optional and " +
                            "never required to get answers. You earn 50 XP when it finishes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        val activity = context as? Activity
                        if (activity == null) {
                            rewardMessage = "Could not open the ad right now."
                            return@Button
                        }
                        if (!container.adMobManager.isRewardedReady()) {
                            rewardMessage = "No ad is available right now. Please try again later."
                            container.adMobManager.loadRewarded()
                            return@Button
                        }
                        container.adMobManager.showRewarded(
                            activity = activity,
                            onRewardEarned = {
                                scope.launch {
                                    container.settingsRepository.addXp(50)
                                    rewardMessage = "You earned +50 XP. Thank you!"
                                }
                            },
                            onClosed = { /* Nothing forced — the user chose to watch. */ },
                        )
                    }) { Text("Watch a short ad (+50 XP)") }
                    rewardMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Study tip.
        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Study Tip",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Short, regular study sessions beat last-minute cramming. " +
                            "Ask EduMind AI to quiz you after each topic.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
