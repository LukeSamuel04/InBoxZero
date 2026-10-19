package com.example.inboxzero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.inboxzero.ui.components.panels.mocksenderpanel.MockSenderControlPanel
import com.example.inboxzero.ui.theme.InboxZeroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 挂载 Compose UI 画布
        setContent {
            // 应用你项目的主题颜色
            InboxZeroTheme {
                // 使用 Scaffold 提供标准的屏幕结构 (自带背景色和安全区适配)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // 挂载我们刚刚写好的控制台！
                    MockSenderControlPanel(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}