package com.example.inboxzero.mocksender.worker

import com.example.inboxzero.mocksender.core.NotificationDispatcher
import com.example.inboxzero.mocksender.data.getRandomCorpusMessage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 自动化连发调度器 (Worker)：握着发射枪管 (dispatcher)，利用协程在后台按节奏自动扣扳机
 */
class AutoBurstWorker(private val dispatcher: NotificationDispatcher) {

    // 1. 协程遥控器 (Job)：用来随时按下“急刹车”
    private var burstJob: Job? = null

    // 2. 创建专门用于后台任务的协程车间 (不会卡顿主界面)
    private val workerScope = CoroutineScope(Dispatchers.Default)

    // 3. 序号递增模式的计数器：用来记录发到了“测试消息 (第几条)”
    private var sequenceCounter = 0

    // 4. 对外公开的只读状态：判断当前是不是正在突突突连发中
    val isRunning: Boolean
        get() = burstJob?.isActive == true

    /**
     * 启动无限连发
     * @param useCorpusMode true = 随机抽语料库模式；false = 序号递增模式 (测试消息 1, 2, 3...)
     * @param intervalMs 每次发射的间隔毫秒数（默认 800 毫秒发一条，方便肉眼观察）
     */
    fun startBurst(useCorpusMode: Boolean, intervalMs: Long = 800L) {
        // 防呆保护：如果已经在运行中，直接忽略，防止重复开启多个协程
        if (isRunning) return

        // 在后台车间启动一个协程任务，并把遥控器交给 burstJob 保管
        burstJob = workerScope.launch {
            // isActive 是协程自带的智能开关：只要没人按 stop，它就永远是 true
            while (isActive) {
                if (useCorpusMode) {
                    // 【模式 A：语料库模式】直接调用刚才 MockCorpus.kt 里的纯函数！
                    val msg = getRandomCorpusMessage()
                    dispatcher.sendSingle(sender = msg.sender, content = msg.content)
                } else {
                    // 【模式 B：序号递增模式】用于测试丢包率与顺序一致性
                    sequenceCounter++
                    dispatcher.sendSingle(
                        sender = "压力测试机器人",
                        content = "测试消息 ($sequenceCounter)"
                    )
                }

                // 挂起等待 800 毫秒（非阻塞式暂停，绝不卡死手机屏幕）
                delay(intervalMs)
            }
        }
    }

    /**
     * 紧急停止连发（相当于按下暂停键）
     */
    fun stopBurst() {
        burstJob?.cancel() // 一键掐断协程电源
        burstJob = null
    }

    /**
     * 辅助功能：重置递增计数器（比如清空通知时顺便把序号归零）
     */
    fun resetCounter() {
        sequenceCounter = 0
    }
}