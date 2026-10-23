package com.example.mockmessagetester.ui.components.panels.mocksenderpanel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.mockmessagetester.mocksender.core.NotificationDispatcher
import com.example.mockmessagetester.mocksender.data.MockMessage
import com.example.mockmessagetester.mocksender.data.corpus.thesisCrisisScenario
import com.example.mockmessagetester.mocksender.worker.AutoBurstWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 调度中心 / 管理层 (Manager)：
 * 核心职责：将 UI 的控制信号（如 BPM 旋钮）实时转化为 Worker 双引擎能够执行的指令。
 */
class MockSenderManager(application: Application) : AndroidViewModel(application) {

    // ==========================================
    // 状态管道 (UI State Pipeline)
    // ==========================================
    private val _uiState = MutableStateFlow(MockSenderUiState())
    val uiState: StateFlow<MockSenderUiState> = _uiState.asStateFlow()

    // 暴露给 UI 的剧本库数据
    val availableScenarios = listOf(thesisCrisisScenario)

    // ==========================================
    // 引擎依赖注入 (Dependencies)
    // ==========================================
    private val dispatcher = NotificationDispatcher(application)
    private val worker = AutoBurstWorker(dispatcher)

    init {
        // 初始化时，默认选中第一个可用剧本
        val defaultScenario = availableScenarios.firstOrNull()
        if (defaultScenario != null) {
            _uiState.update { it.copy(selectedScenarioId = defaultScenario.scenarioId) }
        }
    }

    // ==========================================
    // 接收 UI 传来的信号 (Intent Receivers)
    // ==========================================

    fun changeMode(newMode: MockMode) {
        if (newMode == _uiState.value.currentMode) return

        worker.stopBurst()
        worker.resetCounters()
        _uiState.update { it.copy(
            currentMode = newMode,
            isRunning = false,
            lastSentMessagePreview = "模式已切换，等待发射..."
        )}
    }

    // --- 单发模式 ---
    fun updateManualText(newText: String) {
        _uiState.update { it.copy(manualText = newText) }
    }

    fun sendManualMessage() {
        val text = _uiState.value.manualText
        if (text.isNotBlank()) {
            dispatcher.sendSingle(sender = "手动单发测试", content = text)
            _uiState.update { it.copy(lastSentMessagePreview = "发送成功: $text") }
        }
    }

    // --- 重复轰炸模式 (表盘) ---
    fun updateBpm(bpm: Int) {
        _uiState.update { it.copy(currentBpm = bpm) }
        // 如果正在运行中拨动了表盘，为了保证物理手感，目前不需要强制重启引擎
        // (在更高级的实现中，可以在这里动态通知 Worker 更新 interval)
    }

    // --- 剧本模式 ---
    fun updateSelectedScenario(scenarioName: String) {
        val matchedScenario = availableScenarios.find { it.scenarioName == scenarioName }
        if (matchedScenario != null) {
            _uiState.update { it.copy(selectedScenarioId = matchedScenario.scenarioId) }
        }
    }

    // --- 共有播控 ---
    fun toggleBurst() {
        if (_uiState.value.isRunning) {
            stopEngine()
        } else {
            startEngine()
        }
    }

    fun resetBurst() {
        stopEngine()
        worker.resetCounters()
        _uiState.update { it.copy(lastSentMessagePreview = "弹药已重置洗牌，准备就绪") }
    }

    // ==========================================
    // 私有核心引擎控制 (Private Engine Control)
    // ==========================================

    private fun startEngine() {
        val currentState = _uiState.value
        _uiState.update { it.copy(isRunning = true) }

        when (currentState.currentMode) {
            MockMode.REPEAT -> {
                // 核心换算：将表盘 BPM 转为毫秒延时 (例如 60 BPM = 1000ms)
                val bpm = currentState.currentBpm.coerceAtLeast(1)
                val intervalMs = 60000L / bpm

                val targetMessage = MockMessage(
                    appName = "系统测试",
                    sender = "高频轰炸机",
                    content = currentState.manualText.ifBlank { "压力测试消息" }
                )

                worker.startRepeatMode(
                    targetMessage = targetMessage,
                    intervalMs = intervalMs,
                    onProgress = { sentMsg ->
                        _uiState.update { it.copy(lastSentMessagePreview = "${sentMsg.sender}: ${sentMsg.content}") }
                    }
                )
            }

            MockMode.SCRIPT -> {
                val scenario = availableScenarios.find { it.scenarioId == currentState.selectedScenarioId }
                if (scenario != null) {
                    worker.startScriptMode(
                        scenario = scenario,
                        onProgress = { sentMsg ->
                            val previewText = sentMsg.groupName?.let { "群聊[${it}] ${sentMsg.sender}: ${sentMsg.content}" }
                                ?: "${sentMsg.sender}: ${sentMsg.content}"

                            _uiState.update { it.copy(lastSentMessagePreview = previewText) }
                        },
                        onFinish = {
                            _uiState.update { it.copy(
                                isRunning = false,
                                lastSentMessagePreview = "✅ 剧本全部播送完毕，已自动杀青"
                            )}
                        }
                    )
                } else {
                    stopEngine()
                }
            }

            MockMode.MANUAL -> {
                _uiState.update { it.copy(isRunning = false) }
            }
        }
    }

    private fun stopEngine() {
        worker.stopBurst()
        _uiState.update { it.copy(isRunning = false, lastSentMessagePreview = "已手动暂停") }
    }
}