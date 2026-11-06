package com.example.inboxzero.core.notificationlistening.parser

import android.service.notification.StatusBarNotification
import com.example.inboxzero.data.model.InboxMessage

/**
 * 解析器接口定义
 * 这是一个“行为契约”，规定了所有具体的 Parser 都必须实现 parse 方法
 */
interface NotificationParser {
    fun parse(sbn: StatusBarNotification): InboxMessage
}