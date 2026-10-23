package com.example.mockmessagetester.mocksender.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * 通知发射核心 (Core)：负责持有系统 Context 通行证，向 Android 状态栏投递通知
 */
class NotificationDispatcher(private val context: Context) {

    // 1. 内部持有的状态：系统通知大管家与递增的通知 ID
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var currentNotificationId = 1000

    companion object {
        private const val CHANNEL_ID = "mock_wechat_channel"
        private const val CHANNEL_NAME = "模拟微信消息通道"
        private const val GROUP_KEY_WECHAT = "com.example.inboxzero.WECHAT_GROUP"
    }

    // 2. 初始化代码块（相当于 Python 的 __init__）：造出这个对象时，自动向系统注册渠道
    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 设置为 IMPORTANCE_HIGH，确保通知不仅在状态栏有图标，还会在屏幕顶部弹出横幅 (Heads-up)
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "用于 HCI 实验原型测试的假消息发射通道"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 核心动作 1：发射单条模拟通知
     * @param sender 发送者或群聊名称（如 "导师" 或 "毕设讨论群"）
     * @param content 消息正文（如 "下周把论文初稿发我"）
     */
    fun sendSingle(sender: String, content: String) {
        currentNotificationId++ // 每次发送自增 ID，确保不会覆盖上一条通知

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email) // 借用系统自带的信封图标
            .setContentTitle(sender)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(GROUP_KEY_WECHAT) // 归入同一个组，连发多条时系统会自动折叠它们
            .setAutoCancel(true)        // 点击后自动消失
            .build()

        // 扣动扳机：投递给安卓系统
        notificationManager.notify(currentNotificationId, notification)
    }

    /**
     * 核心动作 2：专门模拟“微信多条折叠”状态的汇总通知 (InboxStyle)
     * 用于后续专门测试你的正则表达式清洗管道与折叠拆包能力
     */
    fun sendFoldedSummary(groupName: String, messages: List<String>) {
        currentNotificationId++

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("$groupName (${messages.size}条新消息)")
            .setSummaryText("微信模拟折叠")

        // 把多条消息一行行塞进折叠包裹的底层数据结构 (textLines) 中
        for (msg in messages) {
            inboxStyle.addLine(msg)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(groupName)
            .setContentText("[${messages.size}条] ${messages.lastOrNull() ?: ""}") // 折叠时只显示最后一条
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(GROUP_KEY_WECHAT)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(currentNotificationId, notification)
    }

    /**
     * 辅助动作：一键清空状态栏里发出的所有测试通知
     */
    fun clearAll() {
        notificationManager.cancelAll()
    }
}