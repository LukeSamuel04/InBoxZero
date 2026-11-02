package com.example.inboxzero

import android.app.Application
import com.example.inboxzero.core.notificationlistening.scheduler.NotificationProcessScheduler
import com.example.inboxzero.data.local.AppDatabase
import com.example.inboxzero.data.repository.MessageRepository

class InboxZeroApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. 初始化底层数据库 (传入 Application 级别的 Context，绝对防止内存泄漏)
        val database = AppDatabase.getDatabase(this)

        // 2. 实例化大管家 (传入 DAO 遥控器)
        val repository = MessageRepository(database.messageDao())

        // 3. 将大管家注入到 Scheduler 的插槽中，打通流水线
        NotificationProcessScheduler.messageRepository = repository
    }
}