package com.example.cbstartup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.cbstartup.core.di.AppContainer
import com.example.cbstartup.presentation.TrainingViewModel
import com.example.cbstartup.presentation.ui.TrainingApp
import com.example.cbstartup.ui.theme.CBstartupTheme

class MainActivity : ComponentActivity() {
    private val appContainer by lazy { AppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = ViewModelProvider(
            this,
            TrainingViewModel.Factory(appContainer)
        )[TrainingViewModel::class.java]

        setContent {
            CBstartupTheme(darkTheme = false) {
                TrainingApp(viewModel = viewModel)
            }
        }
    }
}
