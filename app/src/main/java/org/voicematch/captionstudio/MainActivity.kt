package org.voicematch.captionstudio

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.Locale

private val CanvasInk = Color(0xFF10131B)
private val CanvasPanel = Color(0xFF1C2230)
private val CaptionViolet = Color(0xFFB9A7FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colors = darkColorScheme(
                primary = CaptionViolet,
                onPrimary = CanvasInk,
                background = CanvasInk,
                surface = CanvasPanel,
                onSurface = Color(0xFFF4F2FA),
                secondary = Color(0xFF8DDCC6)
            )
            MaterialTheme(colorScheme = colors) {
                var selectedVideo by remember { mutableStateOf<Uri?>(null) }
                var transcript by remember { mutableStateOf("Welcome to the video. Add caption text here.") }
                var sampleCaption by remember { mutableStateOf("Welcome to the video.") }
                var style by remember { mutableStateOf("Karaoke") }
                var message by remember { mutableStateOf("Choose a video to get started.") }
                val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    selectedVideo = uri
                    message = if (uri == null) "Video selection cancelled." else "Video selected. Captions can be edited below."
                }
                val saveSrt = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/x-subrip")) { uri ->
                    if (uri != null) try {
                        contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(makeSrt(transcript)) }
                            ?: error("Could not open the selected save location")
                        message = "SRT captions saved."
                    } catch (e: Exception) {
                        message = "SRT export failed: ${e.localizedMessage ?: "storage error"}"
                    } else message = "SRT export cancelled."
                }
                Scaffold(containerColor = CanvasInk) { insets ->
                    Column(
                        Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("VOICEMATCH", style = MaterialTheme.typography.labelLarge, color = CaptionViolet)
                            Text("Caption Studio", style = MaterialTheme.typography.headlineLarge)
                            Text("Shape your words. Style your story.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .72f))
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = CanvasPanel), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("01  ·  Video source", style = MaterialTheme.typography.titleLarge)
                                Button(onClick = { picker.launch(arrayOf("video/*")) }, modifier = Modifier.fillMaxWidth()) {
                                    Text(if (selectedVideo == null) "＋  Choose a video" else "Change video")
                                }
                                Text(selectedVideo?.lastPathSegment ?: "No video selected", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .72f))
                            }
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = CanvasPanel), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("02  ·  Caption text", style = MaterialTheme.typography.titleLarge)
                                OutlinedTextField(
                                    value = transcript, onValueChange = { transcript = it },
                                    label = { Text("Transcript / captions") }, modifier = Modifier.fillMaxWidth(), minLines = 4
                                )
                                OutlinedTextField(
                                    value = sampleCaption, onValueChange = { sampleCaption = it },
                                    label = { Text("Preview text") }, modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = CanvasPanel), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("03  ·  Caption look", style = MaterialTheme.typography.titleLarge)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Karaoke", "Classic", "Bold").forEach { option ->
                                        FilterChip(selected = style == option, onClick = { style = option }, label = { Text(option) })
                                    }
                                }
                                Surface(
                                    color = when (style) {
                                        "Karaoke" -> Color(0xFF513D88)
                                        "Bold" -> Color(0xFFFFD54F)
                                        else -> Color(0xFF242A38)
                                    },
                                    shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(Modifier.padding(18.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            sampleCaption.ifBlank { "Your caption preview" },
                                            color = if (style == "Bold") Color(0xFF171717) else Color.White,
                                            style = if (style == "Bold") MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                                Button(onClick = { saveSrt.launch("captions.srt") }, modifier = Modifier.fillMaxWidth(), enabled = transcript.isNotBlank()) {
                                    Text("↓  Export SRT captions")
                                }
                            }
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = CanvasPanel), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Project status", style = MaterialTheme.typography.titleMedium)
                                Text(message)
                                Text("Automatic speech recognition, voice cloning/matching, word-accurate timing and rendered MP4 export are not implemented yet. SRT timing is estimated from text chunks.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .72f))
                                Text("Only use voices you own or have permission to use.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun makeSrt(text: String): String {
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return ""
        val out = StringBuilder()
        var time = 0L
        words.chunked(6).forEachIndexed { index, chunk ->
            val start = time
            time += maxOf(1200L, chunk.size * 350L)
            out.append(index + 1).append('\\n')
            out.append(ts(start)).append(" --> ").append(ts(time)).append('\\n')
            out.append(chunk.joinToString(" ")).append("\\n\\n")
        }
        return out.toString()
    }

    private fun ts(ms: Long): String {
        val h = ms / 3600000
        val m = (ms % 3600000) / 60000
        val s = (ms % 60000) / 1000
        val milli = ms % 1000
        return String.format(Locale.US, "%02d:%02d:%02d,%03d", h, m, s, milli)
    }
}