package com.example.inboxzero.core.notificationlistening.scheduler

import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.inboxzero.core.notificationlistening.parser.ParserFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationProcessScheduler {

    // 创建一个专属的后台协程作用域，用于执行所有耗时任务
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * 唯一入口：接收原始通知并开启完整的处理生命周期
     */
    fun processNotification(sbn: StatusBarNotification) {
        // 立刻将任务丢进后台线程，绝不阻塞前台的 Service
        scope.launch {
            try {
                // 1. 找人：向工厂申请对应的解析器
                val parser = ParserFactory.getParser(sbn.packageName)

                // 2. 干活：执行解析，拿到标准化的数据容器
                val cleanMessage = parser.parse(sbn)

                // 3. 终端输出：打印结果（未来在这里对接 Database Repository）
                Log.i("InboxZero_Scheduler", """
                    后台处理流水线执行完毕，准备入库:
                    [App] ${cleanMessage.packageName}
                    [Title] ${cleanMessage.title}
                    [Content] ${cleanMessage.content}
                """.trimIndent())

            } catch (e: Exception) {
                Log.e("InboxZero_Scheduler", "通知处理流水线发生异常: ${e.message}")
            }
        }
    }
}