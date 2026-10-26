package com.example.inboxzero.data.local.messages

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

// ==========================================
// 逻辑分组组件 (供 @Embedded 使用)
// ==========================================

data class SenderInfo(
    val isGroup: Boolean,
    val groupName: String?,
    val senderName: String,
    val senderId: String?
)

data class AiMetadata(
    /**
     * AI 处理状态机：
     * 0 = 等待处理 (默认)
     * 1 = 分析成功
     * 2 = 文本过短或无需分析
     * 3 = 网络异常/处理失败
     */
    val aiProcessStatus: Int = 0,    // 核心状态字段
    val aiSummary: String? = null,   // 异步回填摘要
    val urgencyScore: Int? = null    // 异步回填评分
)

data class InteractionState(
    val isRead: Boolean = false,
    val isArchived: Boolean = false,
    val isStarred: Boolean = false
)

// ==========================================
// 真实的数据库表映射 (大宽表)
// ==========================================

@Entity(tableName = "messages")
data class MessageEntity(
    // 基础寻址与内容 (放在根节点最方便直观)
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val postTime: Long,
    val content: String,
    val rawDataLog: String,

    // 逻辑分组模块 (Room 会在建表时自动将子类里的字段打平，变成表里的列)
    @Embedded
    val senderInfo: SenderInfo,

    @Embedded
    val aiMetadata: AiMetadata = AiMetadata(), // 赋予默认值，拦截器光速入库时可直接忽略此项

    @Embedded
    val interactionState: InteractionState = InteractionState() // 赋予默认值
)