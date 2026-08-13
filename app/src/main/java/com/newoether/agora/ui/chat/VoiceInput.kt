package com.newoether.agora.ui.chat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.util.Locale

/** Foreground-only speech input. Audio is consumed by Android recognition and never persisted. */
@Composable
fun rememberVoiceInput(onResult: (String) -> Unit): Pair<Boolean, () -> Unit> {
    val context = LocalContext.current
    var listening by remember { mutableStateOf(false) }
    val latestResult by androidx.compose.runtime.rememberUpdatedState(onResult)
    val recognizer = remember(context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) SpeechRecognizer.createSpeechRecognizer(context) else null
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) recognizer?.startListening(voiceIntent(context)) else listening = false
    }

    DisposableEffect(recognizer) {
        if (recognizer == null) return@DisposableEffect onDispose { }
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: android.os.Bundle) {
                results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }?.let(latestResult)
                listening = false
            }
            override fun onError(error: Int) { listening = false }
            override fun onReadyForSpeech(params: android.os.Bundle?) { listening = true }
            override fun onBeginningOfSpeech() { listening = true }
            override fun onEndOfSpeech() { listening = false }
            override fun onPartialResults(partialResults: android.os.Bundle?) = Unit
            override fun onEvent(eventType: Int, params: android.os.Bundle?) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
        })
        onDispose {
            recognizer.cancel()
            recognizer.destroy()
        }
    }

    val toggle = remember(recognizer, listening) {
        {
            if (recognizer == null) return@remember
            if (listening) {
                recognizer.stopListening()
                listening = false
            } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                recognizer.startListening(voiceIntent(context))
                listening = true
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    return listening to toggle
}

private fun voiceIntent(context: Context) = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
}
