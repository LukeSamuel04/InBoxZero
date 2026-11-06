package com.example.inboxzero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.inboxzero.ui.navigation.MainScaffold
import com.example.inboxzero.ui.screens.analytics.AnalyticsScreen
import com.example.inboxzero.ui.screens.inbox.InboxScreen
import com.example.inboxzero.ui.screens.settings.SettingsScreen
import com.example.inboxzero.ui.theme.InboxZeroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userPreferences = (application as InboxZeroApplication).userPreferences

        setContent {
            val systemDark = isSystemInDarkTheme()

            val savedDarkMode by userPreferences.isDarkMode.collectAsState()
            val fontScaleIndex by userPreferences.fontScaleIndex.collectAsState()
            val currentLanguage by userPreferences.language.collectAsState()

            val isDarkModeEffective = savedDarkMode ?: systemDark

            val (fontScale, isBoldText) = when (fontScaleIndex) {
                1 -> 1.2f to true
                2 -> 1.35f to true
                else -> 1.0f to false
            }

            InboxZeroTheme(
                darkTheme = isDarkModeEffective,
                fontScale = fontScale,
                boldText = isBoldText,
                language = currentLanguage
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScaffold(
                        inboxPageContent = {
                            InboxScreen(userPreferences = userPreferences)
                        },
                        analyticsPageContent = {
                            AnalyticsScreen()
                        },
                        settingsPageContent = {
                            SettingsScreen(
                                userPreferences = userPreferences,
                                isDarkModeEffective = isDarkModeEffective
                            )
                        }
                    )
                }
            }
        }
    }
}