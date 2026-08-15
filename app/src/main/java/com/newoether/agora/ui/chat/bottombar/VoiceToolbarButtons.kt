package com.newoether.agora.ui.chat.bottombar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.newoether.agora.R

@Composable
internal fun VoiceToolbarButtons(
    isListening: Boolean,
    onVoiceToggle: () -> Unit,
    onVoiceChatClick: () -> Unit,
) {
    IconButton(onClick = onVoiceToggle, modifier = Modifier.size(32.dp)) {
        Icon(
            if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
            stringResource(R.string.voice_input),
            modifier = Modifier.size(18.dp),
        )
    }
    IconButton(onClick = onVoiceChatClick, modifier = Modifier.size(32.dp)) {
        Icon(
            Icons.Default.VolumeUp,
            stringResource(R.string.voice_chat),
            modifier = Modifier.size(18.dp),
        )
    }
}
