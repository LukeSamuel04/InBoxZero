package com.example.inboxzero

import android.app.Application
import com.example.inboxzero.core.notificationlistening.scheduler.NotificationProcessScheduler
import com.example.inboxzero.data.local.AppDatabase
import com.example.inboxzero.data.local.UserPreferences
import com.example.inboxzero.data.repository.MessageRepository

class InboxZeroApplication : Application() {

    // 1. 提升为全局可访问的公开单例属性 (懒加载，传入 Application Context 绝不内存泄漏)
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: MessageRepository by lazy { MessageRepository(database.messageDao()) }

    val userPreferences: UserPreferences by lazy { UserPreferences(this) }

    override fun onCreate() {
        super.onCreate()

        // 2. 将大管家注入到 Scheduler 的插槽中，打通流水线
        NotificationProcessScheduler.messageRepository = repository
    }
}