package com.example.inboxzero.core.notificationlistening.parser.implementations

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.inboxzero.core.notificationlistening.parser.NotificationParser
import com.example.inboxzero.data.model.InboxMessage

class DefaultParser : NotificationParser {
    override fun parse(sbn: StatusBarNotification): InboxMessage {
        Log.d("InboxZero_Parser", "🔀 流量进入 [DefaultParser] 默认通道")

        val extras = sbn.notification.extras
        val rawTitle = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val rawLog = dumpExtras(sbn.packageName, extras)

        // ==========================================
        // 1. 提取系统级核心凭证
        // ==========================================
        val systemKey = sbn.key // 极其重要：这是未来消除系统状态栏通知的钥匙

        // ==========================================
        // 2. 多媒体附件类型推断
        // ==========================================
        var attachmentType = 0
        if (text.contains("[图片]") || text.contains("[Photo]")) {
            attachmentType = 1
        } else if (text.contains("[语音]") || text.contains("[Voice]")) {
            attachmentType = 2
        }

        // ==========================================
        // 3. 消息类型与业务属性推断
        // ==========================================
        var messageType = 1 // 默认当做单聊
        var extractedGroupName: String? = null
        var extractedSenderName = rawTitle

        // 【推断 A】：是否被 @ 提及？ (简单正则：标题或内容里含有 @)
        val isMentioned = rawTitle.contains("@") || text.contains("@")

        // 【推断 B】：是否为系统通知？ (如果是 Android 自身系统，或者干脆没标题的纯功能通知)
        if (rawTitle.isEmpty() || sbn.packageName == "android" || sbn.packageName == "com.android.systemui") {
            messageType = 3
            extractedSenderName = "系统推送"
        }
        // 【推断 C】：如果是普通的通讯软件，判断是不是群聊？
        else {
            val isSystemGroup = extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false)
            val conversationTitle = extras.getString(Notification.EXTRA_CONVERSATION_TITLE)

            if (isSystemGroup || conversationTitle != null) {
                // 走 Android 标准 MessagingStyle 的规范群聊
                messageType = 2
                extractedGroupName = conversationTitle
            } else if (rawTitle.contains(": ")) {
                // 走降级正则：非标 App 喜欢的 "群名: 发件人" 格式
                val parts = rawTitle.split(": ", limit = 2)
                if (parts.size == 2) {
                    messageType = 2
                    extractedGroupName = parts[0]
                    extractedSenderName = parts[1]
                }
            }
        }

        // 兜底防空
        if (extractedSenderName.isEmpty()) extractedSenderName = "未知发件人"

        // ==========================================
        // 4. 完美打包上交
        // ==========================================
        return InboxMessage(
            systemKey = systemKey,
            packageName = sbn.packageName,
            postTime = sbn.postTime,
            title = rawTitle.ifEmpty { extractedSenderName },
            content = text,
            attachmentType = attachmentType,
            rawDataLog = rawLog,
            messageType = messageType,
            isMentioned = isMentioned,
            groupName = extractedGroupName,
            senderName = extractedSenderName
        )
    }

    private fun dumpExtras(packageName: String, extras: Bundle): String {
        val builder = StringBuilder().append("--- [$packageName 原始包裹] ---\n")
        for (key in extras.keySet()) {
            builder.append("$key -> ${extras.get(key)}\n")
        }
        return builder.toString()
    }
}