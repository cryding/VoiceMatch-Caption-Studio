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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var selectedVideo by remember { mutableStateOf<Uri?>(null) }
                var transcript by remember { mutableStateOf("Welcome to the video. Add caption text here.") }
                var sampleCaption by remember { mutableStateOf("Welcome to the video.") }
                var style by remember { mutableStateOf("Karaoke") }
                var message by remember { mutableStateOf("Choose a video, then edit captions.") }
                val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    selectedVideo = uri
                    message = if (uri == null) "No video selected." else "Video selected. Transcription is not integrated yet."
                }
                val saveSrt = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/x-subrip")) { uri ->
                    if (uri != null) try {
                        contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(makeSrt(transcript)) }
                        message = "SRT export completed."
                    } catch (e: Exception) { message = "SRT export failed: ${e.message}" }
                }
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("VoiceMatch Caption Studio", style = MaterialTheme.typography.headlineMedium)
                    Text("Offline video-caption editor starter · no account or API key")
                    Button(onClick = { picker.launch(arrayOf("video/*")) }) { Text("Choose video") }
                    Text(selectedVideo?.lastPathSegment ?: "No video selected")
                    OutlinedTextField(transcript, { transcript = it }, label = { Text("Transcript / captions") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
                    Text("Caption style", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(style == "Karaoke", { style = "Karaoke" }, label = { Text("Karaoke") })
                        FilterChip(style == "Classic", { style = "Classic" }, label = { Text("Classic") })
                        FilterChip(style == "Bold", { style = "Bold" }, label = { Text("Bold words") })
                    }
                    OutlinedTextField(sampleCaption, { sampleCaption = it }, label = { Text("Preview caption") }, modifier = Modifier.fillMaxWidth())
                    Surface(color = Color(0xFF202124), shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                        Text(sampleCaption.ifBlank { "Caption preview" }, color = Color.White, modifier = Modifier.padding(18.dp))
                    }
                    Button(onClick = { saveSrt.launch("captions.srt") }) { Text("Export SRT") }
                    Text(message)
                    HorizontalDivider()
                    Text("AI integration status", style = MaterialTheme.typography.titleMedium)
                    Text("Automatic transcription, voice cloning/matching, narration generation, accurate word alignment and burned-in MP4 rendering are not implemented yet. SRT timestamps are estimated from text chunks.")
                    Text("Only reproduce voices you own or have permission to use.")
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
            out.append(index + 1).append('\n')
            out.append(ts(start)).append(" --> ").append(ts(time)).append('\n')
            out.append(chunk.joinToString(" ")).append("\n\n")
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