package com.example.inboxzero.ui.components.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// 导入我们之前手搓的所有极简原子组件
import com.example.inboxzero.ui.components.buttons.PrimaryButton
import com.example.inboxzero.ui.components.buttons.SecondaryButton
import com.example.inboxzero.ui.components.buttons.TextButton
import com.example.inboxzero.ui.components.containers.PrimaryContainer
import com.example.inboxzero.ui.components.dropdowns.PrimaryDropdown
import com.example.inboxzero.ui.components.dropdowns.SecondaryDropdown
import com.example.inboxzero.ui.components.textfields.PrimaryTextField
import com.example.inboxzero.ui.theme.InboxZeroTheme

/**
 * 定义控制台的三种核心工作模式
 */
enum class MockMode(val label: String) {
    MANUAL("单发模式 (Manual)"),
    CONTINUOUS("连发模式 (Auto-Send)"),
    SCRIPTED("剧本模式 (Scripted)")
}

/**
 * InboxZero 模拟消息发送控制台 (分子级业务组件)
 *
 * 核心设计规则 (HCI)：
 * 1. 渐进式明示 (Progressive Disclosure)：通过顶部的 PrimaryDropdown 总闸切换模式，任何时刻只展示当前模式所需的最小 UI 集合，杜绝视觉超载。
 * 2. 状态自治与安全隔离：当用户切换模式时，自动终止正在运行的连发或剧本任务，防止后台幽灵任务引发焦虑或 Bug。
 * 3. 完美融合原子规范：使用原生浮动标签保持紧凑布局；使用 SecondaryDropdown 嵌入文本流调节时间；使用三级按钮严格映射操作权重。
 *
 * @param onSendMessage 当触发消息发送时的回调函数（参数为发送的消息文本）
 * @param mockCorpus 剧本模式下使用的预设语料库列表
 */
