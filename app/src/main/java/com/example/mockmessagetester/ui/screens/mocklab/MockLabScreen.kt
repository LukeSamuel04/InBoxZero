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
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                // 开启垂直滚动，防止表盘或长剧本列表导致内容溢出屏幕
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 直接挂载我们刚刚写好的控制面板包装层
            MockSenderControlPanel(
                modifier = Modifier.fillMaxWidth()
            )

            // 未来如果有其他图表或日志面板 (LogPanel)，也可以按顺序挂载在下面
        }
    }
}