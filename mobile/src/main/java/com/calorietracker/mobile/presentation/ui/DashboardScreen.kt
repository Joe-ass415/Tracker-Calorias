package com.calorietracker.mobile.presentation.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorietracker.mobile.presentation.theme.PrimaryGreen
import com.calorietracker.mobile.presentation.ui.components.DailyBudgetCard
import com.calorietracker.mobile.presentation.ui.components.MacroSummaryRow
import com.calorietracker.mobile.presentation.ui.components.MealSectionList
import com.calorietracker.mobile.presentation.ui.components.TargetCaloriesDialog
import com.calorietracker.mobile.presentation.viewmodel.MobileDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MobileDashboardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showTargetDialog by remember { mutableStateOf(false) }
    var manualVoiceInput by remember { mutableStateOf("") }

    LaunchedEffect(uiState.userNotification) {
        uiState.userNotification?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PrimaryGreen
                        )
                        Column {
                            Text(
                                text = "CalorieTrack AI",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Watch,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Wear OS Companion Hub",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                DailyBudgetCard(
                    balance = uiState.dailyBalance,
                    onEditTargetClick = { showTargetDialog = true }
                )
            }

            item {
                MacroSummaryRow(
                    proteinG = uiState.totalProteinG,
                    carbsG = uiState.totalCarbsG,
                    fatG = uiState.totalFatG
                )
            }

            // Barra para registro rápido o prueba de IA (simulador de voz de Wear)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Simulador de Registro por Voz (Reloj/Teléfono):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = manualVoiceInput,
                                onValueChange = { manualVoiceInput = it },
                                placeholder = { Text("Ej: '2 rebanadas de pizza y una soda'...") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = PrimaryGreen
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )

                            if (uiState.isProcessingAi) {
                                Box(
                                    modifier = Modifier.size(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp,
                                        color = PrimaryGreen
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        if (manualVoiceInput.isNotBlank()) {
                                            viewModel.recordMealFromInput(manualVoiceInput)
                                            manualVoiceInput = ""
                                        }
                                    },
                                    enabled = manualVoiceInput.isNotBlank()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Enviar a IA",
                                        tint = if (manualVoiceInput.isNotBlank()) PrimaryGreen else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Historial de comidas por categorías horarias
            item {
                MealSectionList(
                    mealsByType = uiState.mealsByType,
                    onDeleteMeal = { mealId -> viewModel.deleteMeal(mealId) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showTargetDialog) {
        TargetCaloriesDialog(
            currentTarget = uiState.targetCalories,
            onDismiss = { showTargetDialog = false },
            onConfirm = { newTarget ->
                viewModel.updateTargetCalories(newTarget)
                showTargetDialog = false
            }
        )
    }
}

