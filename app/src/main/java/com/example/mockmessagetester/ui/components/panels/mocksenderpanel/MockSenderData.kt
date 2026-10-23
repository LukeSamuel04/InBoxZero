package com.example.mockmessagetester.ui.components.panels.mocksenderpanel

// ============================================================================
// 1. 核心枚举 (Core Enums)
// ============================================================================

/**
 * 控制台的核心工作模式。
 * 已经彻底废弃了底层的 BurstMode 枚举，改为三分天下的极简模式。
 */
enum class MockMode(val label: String) {
    MANUAL("手动单发 (Manual)"),
    REPEAT("无脑轰炸模式 (Repeat)"),
    SCRIPT("剧本情景模式 (Script)")
}

// ============================================================================
// 2. 状态契约 (State Contract / UI Payload)
// ============================================================================

/**
 * 这是一个纯粹的“只读快照”。它定格了 UI 在某一瞬间所有的状态。
 */
data class MockSenderUiState(
    // --- 全局状态 ---
    val currentMode: MockMode = MockMode.MANUAL, // 默认模式
    val isRunning: Boolean = false,              // 引擎是否正在转动[cite: 22]

    // --- 1. 单发模式专属状态 ---
    val manualText: String = "这是一条测试消息",   //[cite: 22]

    // --- 2. 重复轰炸模式专属状态 (取代了旧版的 String 下拉框) ---
    val currentBpm: Int = 30,                    // 绑定到 BpmConfigDial 表盘的核心变量

    // --- 3. 剧本模式专属状态 ---
    val selectedScenarioId: String? = null,      // 当前选中的剧本 ID

    // --- 状态反馈 ---
    // 用于在 UI 面板上回显刚刚发出去的那条消息内容
    val lastSentMessagePreview: String = "等待发射..." //[cite: 22]
)