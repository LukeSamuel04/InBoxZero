package com.example.inboxzero.data.model

/**
 * 看板聚合会话标准实体 (Inbox Session Entity)
 *
 * 严格遵循单一职责原则 (SRP)：
 * 1. 仅作为承载客观会话数据的纯容器 (Pure Data Carrier)，不包含任何包名解析或 UI 分类逻辑。
 * 2. 所有字段均对应底层数据库 [InboxMessage] 聚合结果与 AI 评分结果，不随前端 UI 样式重构而变动。
 */
data class InboxSession(
    // 1. 基础客观来源信息
    val sessionId: String,
    val packageName: String,
    val appDisplayName: String,
    val latestPostTime: Long,

    // 2. 发送者与群组信息 (对齐 InboxMessage)
    val messageType: Int = 1,         // 1 = 单聊, 2 = 群聊, 3 = 系统/服务通知
    val isMentioned: Boolean = false, // 是否有 @我
    val groupName: String? = null,
    val senderName: String = "",
    val senderId: String? = null,

    // 3. AI 引擎计算结果 (对应数据库 AiMetadata)
    val isAnalyzing: Boolean = false,
    val urgencyScore: Int = 0,
    val aiSummary: String? = null,

    // 4. 聚合消息列表与基础交互状态 (对应数据库 InteractionState)
    val unreadCount: Int = 1,
    val isRead: Boolean = false,
    val messages: List<InboxMessage>,
    val canInlineReply: Boolean = true,
    val smartReplies: List<String> = emptyList()
)