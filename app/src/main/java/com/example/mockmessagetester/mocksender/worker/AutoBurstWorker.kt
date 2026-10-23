package com.example.mockmessagetester.mocksender.worker

import com.example.mockmessagetester.mocksender.core.NotificationDispatcher
import com.example.mockmessagetester.mocksender.data.MockMessage
import com.example.mockmessagetester.mocksender.data.MockScenario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AutoBurstWorker(private val dispatcher: NotificationDispatcher) {

    private var burstJob: Job? = null
    private val workerScope = CoroutineScope(Dispatchers.Default)

    private var repeatCounter = 0
    private var burstStartTime = 0L // 相对时间戳锚点

    val isRunning: Boolean
        get() = burstJob?.isActive == true

    fun startRepeatMode(
        targetMessage: MockMessage,
        intervalMs: Long,
        onProgress: (MockMessage) -> Unit
    ) {
        if (isRunning) return
        burstStartTime = System.currentTimeMillis()

        burstJob = workerScope.launch {
            while (isActive) {
                repeatCounter++
                val relativeMs = System.currentTimeMillis() - burstStartTime
                val displayContent = "${targetMessage.content} ($repeatCounter) @${relativeMs}ms"

                dispatcher.sendSingle(
                    sender = targetMessage.sender,
                    content = displayContent
                )

                val sentMsg = targetMessage.copy(content = displayContent)
                onProgress(sentMsg)

                delay(intervalMs)
            }
        }
    }

    fun startScriptMode(
        scenario: MockScenario,
        onProgress: (MockMessage) -> Unit,
        onFinish: () -> Unit
    ) {
        if (isRunning) return

        burstJob = workerScope.launch {
            for (msg in scenario.messages) {
                if (!isActive) break

                val displayTitle = msg.groupName ?: msg.sender
                val displayContent = if (msg.groupName != null) {
                    "${msg.sender}: ${msg.content}"
                } else {
                    msg.content
                }

                dispatcher.sendSingle(sender = displayTitle, content = displayContent)
                onProgress(msg)
                delay(msg.delayAfterMs)
            }

            if (isActive) {
                launch(Dispatchers.Main) { onFinish() }
                stopBurst()
            }
        }
    }

    fun stopBurst() {
        burstJob?.cancel()
        burstJob = null
    }

    fun resetCounters() {
        repeatCounter = 0
        burstStartTime = 0L
    }
}