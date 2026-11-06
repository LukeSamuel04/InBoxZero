package com.example.inboxzero.data.model

/**
 * 看板聚合会话标准实体 (Inbox Session Entity)
 *
 * 架构定位：
 * 1. 入库时：Parser 使用 [InboxMessage] 作为单条通知的“入库包裹”写入数据库。
 * 2. 出库时：Repository 将属于同一会话的多条 [InboxMessage] 聚合，并结合数据库中的
 *    AI 状态 (AiMetadata) 与交互状态 (InteractionState)，组装成本模型流转至 UI 层。
 * 3. 纯净度契约：严格位于 data.model 层，零 Compose UI / Color 依赖。
 */
data class InboxSession(
    // ==========================================
    // 1. 会话聚合标识与基础来源 (对齐 InboxMessage 第 1 部分)
    // ==========================================
    val sessionId: String,            // 会话聚合唯一键 (通常由 packageName + groupName/senderId 构成)
    val packageName: String,          // 对应 InboxMessage.packageName (如 "com.tencent.mm")
    val appDisplayName: String,       // 应用可读名称 (如 "微信" / "Outlook")
    val latestPostTime: Long,         // 最新一条消息的 postTime 毫秒时间戳 (用于 Baseline 时间倒序)

    // ==========================================
    // 2. 逻辑分组与发送者信息 (100% 对齐 InboxMessage 第 2 部分 SenderInfo)
    // ==========================================
    /**
     * 消息类型 (对齐 InboxMessage.messageType)：
     * 1 = 单聊 (Private)
     * 2 = 群聊 (Group)
     * 3 = 系统/服务通知 (System)
     */
    val messageType: Int = 1,
    val isMentioned: Boolean = false, // 该会话未读消息中是否有 "@我" 的提及 (对齐 InboxMessage.isMentioned)
    val groupName: String? = null,    // 群聊名称 (当 messageType == 2 时有效，对齐 InboxMessage.groupName)
    val senderName: String = "",      // 单聊联系人姓名 或 最新发言人姓名 (对齐 InboxMessage.senderName)
    val senderId: String? = null,     // 发送者唯一标识 (对齐 InboxMessage.senderId)

    // ==========================================
    // 3. AI 引擎元数据 (对应数据库后期生成的 AiMetadata)
    // ==========================================
    val isAnalyzing: Boolean = false, // true = 刚入库等待 AI 异步评分中；false = AI 评分与摘要已就绪
    val urgencyScore: Int = 0,        // AI 混合引擎计算的紧急度评分 (0 ~ 100)
    val aiSummary: String? = null,    // AI 提炼的一句话会话话题摘要

    // ==========================================
    // 4. 聚合消息明细与交互状态 (对应 InteractionState 与 RemoteInput)
    // ==========================================
    val unreadCount: Int = 1,         // 当前会话聚合的未读消息条数
    val messages: List<InboxMessage>, // 直接内嵌该会话下的原始 [InboxMessage] 列表，零重复造轮子！
    val canInlineReply: Boolean = true, // 是否在内存中持有系统 RemoteInput 凭证支持原地回复
    val smartReplies: List<String> = emptyList() // 可选的快捷回复短语
) {
    /**
     * 便捷计算属性：获取卡片主标题
     * - 如果是群聊 (messageType == 2) 且有群名，主标题显示群名 (groupName)
     * - 否则显示联系人/发送方名称 (senderName)
     */
    val displayTitle: String
        get() = if (messageType == 2 && !groupName.isNullOrBlank()) groupName else senderName
}