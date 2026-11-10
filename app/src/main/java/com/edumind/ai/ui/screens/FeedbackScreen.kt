package com.edumind.ai.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edumind.ai.AppContainer
import com.edumind.ai.data.local.MessageEntity
import com.edumind.ai.ui.components.SectionTitle
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Feedback form: validated, then stored in the on-device outbox.
 * The app ships without a support email address (no fake contact details),
 * so messages are stored locally and can be sent once the owner configures
 * a support channel (see README.md).
 */
@Composable
fun FeedbackScreen(container: AppContainer, navController: NavHostController) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    fun isEmailValid(value: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        SectionTitle("Feedback", "Tell us what works and what could be better")
        Spacer(modifier = Modifier.height(16.dp))
        if (success) {
            Text(
                text = "Thank you! Your feedback has been saved on this device. " +
                    "It will be sent once a support channel is configured.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }
            return
        }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Your email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Your feedback") },
            placeholder = { Text("What did you like? What should improve?") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            minLines = 5,
        )
        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                when {
                    name.isBlank() -> error = "Please enter your name."
                    !isEmailValid(email) -> error = "Please enter a valid email address."
                    message.trim().length < 10 -> error = "Please write at least a few words of feedback."
                    else -> {
                        error = null
                        scope.launch {
                            container.messagesDao.insert(
                                MessageEntity(
                                    id = UUID.randomUUID().toString(),
                                    name = name.trim(),
                                    email = email.trim(),
                                    subject = "Feedback",
                                    message = message.trim(),
                                    type = "feedback",
                                    timestamp = System.currentTimeMillis(),
                                ),
                            )
                            success = true
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Send Feedback") }
    }
}
