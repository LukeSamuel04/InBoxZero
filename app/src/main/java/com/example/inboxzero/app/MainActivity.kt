package com.example.inboxzero.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 开发者捷径：App一打开，自动跳转到系统的“通知读取权限”授权界面
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))

        // 极简的临时黑板，不依赖任何外部 Theme
        setContent {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("雷达测试中...\n请在弹出的设置里开启权限\n然后去 Android Studio 查看 Logcat")
            }
        }
    }
}