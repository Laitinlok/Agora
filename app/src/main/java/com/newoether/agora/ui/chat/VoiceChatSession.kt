package com.newoether.agora.ui.chat

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.newoether.agora.R
import java.util.Locale

@Composable
fun VoiceChatSession(
    isListening: Boolean,
    isGenerating: Boolean,
    latestAssistantText: String,
    onToggleListening: () -> Unit,
    onClose: () -> Unit,
    onPlaybackFinished: () -> Unit = {},
) {
    val context = LocalContext.current
    var ttsReady by remember { mutableStateOf(false) }
    var spokenText by remember { mutableStateOf(latestAssistantText) }
    val playbackFinished by rememberUpdatedState(onPlaybackFinished)
    var showSettings by remember { mutableStateOf(false) }
    var selectedVoiceName by remember { mutableStateOf<String?>(null) }
    var speechRate by remember { mutableStateOf(1f) }
    var speechPitch by remember { mutableStateOf(1f) }
    val tts = remember(context) {
        TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
    }
    val installedVoices = remember(ttsReady) {
        tts.voices.orEmpty()
            .filterNot { it.isNetworkConnectionRequired }
            .sortedBy { it.locale.displayName }
    }
    val selectedVoice = installedVoices.firstOrNull { it.name == selectedVoiceName }

    DisposableEffect(tts) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    LaunchedEffect(ttsReady) {
        if (ttsReady) tts.language = Locale.getDefault()
    }

    LaunchedEffect(ttsReady, selectedVoice, speechRate, speechPitch) {
        if (ttsReady) {
            selectedVoice?.let(tts::setVoice) ?: tts.setLanguage(Locale.getDefault())
            tts.setSpeechRate(speechRate)
            tts.setPitch(speechPitch)
        }
    }

    LaunchedEffect(isGenerating, latestAssistantText, ttsReady) {
        if (!isGenerating && ttsReady &&
            latestAssistantText.isNotBlank() &&
            latestAssistantText != spokenText
        ) {
            spokenText = latestAssistantText
            tts.speak(
                latestAssistantText,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "agora_voice_reply",
            )
            playbackFinished()
        }
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            IconButton(
                onClick = { showSettings = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = stringResource(R.string.voice_settings),
                )
            }

            DropdownMenu(
                expanded = showSettings,
                onDismissRequest = { showSettings = false },
                modifier = Modifier.widthIn(min = 280.dp, max = 360.dp),
            ) {
                Text(
                    stringResource(R.string.voice_picker_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.voice_automatic)) },
                    onClick = { selectedVoiceName = null },
                )
                installedVoices.take(12).forEach { voice ->
                    DropdownMenuItem(
                        text = { Text("${voice.locale.displayName} - ${voice.name}") },
                        onClick = { selectedVoiceName = voice.name },
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.voice_rate)) },
                    onClick = {},
                    trailingIcon = {
                        Slider(
                            value = speechRate,
                            onValueChange = { speechRate = it },
                            valueRange = 0.5f..2f,
                            modifier = Modifier.width(120.dp),
                        )
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.voice_pitch)) },
                    onClick = {},
                    trailingIcon = {
                        Slider(
                            value = speechPitch,
                            onValueChange = { speechPitch = it },
                            valueRange = 0.5f..2f,
                            modifier = Modifier.width(120.dp),
                        )
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.voice_test)) },
                    onClick = {
                        showSettings = false
                        tts.speak(
                            context.getString(R.string.voice_test_phrase),
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "agora_voice_test",
                        )
                    },
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.voice_close),
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(176.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp),
                    )
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    text = when {
                        isGenerating -> stringResource(R.string.voice_thinking)
                        isListening -> stringResource(R.string.voice_listening)
                        else -> stringResource(R.string.voice_tap_to_speak)
                    },
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(Modifier.height(20.dp))

                IconButton(
                    onClick = onToggleListening,
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            if (isListening) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                            CircleShape,
                        ),
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = stringResource(R.string.voice_toggle),
                        tint = if (isListening) MaterialTheme.colorScheme.onError
                        else MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}
