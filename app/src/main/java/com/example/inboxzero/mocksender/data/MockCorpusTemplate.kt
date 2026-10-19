package com.example.inboxzero.mocksender.data

import java.util.UUID

/**
 * 核心数据契约 (Data Contract)：
 * 规范了整个 InboxZero 系统中流通的每一条消息长什么样。
 */
data class MockMessage(
    val appName: String,          // 来源 App (如 "微信", "淘宝")
    val sender: String,           // 发送者 (如 "导师", "张三")
    val content: String,          // 消息正文
    val groupName: String? = null, // 群聊名称。如果不填 (null) 则代表是私聊

    // --- 剧本控制元数据 (Metadata) ---
    val threadId: String? = null, // 故事线 ID，比如 "paper_review" (论文批改事件)
    val step: Int = 0,            // 故事线中的顺序，必须按 1, 2, 3 的顺序发

    // --- 物理层实例数据 ---
    val id: String = UUID.randomUUID().toString(), // 每次创建自动生成绝对唯一的 ID
    val timestamp: Long = System.currentTimeMillis() // 记录这条消息产生的精确毫秒时间
)