package com.example.inboxzero.core.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

// =========================================================================
// 1. 数据实体：所有消息的最终归宿
// =========================================================================
data class InboxMessage(
    val packageName: String,
    val postTime: Long,
    val title: String,
    val content: String,
    val rawDataLog: String = ""
)

// =========================================================================
// 2. 解析器接口定义
// =========================================================================
interface NotificationParser {
    fun parse(sbn: StatusBarNotification): InboxMessage
}

// =========================================================================
// 3. 各大专属解析器 (侦察兵/沙盒)
// =========================================================================

/**
 * 默认兜底解析器：处理未知 App，负责全盘扫描并打印 Bundle
 */
class DefaultParser : NotificationParser {
    override fun parse(sbn: StatusBarNotification): InboxMessage {
        Log.d("InboxZero_Parser", "🔀 流量进入 [DefaultParser] 默认通道")

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: "未知发件人"
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "无正文"

        // 打印原始数据，供调试用
        val rawLog = dumpExtras(sbn.packageName, extras)

        return InboxMessage(sbn.packageName, sbn.postTime, title, text, rawLog)
    }
}

/**
 * 微信专属解析器 (待填坑)
 */
class WeChatParser : NotificationParser {
    override fun parse(sbn: StatusBarNotification): InboxMessage {
        Log.d("InboxZero_Parser", "🟢 流量进入 [WeChatParser] 专属通道！")

        val extras = sbn.notification.extras
        // TODO: 未来在这里编写专门针对微信群名、发件人的正则拆解逻辑
        // 暂时先用默认方式提取，保证程序不崩
        val rawTitle = extras.getString(Notification.EXTRA_TITLE) ?: "微信未知"
        val rawText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        val rawLog = dumpExtras(sbn.packageName, extras)

        return InboxMessage(sbn.packageName, sbn.postTime, rawTitle, rawText, rawLog)
    }
}

/**
 * Mock Sender 专属测试解析器
 * 用于验证我们自己写的高频轰炸机发出的消息能否被精准路由
 */
class MockSenderParser : NotificationParser {
    override fun parse(sbn: StatusBarNotification): InboxMessage {
        Log.d("InboxZero_Parser", "🛠️ 流量进入 [MockSenderParser] 测试通道！")

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: "测试发件人"
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "测试内容"

        val rawLog = dumpExtras(sbn.packageName, extras)

        return InboxMessage(sbn.packageName, sbn.postTime, title, text, rawLog)
    }
}

// 辅助提取所有 Bundle 信息的纯函数
private fun dumpExtras(packageName: String, extras: android.os.Bundle): String {
    val builder = StringBuilder().append("--- [$packageName 原始包裹] ---\n")
    for (key in extras.keySet()) {
        builder.append("$key -> ${extras.get(key)}\n")
    }
    return builder.toString()
}

// =========================================================================
// 4. 分发工厂：路由枢纽
// =========================================================================
object ParserFactory {
    private val defaultParser = DefaultParser()
    private val wechatParser = WeChatParser()
    private val mockSenderParser = MockSenderParser()

    fun getParser(packageName: String): NotificationParser {
        return when (packageName) {
            "com.tencent.mm" -> wechatParser
            "com.example.mockmessagetester" -> mockSenderParser // 精准捕获你的测试 App
            else -> defaultParser
        }
    }
}

// =========================================================================
// 5. 核心系统服务：门卫
// =========================================================================
class NotificationInterceptService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i("InboxZero_Service", "✅ 通知拦截服务已成功连接，雷达开启！")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // 过滤系统底层通知
        if (sbn.packageName == "android" || sbn.packageName == "com.android.systemui") return

        // 1. 获取专属解析器
        val parser = ParserFactory.getParser(sbn.packageName)

        // 2. 解析出统一数据结构
        val cleanMessage = parser.parse(sbn)

        // 3. 打印入库前确认信息与原始载荷
        Log.i("InboxZero_Result", """
            拦截并清洗成功，准备入库:
            [App] ${cleanMessage.packageName}
            [Title] ${cleanMessage.title}
            [Content] ${cleanMessage.content}
            [Raw Data]
            ${cleanMessage.rawDataLog}
        """.trimIndent())
    }
}