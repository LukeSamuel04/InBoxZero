package com.example.inboxzero.core.notificationlistening.scheduler

import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.inboxzero.core.notificationlistening.parser.ParserFactory
import com.example.inboxzero.data.repository.MessageRepository // 提前为你未来导入大管家留好位置

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationProcessScheduler {

    // 创建一个专属的后台协程作用域，用于执行所有耗时任务
    private val scope = CoroutineScope(Dispatchers.IO)

    var messageRepository: MessageRepository? = null

    /**
     * 唯一入口：接收原始通知并开启完整的处理生命周期
     */
    fun processNotification(sbn: StatusBarNotification) {
        // 立刻将任务丢进后台线程，绝不阻塞前台的 Service[cite: 14]
        scope.launch {
            try {
                // 1. 找人：向工厂申请对应的解析器[cite: 14]
                val parser = ParserFactory.getParser(sbn.packageName)

                // 2. 干活：执行解析，拿到标准化的数据容器[cite: 14]
                val cleanMessage = parser.parse(sbn)

                // 3. 终端输出：验证所有高级业务字段是否提取成功
                Log.i("InboxZero_Scheduler", """
                    📦 完美包裹清洗完毕，准备交接给大管家:
                    [App] ${cleanMessage.packageName}
                    [Type] ${cleanMessage.messageType} (被@: ${cleanMessage.isMentioned})
                    [Sender] ${cleanMessage.senderName}
                    [Group] ${cleanMessage.groupName ?: "无"}
                    [Title] ${cleanMessage.title}
                    [Content] ${cleanMessage.content}
                    [Attachment] ${cleanMessage.attachmentType}
                """.trimIndent())

                // 4. 终极一跃：呼叫大管家入库
                messageRepository?.insertMessage(cleanMessage)

            } catch (e: Exception) {
                Log.e("InboxZero_Scheduler", "通知处理流水线发生异常: ${e.message}") //[cite: 14]
            }
        }
    }
}