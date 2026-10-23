package com.example.mockmessagetester.ui.components.panels.mocksenderpanel

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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

import com.example.mockmessagetester.ui.components.buttons.PrimaryButton
import com.example.mockmessagetester.ui.components.buttons.SecondaryButton
import com.example.mockmessagetester.ui.components.buttons.TextButton
import com.example.mockmessagetester.ui.components.containers.PrimaryContainer
import com.example.mockmessagetester.ui.components.dropdowns.PrimaryDropdown
import com.example.mockmessagetester.ui.components.textfields.PrimaryTextField
import com.example.mockmessagetester.ui.components.dials.BpmConfigDial
import com.example.mockmessagetester.ui.theme.InboxZeroTheme

class MockSenderViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MockSenderManager::class.java)) {
            return MockSenderManager(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun MockSenderControlPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val manager: MockSenderManager = viewModel(factory = MockSenderViewModelFactory(application))
    val uiState by manager.uiState.collectAsState()

    val scenarioNames = manager.availableScenarios.map { it.scenarioName }
    val selectedScenarioName = manager.availableScenarios
        .find { it.scenarioId == uiState.selectedScenarioId }?.scenarioName ?: ""

    MockSenderUi(
        state = uiState,
        availableScenarioNames = scenarioNames,
        selectedScenarioName = selectedScenarioName,
        onModeChange = { manager.changeMode(it) },
        onManualTextChanged = { manager.updateManualText(it) },
        onSendManualClick = { manager.sendManualMessage() },
        onBpmChanged = { manager.updateBpm(it) },
        onScenarioSelected = { manager.updateSelectedScenario(it) },
        onToggleBurstClick = { manager.toggleBurst() },
        onResetBurstClick = { manager.resetBurst() },
        modifier = modifier
    )
}

@Composable
fun MockSenderUi(
    state: MockSenderUiState,
    availableScenarioNames: List<String>,
    selectedScenarioName: String,
    onModeChange: (MockMode) -> Unit,
    onManualTextChanged: (String) -> Unit,
    onSendManualClick: () -> Unit,
    onBpmChanged: (Int) -> Unit,
    onScenarioSelected: (String) -> Unit,
    onToggleBurstClick: () -> Unit,
    onResetBurstClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PrimaryContainer(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Mock 消息发送控制台",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

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

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

            when (state.currentMode) {
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
                MockMode.REPEAT -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PrimaryTextField(
                            value = state.manualText,
                            onValueChange = onManualTextChanged,
                            placeholderText = "输入用作高频轰炸的靶机内容..."
                        )
                        Box(modifier = Modifier.fillMaxWidth(0.7f)) {
                            BpmConfigDial(
                                bpm = state.currentBpm,
                                onBpmChange = onBpmChanged,
                                isRunning = state.isRunning,
                                onCenterClick = onToggleBurstClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                MockMode.SCRIPT -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PrimaryDropdown(
                            options = availableScenarioNames,
                            selectedOption = selectedScenarioName,
                            onOptionSelected = onScenarioSelected,
                            label = "选择测试剧本"
                        )
                    }
                }
            }

            if (state.currentMode != MockMode.MANUAL) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 针对剧本模式才显示底部的主控按钮
                        if (state.currentMode == MockMode.SCRIPT) {
                            if (!state.isRunning) {
                                PrimaryButton(text = "开始自动发射", onClick = onToggleBurstClick, modifier = Modifier.weight(1f))
                            } else {
                                SecondaryButton(text = "暂停发射", onClick = onToggleBurstClick, modifier = Modifier.weight(1f))
                            }
                        } else {
                            // 重复模式下，用占位符把重置按钮推到右侧
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        if (!state.isRunning) {
                            TextButton(text = "重置洗牌", onClick = onResetBurstClick)
                        }
                    }
                }
            }
        }
    }
}