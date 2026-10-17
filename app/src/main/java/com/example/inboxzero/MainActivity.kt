package com.example.inboxzero

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.example.inboxzero.mocksender.core.NotificationDispatcher
import com.example.inboxzero.mocksender.worker.AutoBurstWorker

class MainActivity : AppCompatActivity() {

    // 声明我们的“通知发射枪管”与“自动化连发机械臂”
    private lateinit var dispatcher: NotificationDispatcher
    private lateinit var burstWorker: AutoBurstWorker

    // 整个 App 启动后真正执行的第一站（相当于 main 函数）
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 初始化发射器与自动连发调度器（把枪管交给机械臂）
        dispatcher = NotificationDispatcher(this)
        burstWorker = AutoBurstWorker(dispatcher)

        // 2. 检查并弹窗申请 Android 13+ 的发送通知权限
        checkAndRequestNotificationPermission()

        // 3. 用纯代码搭一个极简的毛坯房测试界面（垂直排列的线性布局）
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(80, 120, 80, 80)
        }

        // 标题说明文字
        val titleText = TextView(this).apply {
            text = "InBoxZero 通知发射测试台"
            textSize = 20f
            setPadding(0, 0, 0, 60)
        }
        rootLayout.addView(titleText)

        // 【按钮 1】：测试单条发送
        val btnSendSingle = Button(this).apply {
            text = "1. 发送单条假微信（导师催稿）"
            setOnClickListener {
                dispatcher.sendSingle(
                    sender = "导师",
                    content = "下周把那篇 HCI 论文的实验设计初稿发给我看一下。"
                )
                Toast.makeText(this@MainActivity, "已发射单条通知！请看顶部状态栏", Toast.LENGTH_SHORT).show()
            }
        }
        rootLayout.addView(btnSendSingle)

        // 【按钮 2】：测试微信多条折叠通知
        val btnSendFolded = Button(this).apply {
            text = "2. 发送折叠汇总通知（模拟群聊刷屏）"
            setOnClickListener {
                val mockGroupMessages = listOf(
                    "张三: 收到，马上改",
                    "李四: @王五 你的实验数据跑完了吗？",
                    "王五: 还在调参，今晚发群里",
                    "赵六: +1",
                    "导师: 大家抓紧进度，注意截稿日期"
                )
                dispatcher.sendFoldedSummary(
                    groupName = "HCI 毕业设计讨论群",
                    messages = mockGroupMessages
                )
                Toast.makeText(this@MainActivity, "已发射折叠通知！请下拉状态栏展开查看", Toast.LENGTH_SHORT).show()
            }
        }
        rootLayout.addView(btnSendFolded)

        // --- 分割提示：自动化连续发送测试区 ---
        val sectionTitle = TextView(this).apply {
            text = "—— 自动化压力测试区 ——"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 60, 0, 20)
        }
        rootLayout.addView(sectionTitle)

        // 【模式切换开关 Toggle Switch】：选择递增模式还是语料库模式
        val modeSwitch = SwitchCompat(this).apply {
            text = "当前：序号递增模式 (测试消息 1, 2...)"
            isChecked = false // 默认关闭（即使用序号递增模式）
            setPadding(0, 20, 0, 20)
            // 当你拨动开关时，动态更新旁边的提示文字
            setOnCheckedChangeListener { _, isChecked ->
                text = if (isChecked) {
                    "当前：语料库随机模式 (模拟真实微信)"
                } else {
                    "当前：序号递增模式 (测试消息 1, 2...)"
                }
            }
        }
        rootLayout.addView(modeSwitch)

        // 【按钮 3】：共用开始/停止的连续发送按钮（状态机逻辑）
        val btnToggleBurst = Button(this).apply {
            text = "▶ 开始连续发送"
            setOnClickListener {
                if (burstWorker.isRunning) {
                    // 状态 A：如果正在跑 -> 紧急刹车停止
                    burstWorker.stopBurst()
                    text = "▶ 开始连续发送"
                    modeSwitch.isEnabled = true // 解锁上方的模式开关
                    Toast.makeText(this@MainActivity, "已停止连续发送", Toast.LENGTH_SHORT).show()
                } else {
                    // 状态 B：如果没在跑 -> 读取开关状态并点火启动
                    val useCorpus = modeSwitch.isChecked
                    burstWorker.startBurst(useCorpusMode = useCorpus, intervalMs = 800L)
                    text = "⏹ 停止连续发送 (轰炸中...)"
                    modeSwitch.isEnabled = false // 轰炸期间锁定开关，防止中途误触切换
                    Toast.makeText(this@MainActivity, "开始自动连发！每 0.8 秒一条", Toast.LENGTH_SHORT).show()
                }
            }
        }
        rootLayout.addView(btnToggleBurst)

        // 【按钮 4】：一键清空所有测试通知（同时重置递增序号）
        val btnClear = Button(this).apply {
            text = "清空状态栏通知 & 重置序号"
            setOnClickListener {
                // 如果还在连发，顺手先帮用户停掉
                if (burstWorker.isRunning) {
                    burstWorker.stopBurst()
                    btnToggleBurst.text = "▶ 开始连续发送"
                    modeSwitch.isEnabled = true
                }
                dispatcher.clearAll()
                burstWorker.resetCounter()
                Toast.makeText(this@MainActivity, "状态栏已清空，递增序号已归零", Toast.LENGTH_SHORT).show()
            }
        }
        rootLayout.addView(btnClear)

        // 把画好的毛坯房界面挂载到手机屏幕上
        setContentView(rootLayout)
    }

    /**
     * 生命周期安全保障：当窗口被销毁（比如退出 App）时，强制关闭后台协程，绝不内存泄漏
     */
    override fun onDestroy() {
        super.onDestroy()
        burstWorker.stopBurst()
    }

    /**
     * 专门应对高版本 Android (API 33+) 的运行时通知权限弹窗申请
     */
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }
}