@Composable
fun MockSenderControlPanel(
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    mockCorpus: List<String> = listOf(
        "【系统通知】您的验证码是 8921，请勿泄露。",
        "【快递提醒】您的包裹已存放至丰巢快递柜，取件码 12-3456。",
        "老板：今天下午 3 点开个紧急碰头会，收到请回复。",
        "信用卡中心：您尾号 8888 的卡片最新账单已生成。",
        "妈妈：周末回家吃饭吗？做了你爱吃的红烧肉。"
    )
) {
    // ==========================================
    // 状态管理区 (State Management)
    // ==========================================
    val modeOptions = MockMode.entries.map { it.label }
    var currentMode by remember { mutableStateOf(MockMode.MANUAL) }

    // --- 模式 1：单发状态 ---
    var manualText by remember { mutableStateOf("这是一条测试消息") }

    // --- 模式 2：连发状态 ---
    var isContinuousRunning by remember { mutableStateOf(false) }
    var continuousCounter by remember { mutableIntStateOf(1) }
    val intervalOptions = listOf("500 ms", "1.0 s", "2.0 s", "3.0 s")
    var selectedInterval by remember { mutableStateOf(intervalOptions[1]) } // 默认 1.0 s

    // --- 模式 3：剧本状态 ---
    var isScriptRunning by remember { mutableStateOf(false) }
    var scriptIndex by remember { mutableIntStateOf(0) }

    // ==========================================
    // 自动化协程引擎 (Coroutine Timers)
    // ==========================================

    // 解析间隔时间字符串为毫秒数
    val intervalMillis = remember(selectedInterval) {
        when (selectedInterval) {
            "500 ms" -> 500L
            "1.0 s" -> 1000L
            "2.0 s" -> 2000L
            "3.0 s" -> 3000L
            else -> 1000L
        }
    }

    // 引擎 A：处理【连发模式】的定时发送
    LaunchedEffect(isContinuousRunning, intervalMillis, currentMode) {
        if (isContinuousRunning && currentMode == MockMode.CONTINUOUS) {
            while (true) {
                onSendMessage("测试消息 $continuousCounter")
                continuousCounter++
                delay(intervalMillis)
            }
        }
    }

    // 引擎 B：处理【剧本模式】的顺序播放
    LaunchedEffect(isScriptRunning, currentMode) {
        if (isScriptRunning && currentMode == MockMode.SCRIPTED) {
            while (scriptIndex < mockCorpus.size) {
                onSendMessage(mockCorpus[scriptIndex])
                scriptIndex++
                // 如果还没发完，就等待 1.5 秒再发下一条
                if (scriptIndex < mockCorpus.size) {
                    delay(1500L)
                } else {
                    // 剧本全部发完，自动停止
                    isScriptRunning = false
                }
            }
        }
    }

    // ==========================================
    // UI 渲染区 (UI Layout)
    // ==========================================
    PrimaryContainer(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 面板大标题
            Text(
                text = "Mock 消息发送控制台",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 2. 模式切换总闸 (采用内嵌浮动 Label 的样式)
            PrimaryDropdown(
                options = modeOptions,
                selectedOption = currentMode.label,
                onOptionSelected = { selectedLabel ->
                    val newMode = MockMode.entries.first { it.label == selectedLabel }
                    if (newMode != currentMode) {
                        // 【安全防呆】：切换模式时，强行关闭所有后台自动发送任务
                        isContinuousRunning = false
                        isScriptRunning = false
                        currentMode = newMode
                    }
                },
                label = "当前测试模式"
            )

            // 极简分割线，划定总闸与具体操作区的边界
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            )

            // 3. 动态工作区：根据当前模式渲染完全不同的 UI
            when (currentMode) {
                // ------------------------------------------
                // 分支 1：手动单发模式
                // ------------------------------------------
                MockMode.MANUAL -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PrimaryTextField(
                            value = manualText,
                            onValueChange = { manualText = it },
                            placeholderText = "输入要测试发送的内容..."
                        )
                        // 发送按钮（文字不消失，方便连点测试）
                        PrimaryButton(
                            text = "发送单条消息",
                            onClick = {
                                if (manualText.isNotBlank()) {
                                    onSendMessage(manualText)
                                }
                            },
                            enabled = manualText.isNotBlank(), // 空文本防呆置灰
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // ------------------------------------------
                // 分支 2：连续发送模式
                // ------------------------------------------
                MockMode.CONTINUOUS -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // 参数调节行：巧妙嵌入 SecondaryDropdown
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "发送频率：每隔",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            SecondaryDropdown(
                                options = intervalOptions,
                                selectedOption = selectedInterval,
                                onOptionSelected = { selectedInterval = it },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Text(
                                text = "自动发送一条",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // 状态预览与重置行
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isContinuousRunning) "状态：正在发送 (下一条: 测试消息 $continuousCounter)" else "状态：已就绪 (下一条: 测试消息 $continuousCounter)",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isContinuousRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            // 只有当计数器大于 1 且停止时，才显示重置按钮
                            if (!isContinuousRunning && continuousCounter > 1) {
                                TextButton(
                                    text = "重置计数",
                                    onClick = { continuousCounter = 1 }
                                )
                            }
                        }

                        // 启停控制按钮：运行时降级为 SecondaryButton，避免主色视觉疲劳
                        if (!isContinuousRunning) {
                            PrimaryButton(
                                text = "开始连续发送",
                                onClick = { isContinuousRunning = true },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            SecondaryButton(
                                text = "停止连续发送",
                                onClick = { isContinuousRunning = false },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // ------------------------------------------
                // 分支 3：剧本重演模式
                // ------------------------------------------
                MockMode.SCRIPTED -> {
                    val isFinished = scriptIndex >= mockCorpus.size

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // 进度与内容预览框
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "剧本进度",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$scriptIndex / ${mockCorpus.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // 预告下一条要发什么，让测试者心里有底
                            Text(
                                text = if (isFinished) "✅ 剧本已全部发送完毕" else "下一条：${mockCorpus[scriptIndex]}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }

                        // 完美运用三级按钮权重的控制栏
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. 开始/继续 / 暂停 按钮 (占据主要宽度)
                            if (!isScriptRunning) {
                                PrimaryButton(
                                    text = when {
                                        isFinished -> "重新运行剧本"
                                        scriptIndex > 0 -> "继续发送"
                                        else -> "开始按剧本发送"
                                    },
                                    onClick = {
                                        if (isFinished) scriptIndex = 0
                                        isScriptRunning = true
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                SecondaryButton(
                                    text = "暂停剧本",
                                    onClick = { isScriptRunning = false },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // 2. 终止并重置按钮 (仅在剧本进行到一半时出现，使用最低权重的 TextButton)
                            if (scriptIndex > 0 && !isFinished) {
                                TextButton(
                                    text = "终止重置",
                                    onClick = {
                                        isScriptRunning = false
                                        scriptIndex = 0
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// IDE 本地热预览 (自带简易日志接收器供测试)
// ==========================================
@Preview(showBackground = true, name = "Control Panel Preview")
@Composable
fun MockSenderControlPanelPreview() {
    InboxZeroTheme(darkTheme = false) {
        // 在预览中模拟一个极其简单的外部页面，用来接收并展示控制台发出的消息
        var lastReceivedMessage by remember { mutableStateOf("等待发送...") }
        var totalCount by remember { mutableIntStateOf(0) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 挂载我们刚刚写好的控制台
            MockSenderControlPanel(
                onSendMessage = { msg ->
                    lastReceivedMessage = msg
                    totalCount++
                }
            )

            // 简易的反馈显示区，让你在 Preview 的 Interactive 模式下直接看到发送效果！
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(
                    text = "外部接收器监控 (共收到 $totalCount 条)：",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lastReceivedMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}