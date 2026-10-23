package com.example.mockmessagetester

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import com.example.mockmessagetester.ui.screens.navigation.MainPagerScreen
import com.example.mockmessagetester.ui.theme.InboxZeroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InboxZeroTheme {

                // 1. 准备一个系统权限请求发射器 (Launcher)
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        // 如果用户拒绝了，这里可以加 Toast 提示。目前保持极简，不做阻断。
                    }
                )

                // 2. 界面加载时 (LaunchedEffect(Unit))，自动检查并申请权限
                LaunchedEffect(Unit) {
                    // POST_NOTIFICATIONS 是 Android 13 (Tiramisu / API 33) 引入的
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                // 直接挂载包含滑动路由的主舞台容器
                MainPagerScreen()
            }
        }
    }
}