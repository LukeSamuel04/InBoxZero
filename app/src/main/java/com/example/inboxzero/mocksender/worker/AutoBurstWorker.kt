package com.example.inboxzero.mocksender.worker

import com.example.inboxzero.mocksender.core.NotificationDispatcher
import com.example.inboxzero.mocksender.data.CORPUS_MESSAGES
import com.example.inboxzero.mocksender.data.MockMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class BurstMode {
    NUMBERED,          // 1. 无情复读机：发送 "测试消息 (1)", "测试消息 (2)"... (需要手动停止)
    CORPUS_SEQUENTIAL, // 2. 剧本模式：严格按照语料库顺序发，发完所有条目后自动停止
    CORPUS_REALISTIC   // 3. 拟真随机：剧情按步骤推进，噪音随机穿插，所有语料发完一遍后自动停止
}

class AutoBurstWorker(private val dispatcher: NotificationDispatcher) {

    private var burstJob: Job? = null
    private val workerScope = CoroutineScope(Dispatchers.Default)

    // ==========================================
    // 进度与状态追踪器 (荷官的记忆)
    // ==========================================
    private var sequenceCounter = 0
    private var sequentialIndex = 0

    // 记录每个连续剧进行到了第几步
    private val threadProgressTracker = mutableMapOf<String, Int>()

    // 【全新设计：独立抽牌堆】初始化时，将所有无剧情的“噪音”装进这个可变列表
    private val noiseDrawPile = CORPUS_MESSAGES
        .filter { it.threadId == null }
        .toMutableList()

    val isRunning: Boolean
        get() = burstJob?.isActive == true

    /**
     * 启动连发
     * @param onProgress 进度汇报对讲机 (每发一条汇报一次)
     * @param onFinish 杀青汇报对讲机 (剧本播完时呼叫，用于重置 UI 状态)
     */
    fun startBurst(
        mode: BurstMode,
        intervalMs: Long = 800L,
        onProgress: (MockMessage) -> Unit,
        onFinish: () -> Unit
    ) {
        if (isRunning) return

        burstJob = workerScope.launch {
            while (isActive) {
                // 1. 获取下一条消息。如果返回 null，说明剧本或整个抽牌堆播完了
                val msgToSend: MockMessage? = when (mode) {
                    BurstMode.NUMBERED -> {
                        sequenceCounter++
                        MockMessage(
                            appName = "系统测试",
                            sender = "压力测试机器人",
                            content = "测试消息 ($sequenceCounter)"
                        )
                    }
                    BurstMode.CORPUS_SEQUENTIAL -> {
                        if (sequentialIndex >= CORPUS_MESSAGES.size) {
                            null // 索引越界，说明全部发完了，返回 null 触发停止
                        } else {
                            val msg = CORPUS_MESSAGES[sequentialIndex]
                            sequentialIndex++
                            msg
                        }
                    }
                    BurstMode.CORPUS_REALISTIC -> {
                        calculateNextRealisticMessage()
                    }
                }

                // 2. 终止判定：如果没有子弹了，执行杀青逻辑
                if (msgToSend == null) {
                    launch(Dispatchers.Main) { onFinish() } // 告诉 UI 播完了
                    stopBurst() // 切断电源
                    break // 跳出死循环
                }

                // 3. 扣动扳机发出通知
                val displayTitle = msgToSend.groupName ?: msgToSend.sender
                val displayContent = if (msgToSend.groupName != null) {
                    "${msgToSend.sender}: ${msgToSend.content}"
                } else {
                    msgToSend.content
                }
                dispatcher.sendSingle(sender = displayTitle, content = displayContent)

                // 4. 向 UI 汇报当前发送的消息
                onProgress(msgToSend)

                delay(intervalMs)
            }
        }
    }

    /**
     * 高性能算法：拟真随机发牌逻辑
     * @return 下一条要发的消息。如果整个语料库（包括剧情和噪音）都发完了，返回 null。
     */
    private fun calculateNextRealisticMessage(): MockMessage? {
        val eligibleMessages = mutableListOf<MockMessage>()

        // 步骤 A：找出所有还有下一步的“剧情牌”
        val allThreadIds = CORPUS_MESSAGES.mapNotNull { it.threadId }.distinct()
        for (tId in allThreadIds) {
            val expectedStep = threadProgressTracker.getOrDefault(tId, 1)
            val nextMsg = CORPUS_MESSAGES.find { it.threadId == tId && it.step == expectedStep }
            if (nextMsg != null) {
                eligibleMessages.add(nextMsg)
            }
        }

        // 步骤 B：直接将“噪音抽牌堆”中剩下的牌全部倒进候选池
        eligibleMessages.addAll(noiseDrawPile)

        // 步骤 C：杀青判定
        if (eligibleMessages.isEmpty()) {
            return null // 剧情走完且抽牌堆空了，触发整体停止
        }

        // 步骤 D：盲盒抽取
        val pickedMsg = eligibleMessages.random()

        // 步骤 E：状态更新 (物理销毁或推进剧情)
        if (pickedMsg.threadId != null) {
            // 抽中的是剧情，推进该剧情的进度
            threadProgressTracker[pickedMsg.threadId] = pickedMsg.step + 1
        } else {
            // 【核心优化】：抽中的是噪音，直接从抽牌堆里物理撕毁销毁，再也不会被抽到
            noiseDrawPile.removeAll { it.id == pickedMsg.id }
        }

        return pickedMsg
    }

    fun stopBurst() {
        burstJob?.cancel()
        burstJob = null
    }

    /**
     * 重置所有状态，换一副新牌
     */
    fun resetCounters() {
        sequenceCounter = 0
        sequentialIndex = 0
        threadProgressTracker.clear()

        // 洗牌：把全新的噪音牌重新装进抽牌堆
        noiseDrawPile.clear()
        noiseDrawPile.addAll(CORPUS_MESSAGES.filter { it.threadId == null })
    }
}