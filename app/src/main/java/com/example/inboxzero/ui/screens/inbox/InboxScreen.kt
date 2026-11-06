package com.example.inboxzero.ui.screens.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inboxzero.data.local.UserPreferences
import com.example.inboxzero.data.model.InboxMessage
import com.example.inboxzero.data.model.InboxSession
import com.example.inboxzero.ui.components.containers.CollapsibleContainer
import com.example.inboxzero.ui.components.containers.SecondaryContainer
import com.example.inboxzero.ui.components.inbox.InboxSessionCard
import com.example.inboxzero.ui.components.inbox.InboxStatusHeader
import com.example.inboxzero.ui.theme.LocalAppStrings
import com.example.inboxzero.ui.theme.MorandiGreen
import kotlinx.coroutines.delay

/**
 * InboxZero 首页智能防焦虑看板 (Tab 0: Inbox Screen)
 *
 * 核心架构与组件复用设计：
 * 1. 极致组件复用：高优核心区与底部杂音收纳舱 100% 共用同一个 [InboxSessionCard]；
 *    底部杂音收纳抽屉直接复用全站通用的 [CollapsibleContainer]，彻底消除重复代码 (Zero Code Smell)。
 * 2. 实验参数实时联动：自动监听 [UserPreferences] 的 AI 引擎开关 (实验组 vs Baseline 对照组)、自动折叠开关与过滤阈值档位。
 * 3. 两阶段乐观 UI 演示：点击顶部 [+ 模拟来信]，新消息 0ms 瞬时上屏呈现“✦ AI 解析中...”呼吸态，1.8s 后自动回填评分与摘要。
 */
