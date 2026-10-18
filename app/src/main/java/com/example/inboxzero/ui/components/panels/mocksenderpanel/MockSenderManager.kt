package com.example.inboxzero.ui.components.panels.mocksenderpanel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 调度中心 / 管理层。
 * 它持有 UiState 的可变版本，负责所有复杂的协程逻辑。
 * 它继承 ViewModel，这意味着它的生命周期比 UI 长，无惧屏幕旋转。
 */
class MockSenderManager : ViewModel() {

    // 内部持有一个【可变】的数据管道 (只有自己能改)
    private val _uiState = MutableStateFlow(MockSenderUiState())
    // 暴露给外部一个【只读】的数据管道 (UI 只能看)
    val uiState: StateFlow<MockSenderUiState> = _uiState.asStateFlow()

    // 这个回调用于向外发射真正的发送信号 (比如传给日志面板)
    // 工业上通常用 SharedFlow，但为了保持极简，我们这里提供一个设置监听器的口子
    private var messageListener: ((String) -> Unit)? = null

    // 记录当前正在运行的后台任务，方便随时取消 (掐死协程)
    private var engineJob: Job? = null

    fun setOnMessageListener(listener: (String) -> Unit) {
        messageListener = listener
    }

    // ==========================================
    // 接收 UI 传来的信号 (Intent Receivers)
    // ==========================================

    fun changeMode(newMode: MockMode) {
        if (newMode == _uiState.value.currentMode) return

        // 安全防呆：切换模式时，立刻掐死可能正在跑的后台任务，重置运行状态
        stopEngine()
        _uiState.update { it.copy(
            currentMode = newMode,
            isRunning = false
        )}
    }

    // --- 单发模式相关 ---
    fun updateManualText(newText: String) {
        _uiState.update { it.copy(manualText = newText) }
    }

    fun sendManualMessage() {
        val text = _uiState.value.manualText
        if (text.isNotBlank()) {
            messageListener?.invoke(text)
        }
    }

    // --- 连发模式相关 ---
    fun updateContinuousInterval(newInterval: String) {
        _uiState.update { it.copy(selectedInterval = newInterval) }
        // 如果正在连发时改了时间，需要重启引擎应用新时间
        if (_uiState.value.isRunning && _uiState.value.currentMode == MockMode.CONTINUOUS) {
            startContinuousEngine()
        }
    }

    fun toggleContinuous() {
        if (_uiState.value.isRunning) stopEngine() else startContinuousEngine()
    }

    fun resetContinuousCounter() {
        stopEngine()
        _uiState.update { it.copy(continuousCounter = 1) }
    }

    // --- 剧本模式相关 ---
    fun toggleScript() {
        if (_uiState.value.isRunning) stopEngine() else startScriptEngine()
    }

    fun resetScript() {
        stopEngine()
        _uiState.update { it.copy(scriptIndex = 0) }
    }

    // ==========================================
    // 私有核心引擎逻辑 (Private Engines)
    // ==========================================

    private fun stopEngine() {
        engineJob?.cancel()
        engineJob = null
        _uiState.update { it.copy(isRunning = false) }
    }

    private fun startContinuousEngine() {
        stopEngine() // 先清理旧任务
        _uiState.update { it.copy(isRunning = true) }

        engineJob = viewModelScope.launch {
            while (true) { // 只要 Job 不被取消，就会一直转
                val state = _uiState.value
                val delayMillis = parseInterval(state.selectedInterval)

                messageListener?.invoke("测试消息 ${state.continuousCounter}")
                _uiState.update { it.copy(continuousCounter = state.continuousCounter + 1) }

                delay(delayMillis)
            }
        }
    }

    private fun startScriptEngine() {
        stopEngine()
        _uiState.update { it.copy(isRunning = true) }

        // 如果已经发完了，自动重头开始
        if (_uiState.value.isScriptFinished) {
            _uiState.update { it.copy(scriptIndex = 0) }
        }

        engineJob = viewModelScope.launch {
            while (true) {
                val state = _uiState.value
                if (state.scriptIndex < state.mockCorpus.size) {
                    messageListener?.invoke(state.mockCorpus[state.scriptIndex])
                    _uiState.update { it.copy(scriptIndex = state.scriptIndex + 1) }

                    // 剧本发完最后一条后自动停止
                    if (_uiState.value.scriptIndex >= state.mockCorpus.size) {
                        stopEngine()
                        break // 跳出死循环
                    }
                    delay(1500L) // 剧本模式固定每条间隔 1.5 秒
                }
            }
        }
    }

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