package com.calorietracker.wear.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.calorietracker.wear.presentation.theme.WearError
import com.calorietracker.wear.presentation.theme.WearPrimary
import com.calorietracker.wear.presentation.theme.WearWarning
import com.calorietracker.wear.presentation.ui.components.CalorieProgressDisplay
import com.calorietracker.wear.presentation.ui.components.VoiceRecordButton
import com.calorietracker.wear.presentation.viewmodel.WearMealTrackerViewModel

@Composable
fun WearMealTrackerScreen(
    viewModel: WearMealTrackerViewModel,
    onStartVoiceRecognition: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val balance = uiState.balance

    Scaffold(
        modifier = modifier.fillMaxSize(),
        timeText = { TimeText() }
    ) {
        CalorieProgressDisplay(balance = balance) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Cabecera: Calorías consumidas vs meta
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Text(
                        text = "${balance.consumedCalories}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (balance.isLimitExceeded) WearError else WearPrimary
                    )
                    Text(
                        text = "de ${balance.targetCalories} kcal",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Botón central flotante de micrófono
                VoiceRecordButton(
                    isSending = uiState.isSending,
                    onClick = onStartVoiceRecognition
                )

                // Sección inferior: Estado, restante o aviso offline
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    if (uiState.pendingOfflineCount > 0) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(WearWarning.copy(alpha = 0.25f))
                                .clickable { viewModel.retryOfflineQueue() }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📦 ${uiState.pendingOfflineCount} offline (reintentar)",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WearWarning
                            )
                        }
                    } else if (balance.isLimitExceeded) {
                        Text(
                            text = "Límite excedido",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = WearError
                        )
                    } else {
                        Text(
                            text = "Restan: ${balance.remainingCalories} kcal",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    if (balance.lastMealName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Última: ${balance.lastMealName} (${balance.lastMealCalories} kcal)",
                            fontSize = 8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )
                    }
                }
            }
        }
    }
}

