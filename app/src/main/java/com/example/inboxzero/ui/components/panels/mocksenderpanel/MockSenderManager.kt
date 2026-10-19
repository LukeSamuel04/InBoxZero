package com.example.inboxzero.ui.components.panels.mocksenderpanel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.inboxzero.mocksender.core.NotificationDispatcher
import com.example.inboxzero.mocksender.worker.AutoBurstWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 调度中心 / 管理层 (Manager)：
 * 核心职责是：作为 UI 界面 和 底层 Worker 引擎的传话筒。
 * 继承 AndroidViewModel 以便获取 Application Context 传递给 NotificationDispatcher。
 */
class MockSenderManager(application: Application) : AndroidViewModel(application) {

    // ==========================================
    // 状态管道 (UI State Pipeline)
    // ==========================================
    private val _uiState = MutableStateFlow(MockSenderUiState())
    val uiState: StateFlow<MockSenderUiState> = _uiState.asStateFlow()

    // ==========================================
    // 引擎依赖注入 (Dependencies)
    // ==========================================
    // 将 application 作为 Context 传递给 Dispatcher
    private val dispatcher = NotificationDispatcher(application)
    private val worker = AutoBurstWorker(dispatcher)

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

    // --- 单发模式相关 ---
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

    // --- 连发模式相关 ---
    fun updateContinuousInterval(newInterval: String) {
        _uiState.update { it.copy(selectedInterval = newInterval) }
        if (_uiState.value.isRunning) {
            startEngine()
        }
    }

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
        val currentBurstMode = currentState.currentMode.burstMode

        if (currentBurstMode == null) return

        val delayMillis = parseInterval(currentState.selectedInterval)

        _uiState.update { it.copy(isRunning = true) }

        worker.startBurst(
            mode = currentBurstMode,
            intervalMs = delayMillis,
            onProgress = { sentMsg ->
                val previewText = sentMsg.groupName?.let { "群聊[${it}] ${sentMsg.sender}: ${sentMsg.content}" }
                    ?: "${sentMsg.sender}: ${sentMsg.content}"

                _uiState.update { it.copy(lastSentMessagePreview = previewText) }
            },
            onFinish = {
                _uiState.update { it.copy(
                    isRunning = false,
                    lastSentMessagePreview = "✅ 语料已全部播送完毕，请重置弹药"
                )}
            }
        )
    }

    private fun stopEngine() {
        worker.stopBurst()
        _uiState.update { it.copy(isRunning = false, lastSentMessagePreview = "已手动暂停发射") }
    }

    // ==========================================
    // 工具方法 (Utils)
    // ==========================================
    private fun parseInterval(intervalStr: String): Long {
        return when (intervalStr) {
            "500 ms" -> 500L
            "1.0 s" -> 1000L
            "2.0 s" -> 2000L
            "3.0 s" -> 3000L
            else -> 1000L
        }
    }
}