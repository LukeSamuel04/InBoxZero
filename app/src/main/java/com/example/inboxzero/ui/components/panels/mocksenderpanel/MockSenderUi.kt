package com.example.inboxzero.ui.components.panels.mocksenderpanel

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

// 导入极简原子组件
import com.example.inboxzero.ui.components.buttons.PrimaryButton
import com.example.inboxzero.ui.components.buttons.SecondaryButton
import com.example.inboxzero.ui.components.buttons.TextButton
import com.example.inboxzero.ui.components.containers.PrimaryContainer
import com.example.inboxzero.ui.components.dropdowns.PrimaryDropdown
import com.example.inboxzero.ui.components.dropdowns.SecondaryDropdown
import com.example.inboxzero.ui.components.textfields.PrimaryTextField
import com.example.inboxzero.ui.theme.InboxZeroTheme

// ============================================================================
// 兼容低版本的终极解法：我们自己手写一个工厂 (Factory)。
// 当 Compose 问 "我该怎么造出 MockSenderManager" 时，我们就用这个工厂告诉它：
// "拿着我给你的 application，调用 MockSenderManager(application) 把它造出来"。
// ============================================================================
class MockSenderViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MockSenderManager::class.java)) {
            return MockSenderManager(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * 包装层 (Wrapper)：
 * 负责生成 ViewModel 大脑，并把大脑里的数据和函数，喂给下方的哑巴画板。
 */
@Composable
fun MockSenderControlPanel(
    modifier: Modifier = Modifier
) {
    // 1. 手动从当前的 Compose 环境中把 Application 抓取出来
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // 2. 利用我们自己写的 Factory，向系统正式申请一个带 Context 的 ViewModel
    val manager: MockSenderManager = viewModel(
        factory = MockSenderViewModelFactory(application)
    )

    // 3. 从管道中收集最新的状态包，UI 会在这个包改变时自动重绘
    val uiState by manager.uiState.collectAsState()

    // 4. 将毫无逻辑的 Dumb UI 渲染出来
    MockSenderUi(
        state = uiState,
        onModeChange = { manager.changeMode(it) },

        // 单发控制
        onManualTextChanged = { manager.updateManualText(it) },
        onSendManualClick = { manager.sendManualMessage() },

        // 连发控制 (三种自动模式共用)
        onIntervalChanged = { manager.updateContinuousInterval(it) },
        onToggleBurstClick = { manager.toggleBurst() },
        onResetBurstClick = { manager.resetBurst() },

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
    onToggleBurstClick: () -> Unit,
    onResetBurstClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PrimaryContainer(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // 1. 面板大标题
            // ==========================================
            Text(
                text = "Mock 消息发送控制台",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // ==========================================
            // 2. 模式切换总闸
            // ==========================================
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

            // ==========================================
            // 3. 动态工作区
            // ==========================================
            if (state.currentMode == MockMode.MANUAL) {
                // --------------------------
                // A. 手动单发面板
                // --------------------------
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
            } else {
                // --------------------------
                // B. 自动连发共享面板 (涵盖三种连发模式)
                // --------------------------
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // a) 发送频率设置
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
                        Text("自动发射一条", style = MaterialTheme.typography.bodyMedium)
                    }

                    // b) 最新发送动态反馈框 (取代了生硬的计数)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "最新发射状态",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = state.lastSentMessagePreview,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // c) 操控按钮区
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!state.isRunning) {
                            PrimaryButton(
                                text = "开始自动发射",
                                onClick = onToggleBurstClick,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            SecondaryButton(
                                text = "暂停",
                                onClick = onToggleBurstClick,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // 只要没在跑，就允许一键重置，打扫战场重新洗牌
                        if (!state.isRunning) {
                            TextButton(text = "重置", onClick = onResetBurstClick)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 测试与预览 (Preview)
// ============================================================================

@Preview(showBackground = true, name = "1. 单发模式")
@Composable
fun PreviewManualMode() {
    InboxZeroTheme(darkTheme = false) {
        MockSenderUi(
            state = MockSenderUiState(currentMode = MockMode.MANUAL, manualText = "预览单发文字"),
            onModeChange = {}, onManualTextChanged = {}, onSendManualClick = {},
            onIntervalChanged = {}, onToggleBurstClick = {}, onResetBurstClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "2. 自动连发 (进行中)")
@Composable
fun PreviewContinuousRunning() {
    InboxZeroTheme {
        MockSenderUi(
            state = MockSenderUiState(
                currentMode = MockMode.REALISTIC,
                isRunning = true,
                lastSentMessagePreview = "导师: 下周把那篇论文发我",
                selectedInterval = "1.0 s"
            ),
            onModeChange = {}, onManualTextChanged = {}, onSendManualClick = {},
            onIntervalChanged = {}, onToggleBurstClick = {}, onResetBurstClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "3. 自动连发 (已停止态)")
@Composable
fun PreviewContinuousStopped() {
    InboxZeroTheme {
        MockSenderUi(
            state = MockSenderUiState(
                currentMode = MockMode.SCRIPTED,
                isRunning = false,
                lastSentMessagePreview = "已播报完毕 / 弹药重置就绪"
            ),
            onModeChange = {}, onManualTextChanged = {}, onSendManualClick = {},
            onIntervalChanged = {}, onToggleBurstClick = {}, onResetBurstClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}