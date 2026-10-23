package com.example.mockmessagetester.ui.screens.mocklab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mockmessagetester.ui.components.panels.mocksenderpanel.MockSenderControlPanel

@Composable
fun MockLabScreen(
    modifier: Modifier = Modifier
) {
    // 直接使用 Column，不再需要 Scaffold 和 innerPadding
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        MockSenderControlPanel(
            modifier = Modifier.fillMaxWidth()
        )
    }
}