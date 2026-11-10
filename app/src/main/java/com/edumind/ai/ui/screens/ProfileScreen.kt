package com.edumind.ai.ui.screens

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edumind.ai.AppContainer
import com.edumind.ai.ads.ConsentManager
import com.edumind.ai.ui.components.RobotMascot
import com.edumind.ai.ui.components.SectionTitle
import com.edumind.ai.ui.navigation.Routes
import com.edumind.ai.ui.theme.ThemeMode
import com.edumind.ai.ui.viewmodel.SettingsViewModel

/**
 * Profile / Settings: guest profile, stats, theme, navigation to every
 * info page, ad choices, and a working "delete my data" action.
 * No mandatory login — this is a guest profile.
 */
@Composable
fun ProfileScreen(
    container: AppContainer,
    viewModel: SettingsViewModel,
    navController: NavHostController,
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val streak by viewModel.dayStreak.collectAsState()
    val xp by viewModel.xp.collectAsState()
    val questions by viewModel.questionsAsked.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RobotMascot(size = 72.dp)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = "Guest Learner",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "No account needed — your data stays on this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp)) {
                StatItem(label = "Day Streak", value = "$streak", modifier = Modifier.weight(1f))
                StatItem(label = "XP", value = "$xp", modifier = Modifier.weight(1f))
                StatItem(label = "Questions", value = "$questions", modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle("Appearance")
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            listOf(ThemeMode.SYSTEM to "System", ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark")
                .forEach { (mode, label) ->
                    val selected = themeMode == mode
                    OutlinedButton(
                        onClick = { viewModel.setThemeMode(mode) },
                        modifier = Modifier.padding(end = 8.dp),
                    ) { Text(if (selected) "✓ $label" else label) }
                }
        }
        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle("Your Learning")
        Spacer(modifier = Modifier.height(8.dp))
        MenuRow("Saved Answers") { navController.navigate(Routes.SAVED) }
        MenuRow("Favorites") { navController.navigate(Routes.FAVORITES) }
        MenuRow("AI Chat History") { navController.navigate(Routes.CHAT_HISTORY) }
        MenuRow("Study Planner") { navController.navigate(Routes.PLANNER) }

        Spacer(modifier = Modifier.height(20.dp))
        SectionTitle("Support & Legal")
        Spacer(modifier = Modifier.height(8.dp))
        MenuRow("Help and Support") { navController.navigate(Routes.HELP) }
        MenuRow("Feedback") { navController.navigate(Routes.FEEDBACK) }
        MenuRow("Privacy Policy") { navController.navigate(Routes.PRIVACY) }
        MenuRow("Terms and Conditions") { navController.navigate(Routes.TERMS) }
        MenuRow("Disclaimer") { navController.navigate(Routes.DISCLAIMER) }
        MenuRow("About Us") { navController.navigate(Routes.ABOUT) }
        MenuRow("Contact Us") { navController.navigate(Routes.CONTACT) }
        MenuRow("FAQ") { navController.navigate(Routes.FAQ) }
        MenuRow("AI Usage Guidelines") { navController.navigate(Routes.GUIDELINES) }

        Spacer(modifier = Modifier.height(20.dp))
        SectionTitle("Privacy")
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                val activity = context as? Activity
                if (activity != null) ConsentManager.showFormIfRequired(activity)
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Ad Choices / Privacy Choices") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Delete My Data") }
        deleteMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "EduMind AI v1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete my data?") },
            text = {
                Text(
                    "This permanently deletes your saved answers, chat history, " +
                        "planner tasks and settings from this device. This cannot be undone.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteAllMyData {
                        deleteMessage = "Your local data has been deleted."
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MenuRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        androidx.compose.material3.Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
