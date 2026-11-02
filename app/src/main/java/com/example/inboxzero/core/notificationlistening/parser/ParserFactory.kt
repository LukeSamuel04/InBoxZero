package com.example.inboxzero.core.notificationlistening.parser

import com.example.inboxzero.core.notificationlistening.parser.implementations.DefaultParser

/**
 * 分发工厂：路由枢纽
 * 负责根据通知的包名，将原始通知分发给对应的专职解析器。
 */
object ParserFactory {
    // 预先实例化各个解析器，避免每次收到通知都重复创建对象，节省内存
    private val defaultParser = DefaultParser()

    fun getParser(packageName: String): NotificationParser {
        return when (packageName) {
            else -> defaultParser
        }
    }
}