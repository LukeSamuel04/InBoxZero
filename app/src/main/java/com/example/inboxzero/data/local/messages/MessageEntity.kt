package com.example.inboxzero.data.local.messages

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ==========================================
// 逻辑分组组件 (供 @Embedded 使用)
// ==========================================

data class SenderInfo(
    /**
     * 消息类型：
     * 1 = 单聊 (Private)
     * 2 = 群聊 (Group)
     * 3 = 系统/服务通知 (System)
     */
    val messageType: Int,
    val isMentioned: Boolean = false, // 是否被 @ 提及
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
    val aiProcessStatus: Int = 0,    // 核心状态字段[cite: 12]
    val aiSummary: String? = null,   // 异步回填摘要[cite: 12]
    val urgencyScore: Int? = null    // 异步回填评分[cite: 12]
)

data class InteractionState(
    val isRead: Boolean = false,     //[cite: 12]
    val isArchived: Boolean = false, //[cite: 12]
    val isStarred: Boolean = false   //[cite: 12]
)

// ==========================================
// 真实的数据库表映射 (大宽表)
// ==========================================

// 增加数据库索引，防止数据量达到十万级时全表扫描导致手机卡顿发热
@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["aiProcessStatus"]), // 加速 Worker 查找未处理消息
        Index(value = ["isArchived", "postTime"]), // 加速 UI 层列表排序查询
        Index(value = ["packageName", "messageType"]) // 加速按应用和类型检索
    ]
)
data class MessageEntity(
    // 基础寻址与内容
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,                 //[cite: 12]
    val systemKey: String,            // 系统通知的唯一凭证 (sbn.key)，用于反向消除状态栏通知
    val packageName: String,          //[cite: 12]
    val postTime: Long,               //[cite: 12]
    val title: String,                // 核心标题字段 (此前遗漏，现已补齐)
    val content: String,              //[cite: 12]

    /**
     * 附件类型标识：
     * 0 = 无附件 (纯文本)
     * 1 = 图片
     * 2 = 语音
     */
    val attachmentType: Int = 0,

    val rawDataLog: String,           //[cite: 12]

    // 逻辑分组模块 (Room 会在建表时自动将子类里的字段打平，变成表里的列)[cite: 12]
    @Embedded
    val senderInfo: SenderInfo,

    @Embedded
    val aiMetadata: AiMetadata = AiMetadata(), // 赋予默认值，拦截器光速入库时可直接忽略此项[cite: 12]

    @Embedded
    val interactionState: InteractionState = InteractionState() // 赋予默认值[cite: 12]
)