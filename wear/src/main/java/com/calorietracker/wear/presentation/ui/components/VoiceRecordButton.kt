package com.calorietracker.wear.presentation.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import com.calorietracker.wear.presentation.theme.WearPrimary

@Composable
fun VoiceRecordButton(
    isSending: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSending) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier.size(68.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSending) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(WearPrimary.copy(alpha = 0.25f))
            )
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                strokeWidth = 3.dp,
                indicatorColor = WearPrimary
            )
        }

        Button(
            onClick = onClick,
            enabled = !isSending,
            modifier = Modifier.size(52.dp),
            colors = ButtonDefaults.primaryButtonColors(
                backgroundColor = WearPrimary,
                contentColor = Color.Black
            )
        ) {
            // Icono de micrófono usando recurso vectorial del sistema o icono
            Icon(
                painter = painterResource(android.R.drawable.ic_btn_speak_now),
                contentDescription = "Grabar comida por voz",
                modifier = Modifier.size(26.dp),
                tint = Color.Black
            )
        }
    }
}

