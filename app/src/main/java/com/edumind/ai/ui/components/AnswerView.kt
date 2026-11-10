package com.edumind.ai.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edumind.ai.util.AnswerFormatter

/**
 * Renders an AI answer in the structured 7-part format with
 * Copy / Share / Regenerate / Save actions.
 */
@Composable
fun AnswerView(
    answerText: String,
    onRegenerate: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onSpeak: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val sections = AnswerFormatter.displaySections(AnswerFormatter.parse(answerText))

    Column(modifier = modifier.fillMaxWidth()) {
        sections.forEach { (title, body) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(onClick = {
                clipboard.setText(AnnotatedString(answerText))
            }) {
                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy answer")
            }
            IconButton(onClick = {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, answerText)
                }
                context.startActivity(Intent.createChooser(send, "Share answer"))
            }) {
                Icon(Icons.Filled.Share, contentDescription = "Share answer")
            }
            if (onRegenerate != null) {
                IconButton(onClick = onRegenerate) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Regenerate answer")
                }
            }
            if (onSave != null) {
                IconButton(onClick = onSave) {
                    Icon(Icons.Filled.Save, contentDescription = "Save answer")
                }
            }
            if (onSpeak != null) {
                IconButton(onClick = onSpeak) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = "Read answer aloud")
                }
            }
        }
    }
}