@Composable
fun InboxScreen(
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    // --- 1. 监听设置页的实验组/对照组持久化配置 (100% 对齐 UserPreferences.kt) ---
    val isAiEnabled by userPreferences.isAiEngineEnabled.collectAsState()
    val isAutoFoldEnabled by userPreferences.isAutoFoldNoise.collectAsState()
    val thresholdIndex by userPreferences.thresholdIndex.collectAsState()

    val thresholdScore = when (thresholdIndex) {
        0 -> 20
        2 -> 60
        else -> 40
    }

    // --- 2. 页面交互状态 ---
    var expandedSessionId by rememberSaveable { mutableStateOf<String?>(null) }
    var isNoiseVaultExpanded by rememberSaveable { mutableStateOf(false) }
    var simulationCounter by rememberSaveable { mutableIntStateOf(0) }

    // --- 3. 初始会话演示数据集 (严格遵循 data.model.InboxSession 与 InboxMessage 契约) ---
    val sessionList = remember {
        mutableStateListOf<InboxSession>().apply {
            addAll(createInitialDemoSessions())
        }
    }

    // 处理模拟新消息到来后的 1.8 秒 AI 异步评分与摘要回填
    val analyzingSessions = sessionList.filter { it.isAnalyzing }
    LaunchedEffect(analyzingSessions.size, simulationCounter) {
        if (analyzingSessions.isNotEmpty()) {
            delay(1800L)
            val updated = sessionList.map { session ->
                if (session.isAnalyzing) {
                    session.copy(
                        isAnalyzing = false,
                        urgencyScore = 92,
                        aiSummary = "紧急确认：下午 3 点用户实验设备已就绪，请带好实验知情同意书"
                    )
                } else {
                    session
                }
            }
            sessionList.clear()
            sessionList.addAll(updated)
        }
    }

    // --- 4. 根据当前实验模式 (AI 开启 vs Baseline) 与阈值，动态分流主看板与低优杂音 ---
    val (focusSessions, foldedNoiseSessions) = remember(
        sessionList.toList(),
        isAiEnabled,
        isAutoFoldEnabled,
        thresholdScore
    ) {
        if (!isAiEnabled) {
            // Baseline 对照组模式：不进行 AI 评分排序与折叠，全部按最新时间戳倒序平铺
            sessionList.sortedByDescending { it.latestPostTime } to emptyList()
        } else {
            // AI 实验组模式：正在解析的新消息置顶，其余按紧急度分数降序
            val sorted = sessionList.sortedWith(
                compareByDescending<InboxSession> { it.isAnalyzing }
                    .thenByDescending { it.urgencyScore }
                    .thenByDescending { it.latestPostTime }
            )
            if (isAutoFoldEnabled) {
                val focus = sorted.filter { it.isAnalyzing || it.urgencyScore >= thresholdScore }
                val noise = sorted.filter { !it.isAnalyzing && it.urgencyScore < thresholdScore }
                focus to noise
            } else {
                // 关闭了“自动折叠低优杂音”：全部留在主列表，仅按评分高低排序
                sorted to emptyList()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // =====================================================================
        // 模块一：顶部态势感知与模拟注入条
        // =====================================================================
        InboxStatusHeader(
            isAiEngineEnabled = isAiEnabled,
            currentThreshold = thresholdScore,
            onSimulateMessageClick = {
                simulationCounter++
                val now = System.currentTimeMillis()
                val newId = "sim_session_$simulationCounter"
                val newMsg = InboxMessage(
                    systemKey = "key_$now",
                    packageName = "com.tencent.mm",
                    postTime = now,
                    title = "导师 · 李教授",
                    content = "下午 3 点的 HCI 组会和受试者设备已经调试好了，记得把知情同意书打印带过来！",
                    attachmentType = 0,
                    messageType = 1,
                    isMentioned = true,
                    senderName = "导师 · 李教授"
                )
                // 0ms 乐观 UI 插入列表顶部 (isAnalyzing = true)
                sessionList.add(
                    0,
                    InboxSession(
                        sessionId = newId,
                        packageName = "com.tencent.mm",
                        appDisplayName = "微信",
                        latestPostTime = now,
                        messageType = 1,
                        isMentioned = true,
                        senderName = "导师 · 李教授",
                        isAnalyzing = true,
                        urgencyScore = 50,
                        aiSummary = null,
                        unreadCount = 1,
                        messages = listOf(newMsg),
                        canInlineReply = true,
                        smartReplies = listOf("收到，马上带过去！", "已打印好，准时到")
                    )
                )
            }
        )

        // =====================================================================
        // 模块二：主舞台核心会话列表 (高优与日常关注区)
        // =====================================================================
        Text(
            text = if (isAiEnabled) strings.inboxSectionFocus else strings.inboxSectionAllChrono,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        if (focusSessions.isEmpty()) {
            // 当所有高优会话处理完毕时，展示温和的 Inbox Zero 清零达成卡片 (复用 SecondaryContainer)
            SecondaryContainer(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = strings.inboxEmptyTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MorandiGreen,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = strings.inboxEmptySubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                focusSessions.forEach { session ->
                    InboxSessionCard(
                        session = session,
                        threshold = thresholdScore,
                        isExpanded = (expandedSessionId == session.sessionId),
                        onExpandChange = { expand ->
                            expandedSessionId = if (expand) session.sessionId else null
                        },
                        onSendInlineReply = { sessionId, _ ->
                            sessionList.removeAll { it.sessionId == sessionId }
                            if (expandedSessionId == sessionId) expandedSessionId = null
                        },
                        onMarkAsDone = { sessionId ->
                            sessionList.removeAll { it.sessionId == sessionId }
                            if (expandedSessionId == sessionId) expandedSessionId = null
                        },
                        isBaselineMode = !isAiEnabled
                    )
                }
            }
        }

        // =====================================================================
        // 模块三：底部低优杂音静默收纳舱 (直接复用 CollapsibleContainer + InboxSessionCard！)
        // =====================================================================
        if (foldedNoiseSessions.isNotEmpty()) {
            val safetyPeekNames = remember(foldedNoiseSessions, strings) {
                val topNames = foldedNoiseSessions.take(3).joinToString("、") { it.displayTitle }
                val suffix = if (foldedNoiseSessions.size > 3) " ..." else ""
                "${strings.inboxVaultPeekPrefix}$topNames$suffix"
            }

            Spacer(modifier = Modifier.height(4.dp))

            CollapsibleContainer(
                summaryLeftText = strings.inboxVaultTitle,
                summaryRightHighlight = "${strings.inboxVaultFoldedPrefix} ${foldedNoiseSessions.size} ${strings.inboxVaultFoldedSuffix}",
                summarySubtitle = safetyPeekNames,
                highlightColor = MorandiGreen,
                isExpanded = isNoiseVaultExpanded,
                onExpandChange = { isNoiseVaultExpanded = it }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 顶部工具行：[🧹 一键已读全部杂音]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MorandiGreen.copy(alpha = 0.14f))
                                .border(
                                    width = 0.8.dp,
                                    color = MorandiGreen.copy(alpha = 0.40f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    val noiseIds = foldedNoiseSessions.map { it.sessionId }.toSet()
                                    sessionList.removeAll { it.sessionId in noiseIds }
                                    isNoiseVaultExpanded = false
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = strings.inboxVaultBtnClearAll,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // 100% 复用同一个 InboxSessionCard 渲染被折叠的低优卡片！
                    foldedNoiseSessions.forEach { noiseSession ->
                        InboxSessionCard(
                            session = noiseSession,
                            threshold = thresholdScore,
                            isExpanded = (expandedSessionId == noiseSession.sessionId),
                            onExpandChange = { expand ->
                                expandedSessionId = if (expand) noiseSession.sessionId else null
                            },
                            onSendInlineReply = { sessionId, _ ->
                                sessionList.removeAll { it.sessionId == sessionId }
                                if (expandedSessionId == sessionId) expandedSessionId = null
                            },
                            onMarkAsDone = { sessionId ->
                                sessionList.removeAll { it.sessionId == sessionId }
                                if (expandedSessionId == sessionId) expandedSessionId = null
                            },
                            // 传入提权回调：点击 [↑ 移出杂音舱] 将该卡片分数提至 75 分回到主看板
                            onPromotePriority = { sessionId ->
                                val idx = sessionList.indexOfFirst { it.sessionId == sessionId }
                                if (idx != -1) {
                                    val current = sessionList[idx]
                                    sessionList[idx] = current.copy(
                                        urgencyScore = maxOf(thresholdScore + 15, 75)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}

/**
 * 构造符合 InboxSession 与 InboxMessage 契约的演示数据
 */
private fun createInitialDemoSessions(): List<InboxSession> {
    val now = System.currentTimeMillis()
    val min = 60_000L

    return listOf(
        // 1. 高优微信单聊 (88分)
        InboxSession(
            sessionId = "wx_xiaowang",
            packageName = "com.tencent.mm",
            appDisplayName = "微信",
            latestPostTime = now - 2 * min,
            messageType = 1,
            isMentioned = false,
            senderName = "小王 (前端组)",
            isAnalyzing = false,
            urgencyScore = 88,
            aiSummary = "询问统计页莫兰迪图表是否已合入主分支，并确认下午 3 点组会地点",
            unreadCount = 3,
            messages = listOf(
                InboxMessage(
                    systemKey = "k1",
                    packageName = "com.tencent.mm",
                    postTime = now - 5 * min,
                    title = "小王 (前端组)",
                    content = "在吗？统计页的折叠图表我刚拉下来看了一眼，巨丝滑！",
                    messageType = 1,
                    senderName = "小王"
                ),
                InboxMessage(
                    systemKey = "k2",
                    packageName = "com.tencent.mm",
                    postTime = now - 3 * min,
                    title = "小王 (前端组)",
                    content = "这部分代码你已经 git commit 合进主分支了吗？",
                    messageType = 1,
                    senderName = "小王"
                ),
                InboxMessage(
                    systemKey = "k3",
                    packageName = "com.tencent.mm",
                    postTime = now - 2 * min,
                    title = "小王 (前端组)",
                    content = "另外下午 3 点的组会还是在 302 实验室开吗？",
                    messageType = 1,
                    senderName = "小王"
                )
            ),
            canInlineReply = true,
            smartReplies = listOf("已合并，下午 302 见！", "合进去了，稍后同步你")
        ),

        // 2. 高优研发群聊 (带 @提及，82分)
        InboxSession(
            sessionId = "wx_core_group",
            packageName = "com.tencent.mm",
            appDisplayName = "微信",
            latestPostTime = now - 11 * min,
            messageType = 2,
            isMentioned = true,
            groupName = "InboxZero 毕设冲刺群 (6)",
            senderName = "张学长",
            isAnalyzing = false,
            urgencyScore = 82,
            aiSummary = "张学长发来 Room 数据库迁移脚本截图，@你确认字段是否对齐",
            unreadCount = 2,
            messages = listOf(
                InboxMessage(
                    systemKey = "k4",
                    packageName = "com.tencent.mm",
                    postTime = now - 12 * min,
                    title = "InboxZero 毕设冲刺群 (6)",
                    content = "我刚把 AiMetadata 表的迁移脚本写好了",
                    attachmentType = 1, // [图片]
                    messageType = 2,
                    groupName = "InboxZero 毕设冲刺群 (6)",
                    senderName = "张学长"
                ),
                InboxMessage(
                    systemKey = "k5",
                    packageName = "com.tencent.mm",
                    postTime = now - 11 * min,
                    title = "InboxZero 毕设冲刺群 (6)",
                    content = "@你 看看 InboxMessage 的 messageType 和 attachmentType 字段是不是都对齐了？",
                    messageType = 2,
                    isMentioned = true,
                    groupName = "InboxZero 毕设冲刺群 (6)",
                    senderName = "张学长"
                )
            ),
            canInlineReply = true,
            smartReplies = listOf("完全对齐，可以直接跑！", "收到，我马上核对一下")
        ),

        // 3. 普通日常办公邮件 (54分)
        InboxSession(
            sessionId = "mail_university",
            packageName = "com.microsoft.office.outlook",
            appDisplayName = "Outlook",
            latestPostTime = now - 28 * min,
            messageType = 1,
            isMentioned = false,
            senderName = "学院教务办公室",
            isAnalyzing = false,
            urgencyScore = 54,
            aiSummary = "下周三中期答辩排期表公示，请于周五前在线确认汇报顺序",
            unreadCount = 1,
            messages = listOf(
                InboxMessage(
                    systemKey = "k6",
                    packageName = "com.microsoft.office.outlook",
                    postTime = now - 28 * min,
                    title = "学院教务办公室",
                    content = "各位同学好，下周三（11月11日）上午将举行毕设中期检查，附件为答辩顺序表，请于本周五 17:00 前确认。",
                    messageType = 1,
                    senderName = "学院教务办公室"
                )
            ),
            canInlineReply = true,
            smartReplies = listOf("已收到并确认，谢谢老师")
        ),

        // 4. 低优杂音 1：电商促销 (15分 -> 默认自动折叠进底部 CollapsibleContainer)
        InboxSession(
            sessionId = "noise_jd",
            packageName = "com.jingdong.app.mall",
            appDisplayName = "京东",
            latestPostTime = now - 45 * min,
            messageType = 3,
            senderName = "京东数码特惠",
            isAnalyzing = false,
            urgencyScore = 15,
            aiSummary = "双十一数码神券即将过期提醒，满 3000 减 300",
            unreadCount = 4,
            messages = listOf(
                InboxMessage(
                    systemKey = "k7",
                    packageName = "com.jingdong.app.mall",
                    postTime = now - 45 * min,
                    title = "京东数码特惠",
                    content = "您账户中的一张『满3000减300』显示器神券将于今晚 24 点失效，点击立即前往会场选购 >",
                    messageType = 3,
                    senderName = "京东数码特惠"
                )
            ),
            canInlineReply = false
        ),

        // 5. 低优杂音 2：闲聊大群 (22分 -> 默认自动折叠进底部 CollapsibleContainer)
        InboxSession(
            sessionId = "noise_community",
            packageName = "com.tencent.mm",
            appDisplayName = "微信",
            latestPostTime = now - 60 * min,
            messageType = 2,
            groupName = "A栋业主团购闲聊群 (482)",
            senderName = "302王阿姨",
            isAnalyzing = false,
            urgencyScore = 22,
            aiSummary = "群友正在拼单团购赣南脐橙与周末广场舞通知",
            unreadCount = 26,
            messages = listOf(
                InboxMessage(
                    systemKey = "k8",
                    packageName = "com.tencent.mm",
                    postTime = now - 65 * min,
                    title = "A栋业主团购闲聊群 (482)",
                    content = "今天的赣南脐橙特别甜，还差 3 份成团！",
                    attachmentType = 1,
                    messageType = 2,
                    groupName = "A栋业主团购闲聊群 (482)",
                    senderName = "团长小刘"
                ),
                InboxMessage(
                    systemKey = "k9",
                    packageName = "com.tencent.mm",
                    postTime = now - 60 * min,
                    title = "A栋业主团购闲聊群 (482)",
                    content = "给我留两箱，晚上散步过去拿",
                    attachmentType = 2, // [语音]
                    messageType = 2,
                    groupName = "A栋业主团购闲聊群 (482)",
                    senderName = "302王阿姨"
                )
            ),
            canInlineReply = true,
            smartReplies = listOf("+1")
        ),

        // 6. 低优杂音 3：游戏资讯 (12分 -> 默认自动折叠进底部 CollapsibleContainer)
        InboxSession(
            sessionId = "noise_steam",
            packageName = "com.valvesoftware.android.steam.community",
            appDisplayName = "Steam",
            latestPostTime = now - 90 * min,
            messageType = 3,
            senderName = "Steam 愿望单提醒",
            isAnalyzing = false,
            urgencyScore = 12,
            aiSummary = "愿望单中的 2 款独立游戏开启秋季周末 6 折特惠",
            unreadCount = 1,
            messages = listOf(
                InboxMessage(
                    systemKey = "k10",
                    packageName = "com.valvesoftware.android.steam.community",
                    postTime = now - 90 * min,
                    title = "Steam 愿望单提醒",
                    content = "您愿望单中的《Dave the Diver》现已开启 30% 折扣特惠！",
                    messageType = 3,
                    senderName = "Steam 愿望单提醒"
                )
            ),
            canInlineReply = false
        )
    )
}