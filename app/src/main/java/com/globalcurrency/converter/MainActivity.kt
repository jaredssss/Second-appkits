package com.globalcurrency.converter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.globalcurrency.converter.ui.screens.MainScreen
import com.globalcurrency.converter.ui.theme.GlobalCurrencyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlobalCurrencyTheme {
                MainScreen()
            }
        }
    }
}
