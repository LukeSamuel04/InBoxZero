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
        val title = extras.getString(Notification.EXTRA_TITLE) ?: "未知发件人"
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "无正文"

        val rawLog = dumpExtras(sbn.packageName, extras)

        return InboxMessage(sbn.packageName, sbn.postTime, title, text, rawLog)
    }

    private fun dumpExtras(packageName: String, extras: Bundle): String {
        val builder = StringBuilder().append("--- [$packageName 原始包裹] ---\n")
        for (key in extras.keySet()) {
            builder.append("$key -> ${extras.get(key)}\n")
        }
        return builder.toString()
    }
}