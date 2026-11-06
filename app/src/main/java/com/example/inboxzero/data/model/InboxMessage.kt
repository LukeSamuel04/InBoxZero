package com.example.inboxzero.data.model

/**
 * 纯数据实体：所有消息解析后的最终标准归宿。
 * 它是隔绝底层数据库和系统通知的“通用货币”，负责在 Parser、Scheduler、Repository 和 UI 之间安全流转。[cite: 13]
 */
data class InboxMessage(
    // ==========================================
    // 1. 基础通知内容[cite: 13]
    // ==========================================
    val systemKey: String,            // 新增：系统通知的唯一凭证 (sbn.key)
    val packageName: String,          //[cite: 13]
    val postTime: Long,               //[cite: 13]
    val title: String,                //[cite: 13]
    val content: String,              //[cite: 13]

    /**
     * 附件类型标识：
     * 0 = 无附件 (纯文本)
     * 1 = 图片
     * 2 = 语音
     */
    val attachmentType: Int = 0,      // 新增：附件类型，默认为 0 (无附件)

    val rawDataLog: String = "",      //[cite: 13]

    // ==========================================
    // 2. 逻辑分组信息 (对应底层数据库的 SenderInfo)[cite: 13]
    // ==========================================

    /**
     * 消息类型：
     * 1 = 单聊 (Private)
     * 2 = 群聊 (Group)
     * 3 = 系统/服务通知 (System)
     */
    val messageType: Int = 1,         // 替换 isGroup，默认 1 (单聊)
    val isMentioned: Boolean = false, // 新增：是否被 @ 提及

    val groupName: String? = null,    //[cite: 13]
    val senderName: String = "",      //[cite: 13]
    val senderId: String? = null      //[cite: 13]

    // 注意：AI 状态 (AiMetadata) 和用户交互状态 (InteractionState) 属于数据库默认生成的后期业务字段。[cite: 13]
    // 由于 Parser 解析时不需要也不可能知道这些状态，所以这个作为“入库包裹”的模具不需要包含它们。[cite: 13]
)