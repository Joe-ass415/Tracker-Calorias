package com.calorietracker.mobile.presentation.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun TargetCaloriesDialog(
    currentTarget: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf(currentTarget.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Ajustar Meta Calórica Diaria") },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = {
                    textValue = it
                    val number = it.toIntOrNull()
                    isError = number == null || number <= 0
                },
                label = { Text("Meta en kcal (ej. 2000)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = isError,
                supportingText = {
                    if (isError) {
                        Text("Introduce un valor numérico válido mayor a 0")
                    }
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = textValue.toIntOrNull()
                    if (parsed != null && parsed > 0) {
                        onConfirm(parsed)
                    }
                },
                enabled = !isError && textValue.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

