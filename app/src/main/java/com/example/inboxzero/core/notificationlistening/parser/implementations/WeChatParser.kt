package com.example.inboxzero.core.notificationlistening.parser.implementations

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.inboxzero.core.notificationlistening.parser.NotificationParser
import com.example.inboxzero.data.model.InboxMessage

class WeChatParser : NotificationParser {
    override fun parse(sbn: StatusBarNotification): InboxMessage {
        Log.d("InboxZero_Parser", "🟢 流量进入 [WeChatParser] 专属通道！")

        val extras = sbn.notification.extras
        // TODO: 未来在这里编写专门针对微信群名、发件人的正则拆解逻辑
        val rawTitle = extras.getString(Notification.EXTRA_TITLE) ?: "微信未知"
        val rawText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        val rawLog = dumpExtras(sbn.packageName, extras)

        return InboxMessage(sbn.packageName, sbn.postTime, rawTitle, rawText, rawLog)
    }

    private fun dumpExtras(packageName: String, extras: Bundle): String {
        val builder = StringBuilder().append("--- [$packageName 原始包裹] ---\n")
        for (key in extras.keySet()) {
            builder.append("$key -> ${extras.get(key)}\n")
        }
        return builder.toString()
    }
}