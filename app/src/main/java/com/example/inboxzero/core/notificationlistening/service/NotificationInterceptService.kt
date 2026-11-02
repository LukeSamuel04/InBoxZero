package com.example.inboxzero.core.notificationlistening.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.inboxzero.core.notificationlistening.scheduler.NotificationProcessScheduler

class NotificationInterceptService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i("InboxZero_Service", "✅ 通知拦截服务已成功连接，前台门卫就绪！")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // 过滤系统底层无用通知
        if (sbn.packageName == "android" || sbn.packageName == "com.android.systemui") return

        // 门卫的唯一工作：把包裹扔给后台调度中心，自己立刻转头去接下一个通知
        NotificationProcessScheduler.processNotification(sbn)
    }
}