package com.example.inboxzero.data.model

/**
 * 纯数据实体：所有消息解析后的最终标准归宿
 */
data class InboxMessage(
    val packageName: String,
    val postTime: Long,
    val title: String,
    val content: String,
    val rawDataLog: String = ""
)