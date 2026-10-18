package com.example.inboxzero.ui.components.panels.mocksenderpanel

// ============================================================================
// 1. 核心枚举 (Core Enums)
// ============================================================================

/**
 * 控制台的三种核心工作模式
 */
enum class MockMode(val label: String) {
    MANUAL("单发模式 (Manual)"),
    CONTINUOUS("连发模式 (Auto-Send)"),
    SCRIPTED("剧本模式 (Scripted)")
}

// ============================================================================
// 2. 状态契约 (State Contract / UI Payload)
// ============================================================================

/**
 * 这是一个纯粹的“只读快照”。它定格了 UI 在某一瞬间所有的状态。
 * 它是管理层（Manager）和表现层（UI）之间唯一的通信包裹。
 */
data class MockSenderUiState(
    // --- 全局状态 ---
    val currentMode: MockMode = MockMode.MANUAL,

    // --- 单发模式专属状态 ---
    val manualText: String = "这是一条测试消息",

    // --- 连发/剧本模式共享的运行状态 ---
    val isRunning: Boolean = false, // 引擎是否正在转动 (控制按钮显示为“开始”还是“暂停/停止”)

    // --- 连发模式专属状态 ---
    val continuousCounter: Int = 1,
    val selectedInterval: String = "1.0 s", // 默认选中 1.0s
    val intervalOptions: List<String> = listOf("500 ms", "1.0 s", "2.0 s", "3.0 s"),

    // --- 剧本模式专属状态 ---
    val scriptIndex: Int = 0,
    val mockCorpus: List<String> = listOf(
        "【系统通知】您的验证码是 8921，请勿泄露。",
        "【快递提醒】您的包裹已存放至丰巢快递柜，取件码 12-3456。",
        "老板：今天下午 3 点开个紧急碰头会，收到请回复。",
        "信用卡中心：您尾号 8888 的卡片最新账单已生成。",
        "妈妈：周末回家吃饭吗？做了你爱吃的红烧肉。"
    )
) {
    // 派生属性：剧本是否已经全部发完 (方便 UI 层直接调用，不用自己算)
    val isScriptFinished: Boolean
        get() = scriptIndex >= mockCorpus.size
}