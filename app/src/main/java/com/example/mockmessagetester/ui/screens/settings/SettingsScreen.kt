package com.example.mockmessagetester.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mockmessagetester.ui.components.settings.SettingActionItem
import com.example.mockmessagetester.ui.components.settings.SettingSectionCard
import com.example.mockmessagetester.ui.components.settings.SettingSwitchItem

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    // 临时状态（UI 跑通后，我们会将其替换为 ViewModel / DataStore 下发的真实配置）
    var isDarkMode by remember { mutableStateOf(false) }
    var isHapticEnabled by remember { mutableStateOf(true) }
    var appendTimestamp by remember { mutableStateOf(true) }
    var autoGroup by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            // 开启垂直滚动，确保设置项增多时能在小屏幕设备上正常滑动
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ==========================================
        // 1. 外观与交互
        // ==========================================
        SettingSectionCard(title = "外观与交互") {
            // 深色模式：使用全局重量级的 PrimarySwitch，并带动态图标
            SettingSwitchItem(
                title = "深色模式",
                subtitle = "强制开启深色主题界面",
                isChecked = isDarkMode,
                onCheckedChange = { isDarkMode = it },
                usePrimarySwitch = true,
                thumbIcon = if (isDarkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode
            )
            // 触觉反馈：常规设置项，使用默认的 SecondarySwitch
            SettingSwitchItem(
                title = "全局触觉反馈",
                subtitle = "转动表盘或点击按钮时的马达震动",
                isChecked = isHapticEnabled,
                onCheckedChange = { isHapticEnabled = it }
            )
        }

        // ==========================================
        // 2. 测试引擎偏好
        // ==========================================
        SettingSectionCard(title = "测试引擎偏好") {
            SettingSwitchItem(
                title = "附加相对时间戳",
                subtitle = "在连发消息尾部追加 @xxx_ms",
                isChecked = appendTimestamp,
                onCheckedChange = { appendTimestamp = it }
            )
            SettingSwitchItem(
                title = "自动折叠通知",
                subtitle = "模拟微信的多条消息合并效果",
                isChecked = autoGroup,
                onCheckedChange = { autoGroup = it }
            )
        }

        // ==========================================
        // 3. 系统操作 (危险区)
        // ==========================================
        SettingSectionCard(title = "系统操作") {
            SettingActionItem(
                title = "一键清空状态栏通知",
                subtitle = "清除由本应用发出的所有测试通知",
                titleColor = MaterialTheme.colorScheme.error,
                onClick = { /* 稍后绑定 dispatcher.clearAll() */ }
            )
        }
    }
}