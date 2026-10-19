package com.example.inboxzero.ui.components.panels.mocksenderpanel

import com.example.inboxzero.mocksender.worker.BurstMode

// ============================================================================
// 1. 核心枚举 (Core Enums)
// ============================================================================

/**
 * 控制台的四种核心工作模式。
 * 连发模式映射到底层的 BurstMode 引擎策略；手动单发不走后台引擎，因此为 null。
 */
enum class MockMode(val label: String, val burstMode: BurstMode?) {
    MANUAL("手动单发 (Manual)", null),
    NUMBERED("复读机模式 (Numbered)", BurstMode.NUMBERED),
    SCRIPTED("剧本顺序模式 (Sequential)", BurstMode.CORPUS_SEQUENTIAL),
    REALISTIC("拟真随机模式 (Realistic)", BurstMode.CORPUS_REALISTIC)
}

// ============================================================================
// 2. 状态契约 (State Contract / UI Payload)
// ============================================================================

/**
 * 这是一个纯粹的“只读快照”。它定格了 UI 在某一瞬间所有的状态。
 */
data class MockSenderUiState(
    // --- 全局状态 ---
    val currentMode: MockMode = MockMode.MANUAL,
    val isRunning: Boolean = false, // 引擎是否正在转动 (仅对后三种连发模式有效)

    // --- 单发模式专属状态 ---
    val manualText: String = "这是一条测试消息",

    // --- 连发设置 ---
    val selectedInterval: String = "1.0 s",
    val intervalOptions: List<String> = listOf("500 ms", "1.0 s", "2.0 s", "3.0 s"),

    // --- 状态反馈 ---
    // 用于在 UI 面板上回显刚刚发出去的那条消息内容
    val lastSentMessagePreview: String = "等待发射..."
)