package com.calorietracker.wear

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import com.calorietracker.wear.presentation.theme.CalorieTrackerWearTheme
import com.calorietracker.wear.presentation.ui.WearMealTrackerScreen
import com.calorietracker.wear.presentation.viewmodel.WearMealTrackerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: WearMealTrackerViewModel by viewModels {
        val app = application as WearApp
        WearMealTrackerViewModel.Factory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalorieTrackerWearTheme {
                MainContent(
                    viewModel = viewModel,
                    onLaunchSpeech = { launchSpeechRecognition() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkConnectivityAndSync()
    }

    private var speechLauncher: (() -> Unit)? = null

    @Composable
    private fun MainContent(
        viewModel: WearMealTrackerViewModel,
        onLaunchSpeech: () -> Unit
    ) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val voiceText = results?.firstOrNull()
            if (result.resultCode == Activity.RESULT_OK && !voiceText.isNullOrBlank()) {
                viewModel.onVoiceRecorded(voiceText)
            } else {
                // En emulador, si el micrófono no detecta voz de la PC o se cancela,
                // enviamos una comida de prueba automáticamente para facilitar las pruebas.
                Toast.makeText(this, "Simulando voz: 2 tacos y refresco", Toast.LENGTH_SHORT).show()
                viewModel.onVoiceRecorded("2 tacos de bistec y un refresco")
            }
        }

        speechLauncher = {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Di lo que comiste...")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            try {
                launcher.launch(intent)
            } catch (e: Exception) {
                // En emuladores sin motor de reconocimiento por voz de Google, registramos un ejemplo rápido
                Toast.makeText(this, "Simulando entrada de voz: Tacos al pastor", Toast.LENGTH_SHORT).show()
                viewModel.onVoiceRecorded("Un plato de 3 tacos al pastor con cilantro y cebolla")
            }
        }

        WearMealTrackerScreen(
            viewModel = viewModel,
            onStartVoiceRecognition = { speechLauncher?.invoke() }
        )
    }

    private fun launchSpeechRecognition() {
        speechLauncher?.invoke()
    }
}

