package com.example.inboxzero.ui.components.panels.mocksenderpanel

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
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel

// 导入所有极简原子组件
import com.example.inboxzero.ui.components.buttons.PrimaryButton
import com.example.inboxzero.ui.components.buttons.SecondaryButton
import com.example.inboxzero.ui.components.buttons.TextButton
import com.example.inboxzero.ui.components.containers.PrimaryContainer
import com.example.inboxzero.ui.components.dropdowns.PrimaryDropdown
import com.example.inboxzero.ui.components.dropdowns.SecondaryDropdown
import com.example.inboxzero.ui.components.textfields.PrimaryTextField
import com.example.inboxzero.ui.theme.InboxZeroTheme

/**
 * 包装层 (Wrapper)：这个函数通常供外部页面直接调用。
 * 它负责生成 ViewModel 大脑，并把大脑里的数据和函数，喂给下方的哑巴画板。
 */
@Composable
fun MockSenderControlPanel(
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    manager: MockSenderManager = viewModel() // 自动生成并保存 ViewModel，屏幕旋转不丢失
) {
    // 设置监听器，将信号传给外部
    manager.setOnMessageListener(onSendMessage)

    // 从管道中收集最新的状态包，UI 会在这个包改变时自动重绘
    val uiState by manager.uiState.collectAsState()

    // 将毫无逻辑的 Dumb UI 渲染出来
    MockSenderUi(
        state = uiState,
        onModeChange = { manager.changeMode(it) },
        onManualTextChanged = { manager.updateManualText(it) },
        onSendManualClick = { manager.sendManualMessage() },
        onIntervalChanged = { manager.updateContinuousInterval(it) },
        onToggleContinuousClick = { manager.toggleContinuous() },
        onResetContinuousClick = { manager.resetContinuousCounter() },
        onToggleScriptClick = { manager.toggleScript() },
        onResetScriptClick = { manager.resetScript() },
        modifier = modifier
    )
}

/**
 * 哑巴画板层 (Dumb/Stateless UI)：
 * 核心设计规则 (HCI)：绝对没有内部业务状态。只负责吃传入的 Data 包，以及抛出点击信号。
 */
@Composable
fun MockSenderUi(
    state: MockSenderUiState,
    onModeChange: (MockMode) -> Unit,
    onManualTextChanged: (String) -> Unit,
    onSendManualClick: () -> Unit,
    onIntervalChanged: (String) -> Unit,
    onToggleContinuousClick: () -> Unit,
    onResetContinuousClick: () -> Unit,
    onToggleScriptClick: () -> Unit,
    onResetScriptClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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

            // 2. 模式切换总闸
            val modeLabels = MockMode.entries.map { it.label }
            PrimaryDropdown(
                options = modeLabels,
                selectedOption = state.currentMode.label,
                onOptionSelected = { label ->
                    val newMode = MockMode.entries.first { it.label == label }
                    onModeChange(newMode)
                },
                label = "当前测试模式"
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            )

            // 3. 动态工作区
            when (state.currentMode) {
                // ==========================
                // 手动单发模式
                // ==========================
                MockMode.MANUAL -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PrimaryTextField(
                            value = state.manualText,
                            onValueChange = onManualTextChanged,
                            placeholderText = "输入要测试发送的内容..."
                        )
                        PrimaryButton(
                            text = "发送单条消息",
                            onClick = onSendManualClick,
                            enabled = state.manualText.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // ==========================
                // 连续发送模式
                // ==========================
                MockMode.CONTINUOUS -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("发送频率：每隔", style = MaterialTheme.typography.bodyMedium)
                            SecondaryDropdown(
                                options = state.intervalOptions,
                                selectedOption = state.selectedInterval,
                                onOptionSelected = onIntervalChanged,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Text("自动发送一条", style = MaterialTheme.typography.bodyMedium)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (state.isRunning) "状态：正在发送 (下一条: 测试消息 ${state.continuousCounter})" else "状态：已就绪 (下一条: 测试消息 ${state.continuousCounter})",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (state.isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!state.isRunning && state.continuousCounter > 1) {
                                TextButton(text = "重置计数", onClick = onResetContinuousClick)
                            }
                        }

                        if (!state.isRunning) {
                            PrimaryButton(
                                text = "开始连续发送",
                                onClick = onToggleContinuousClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            SecondaryButton(
                                text = "停止连续发送",
                                onClick = onToggleContinuousClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // ==========================
                // 剧本重演模式
                // ==========================
                MockMode.SCRIPTED -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("剧本进度", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = "${state.scriptIndex} / ${state.mockCorpus.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = if (state.isScriptFinished) "✅ 剧本已全部发送完毕" else "下一条：${state.mockCorpus[state.scriptIndex]}",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!state.isRunning) {
                                PrimaryButton(
                                    text = when {
                                        state.isScriptFinished -> "重新运行剧本"
                                        state.scriptIndex > 0 -> "继续发送"
                                        else -> "开始按剧本发送"
                                    },
                                    onClick = onToggleScriptClick,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                SecondaryButton(
                                    text = "暂停剧本",
                                    onClick = onToggleScriptClick,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (state.scriptIndex > 0 && !state.isScriptFinished) {
                                TextButton(text = "终止重置", onClick = onResetScriptClick)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 测试与预览 (Preview)
// 极致解耦带来的好处：在预览时，我们甚至不需要真的 ViewModel！
// 我们可以直接造一个假的数据包塞给 MockSenderUi，测试各种极限状态。
// ============================================================================
@Preview(showBackground = true, name = "1. 单发模式就绪态")
@Composable
fun PreviewManualMode() {
    InboxZeroTheme(darkTheme = false) {
        MockSenderUi(
            state = MockSenderUiState(currentMode = MockMode.MANUAL, manualText = "预览文字"),
            onModeChange = {}, onManualTextChanged = {}, onSendManualClick = {},
            onIntervalChanged = {}, onToggleContinuousClick = {}, onResetContinuousClick = {},
            onToggleScriptClick = {}, onResetScriptClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "2. 连发模式运行态 (模拟极速发了 9999 条)")
@Composable
fun PreviewContinuousRunning() {
    InboxZeroTheme(darkTheme = true) { // 顺便测一下深色模式
        MockSenderUi(
            state = MockSenderUiState(
                currentMode = MockMode.CONTINUOUS,
                isRunning = true,
                continuousCounter = 9999,
                selectedInterval = "500 ms"
            ),
            // 假函数占位
            onModeChange = {}, onManualTextChanged = {}, onSendManualClick = {},
            onIntervalChanged = {}, onToggleContinuousClick = {}, onResetContinuousClick = {},
            onToggleScriptClick = {}, onResetScriptClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
@Preview(showBackground = true, name = "3. 剧本模式进行中 (发到一半暂停)")
@Composable
fun PreviewScriptedModePaused() {
    InboxZeroTheme(darkTheme = false) {
        MockSenderUi(
            state = MockSenderUiState(
                currentMode = MockMode.SCRIPTED,
                isRunning = false, // 模拟用户点了暂停
                scriptIndex = 2    // 模拟已经发完了前两条，准备发第三条
            ),
            // 所有点击事件传空，因为预览仅仅是为了看排版
            onModeChange = {}, onManualTextChanged = {}, onSendManualClick = {},
            onIntervalChanged = {}, onToggleContinuousClick = {}, onResetContinuousClick = {},
            onToggleScriptClick = {}, onResetScriptClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}