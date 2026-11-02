package com.example.inboxzero.core.notificationlistening.parser

import com.example.inboxzero.core.notificationlistening.parser.implementations.DefaultParser
import com.example.inboxzero.core.notificationlistening.parser.implementations.MockSenderParser
import com.example.inboxzero.core.notificationlistening.parser.implementations.WeChatParser

/**
 * 分发工厂：路由枢纽
 * 负责根据通知的包名，将原始通知分发给对应的专职解析器。
 */
object ParserFactory {
    // 预先实例化各个解析器，避免每次收到通知都重复创建对象，节省内存
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