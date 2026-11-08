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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inboxzero.data.local.UserPreferences
import com.example.inboxzero.data.model.InboxMessage
import com.example.inboxzero.data.model.InboxSession
import com.example.inboxzero.ui.components.inbox.InboxSessionCard
import com.example.inboxzero.ui.components.inbox.InboxStatusHeader
import com.example.inboxzero.ui.components.inbox.resolveUiCategory
import com.example.inboxzero.ui.components.inbox.uiDisplayTitle
import com.example.inboxzero.ui.components.shared.containers.CollapsibleContainer
import com.example.inboxzero.ui.components.shared.containers.SecondaryContainer
import com.example.inboxzero.ui.components.shared.pillrows.SegmentedPillRow
import com.example.inboxzero.ui.theme.LocalAppStrings
import com.example.inboxzero.ui.theme.MorandiAmber
import com.example.inboxzero.ui.theme.MorandiBlue
import com.example.inboxzero.ui.theme.MorandiGreen
import com.example.inboxzero.ui.theme.MorandiPurple
import kotlinx.coroutines.delay

/**
 * InboxZero 首页智能防焦虑看板 (Tab 0: Inbox Screen)
 */
@Composable
fun InboxScreen(
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val haptic = LocalHapticFeedback.current

    // --- 1. 监听 UserPreferences 实验参数配置 ---
    val isAiEnabled by userPreferences.isAiEngineEnabled.collectAsState()
    val isAutoFoldEnabled by userPreferences.isAutoFoldNoise.collectAsState()
    val thresholdIndex by userPreferences.thresholdIndex.collectAsState()
    val isHapticEnabled by userPreferences.isHapticEnabled.collectAsState()

    val thresholdScore = when (thresholdIndex) {
        0 -> 20
        2 -> 60
        else -> 40
    }

    // --- 2. 页面交互状态 ---
    var selectedCategoryTab by rememberSaveable { mutableIntStateOf(0) } // 0=全部, 1=聊天, 2=办公, 3=社交, 4=服务
    var expandedSessionId by rememberSaveable { mutableStateOf<String?>(null) }
    var revealedArchiveSessionId by rememberSaveable { mutableStateOf<String?>(null) }
    var isNoiseVaultExpanded by rememberSaveable { mutableStateOf(false) }
    var simulationCounter by rememberSaveable { mutableIntStateOf(0) }

    // --- 3. 初始演示会话数据 (涵盖聊天/办公/社交/服务四大类软件) ---
    val sessionList = remember {
        mutableStateListOf<InboxSession>().apply {
            addAll(createInitialDemoSessions())
        }
    }

    // 模拟新消息到来后的 1.8 秒 AI 异步评分回填
    val analyzingSessions = sessionList.filter { it.isAnalyzing }
    LaunchedEffect(analyzingSessions.size, simulationCounter) {
        if (analyzingSessions.isNotEmpty()) {
            delay(1800L)
            val updated = sessionList.map { session ->
                if (session.isAnalyzing) {
                    session.copy(
                        isAnalyzing = false,
                        urgencyScore = 92,
                        aiSummary = "[待确认] 下午 3 点实验设备已就绪 · 请带知情同意书"
                    )
                } else {
                    session
                }
            }
            sessionList.clear()
            sessionList.addAll(updated)
        }
    }

    // --- 4. 按顶部选中的软件性质分类 Tab + 实验模式过滤并排序 ---
    val (focusSessions, foldedNoiseSessions) = remember(
        sessionList.toList(),
        selectedCategoryTab,
        isAiEnabled,
        isAutoFoldEnabled,
        thresholdScore
    ) {
        // 先按软件性质 Tab 过滤 (0 代表全部)
        val categoryFiltered = if (selectedCategoryTab == 0) {
            sessionList.toList()
        } else {
            sessionList.filter { it.resolveUiCategory().tabIndex == selectedCategoryTab }
        }

        if (!isAiEnabled) {
            categoryFiltered.sortedByDescending { it.latestPostTime } to emptyList()
        } else {
            // 未读高优排前，已读会话自动下沉
            val sorted = categoryFiltered.sortedWith(
                compareByDescending<InboxSession> { it.isAnalyzing }
                    .thenBy { it.isRead }
                    .thenByDescending { it.urgencyScore }
                    .thenByDescending { it.latestPostTime }
            )
            if (isAutoFoldEnabled) {
                val focus = sorted.filter { it.isAnalyzing || it.urgencyScore >= thresholdScore }
                val noise = sorted.filter { !it.isAnalyzing && it.urgencyScore < thresholdScore }
                focus to noise
            } else {
                sorted to emptyList()
            }
        }
    }

    // 辅助函数：触发受控马达震动
    fun triggerHaptic() {
        if (isHapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // 辅助函数：双击标为已读并展开
    fun markSessionReadAndExpand(sessionId: String) {
        triggerHaptic()
        val idx = sessionList.indexOfFirst { it.sessionId == sessionId }
        if (idx != -1) {
            sessionList[idx] = sessionList[idx].copy(isRead = true)
        }
        expandedSessionId = sessionId
    }

    // 辅助函数：确认归档移除
    fun archiveSession(sessionId: String) {
        triggerHaptic()
        sessionList.removeAll { it.sessionId == sessionId }
        if (expandedSessionId == sessionId) expandedSessionId = null
        if (revealedArchiveSessionId == sessionId) revealedArchiveSessionId = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // =====================================================================
        // 第一层：顶部 AI 态势感知条 + 模拟来信
        // =====================================================================
        InboxStatusHeader(
            isAiEngineEnabled = isAiEnabled,
            currentThreshold = thresholdScore,
            onSimulateMessageClick = {
                triggerHaptic()
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
        // 第二层：四大软件性质筛选胶囊栏 (复用公用 SegmentedPillRow，色彩与卡片左竖线 1:1 呼应)
        // =====================================================================
        SegmentedPillRow(
            options = strings.inboxCategoryTabs,
            selectedIndex = selectedCategoryTab,
            onOptionSelected = { index ->
                selectedCategoryTab = index
                revealedArchiveSessionId = null
            },
            optionColors = listOf(
                null,          // 全部
                MorandiGreen,  // 聊天 (微信/QQ/WhatsApp)
                MorandiBlue,   // 办公 (钉钉/领英/Canvas/Outlook)
                MorandiPurple, // 社交 (小红书/Ins/TikTok/Steam)
                MorandiAmber   // 服务 (京东/系统)
            )
        )

        // 分区标题行 + 极简手势操作提示
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isAiEnabled) strings.inboxSectionFocus else strings.inboxSectionAllChrono,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = strings.inboxGestureHint,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        // =====================================================================
        // 第三层：焦点关注会话列表
        // =====================================================================
        if (focusSessions.isEmpty()) {
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
                        isArchiveRevealed = (revealedArchiveSessionId == session.sessionId),
                        onExpandToggle = {
                            expandedSessionId = if (expandedSessionId == session.sessionId) null else session.sessionId
                        },
                        onDoubleTapReadAndExpand = {
                            markSessionReadAndExpand(session.sessionId)
                        },
                        onArchiveRevealChange = { revealed ->
                            if (revealed) {
                                triggerHaptic()
                                revealedArchiveSessionId = session.sessionId
                            } else if (revealedArchiveSessionId == session.sessionId) {
                                revealedArchiveSessionId = null
                            }
                        },
                        onSendInlineReply = { sessionId, _ ->
                            archiveSession(sessionId)
                        },
                        onArchiveConfirmed = { sessionId ->
                            archiveSession(sessionId)
                        },
                        isBaselineMode = !isAiEnabled
                    )
                }
            }
        }

        // =====================================================================
        // 第四层：底部低优与广告静默收纳舱 (复用公用 CollapsibleContainer + InboxSessionCard)
        // =====================================================================
        if (foldedNoiseSessions.isNotEmpty()) {
            val safetyPeekNames = remember(foldedNoiseSessions, strings) {
                val topNames = foldedNoiseSessions.take(3).joinToString("、") { it.uiDisplayTitle }
                val suffix = if (foldedNoiseSessions.size > 3) " ..." else ""
                "${strings.inboxVaultPeekPrefix}$topNames$suffix"
            }

            Spacer(modifier = Modifier.height(2.dp))

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MorandiGreen.copy(alpha = 0.14f))
                                .border(0.8.dp, MorandiGreen.copy(alpha = 0.40f), RoundedCornerShape(6.dp))
                                .clickable {
                                    triggerHaptic()
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

                    foldedNoiseSessions.forEach { noiseSession ->
                        InboxSessionCard(
                            session = noiseSession,
                            threshold = thresholdScore,
                            isExpanded = (expandedSessionId == noiseSession.sessionId),
                            isArchiveRevealed = (revealedArchiveSessionId == noiseSession.sessionId),
                            onExpandToggle = {
                                expandedSessionId = if (expandedSessionId == noiseSession.sessionId) null else noiseSession.sessionId
                            },
                            onDoubleTapReadAndExpand = {
                                markSessionReadAndExpand(noiseSession.sessionId)
                            },
                            onArchiveRevealChange = { revealed ->
                                if (revealed) {
                                    triggerHaptic()
                                    revealedArchiveSessionId = noiseSession.sessionId
                                } else if (revealedArchiveSessionId == noiseSession.sessionId) {
                                    revealedArchiveSessionId = null
                                }
                            },
                            onSendInlineReply = { sessionId, _ ->
                                archiveSession(sessionId)
                            },
                            onArchiveConfirmed = { sessionId ->
                                archiveSession(sessionId)
                            },
                            onPromotePriority = { sessionId ->
                                triggerHaptic()
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
 * 构造演示会话集（全面覆盖 聊天 / 办公 / 社交 / 服务 四大类软件性质，采用精炼短句摘要）
 */
private fun createInitialDemoSessions(): List<InboxSession> {
    val now = System.currentTimeMillis()
    val min = 60_000L

    return listOf(
        // 1. [聊天 - 绿] 高优微信单聊 (88分)
        InboxSession(
            sessionId = "wx_xiaowang",
            packageName = "com.tencent.mm",
            appDisplayName = "微信",
            latestPostTime = now - 2 * min,
            messageType = 1,
            isMentioned = false,
            senderName = "小王 · 前端组",
            isAnalyzing = false,
            urgencyScore = 88,
            aiSummary = "[待回复] 统计页图表是否已合并 · 确认下午 3 点组会地点",
            unreadCount = 3,
            messages = listOf(
                InboxMessage(
                    systemKey = "k1",
                    packageName = "com.tencent.mm",
                    postTime = now - 5 * min,
                    title = "小王 · 前端组",
                    content = "在吗？统计页的折叠图表我刚拉下来看了一眼，巨丝滑！",
                    messageType = 1,
                    senderName = "小王"
                ),
                InboxMessage(
                    systemKey = "k2",
                    packageName = "com.tencent.mm",
                    postTime = now - 3 * min,
                    title = "小王 · 前端组",
                    content = "这部分代码你已经 git commit 合进主分支了吗？",
                    messageType = 1,
                    senderName = "小王"
                ),
                InboxMessage(
                    systemKey = "k3",
                    packageName = "com.tencent.mm",
                    postTime = now - 2 * min,
                    title = "小王 · 前端组",
                    content = "另外下午 3 点的组会还是在 302 实验室开吗？",
                    messageType = 1,
                    senderName = "小王"
                )
            ),
            canInlineReply = true,
            smartReplies = listOf("已合并，下午 302 见！", "合进去了，稍后同步你")
        ),

        // 2. [办公 - 蓝] 高优钉钉/项目群 (带 @提及，82分)
        InboxSession(
            sessionId = "ding_core_group",
            packageName = "com.alibaba.android.rimet.ding",
            appDisplayName = "钉钉",
            latestPostTime = now - 11 * min,
            messageType = 2,
            isMentioned = true,
            groupName = "毕设冲刺研发群",
            senderName = "张学长",
            isAnalyzing = false,
            urgencyScore = 82,
            aiSummary = "[请核对] 张学长发来数据库迁移脚本，请你确认字段对齐",
            unreadCount = 2,
            messages = listOf(
                InboxMessage(
                    systemKey = "k4",
                    packageName = "com.alibaba.android.rimet.ding",
                    postTime = now - 12 * min,
                    title = "毕设冲刺研发群",
                    content = "我刚把 AiMetadata 表的迁移脚本写好了",
                    attachmentType = 1,
                    messageType = 2,
                    groupName = "毕设冲刺研发群",
                    senderName = "张学长"
                ),
                InboxMessage(
                    systemKey = "k5",
                    packageName = "com.alibaba.android.rimet.ding",
                    postTime = now - 11 * min,
                    title = "毕设冲刺研发群",
                    content = "@你 看看 InboxMessage 的字段是不是都对齐了？",
                    messageType = 2,
                    isMentioned = true,
                    groupName = "毕设冲刺研发群",
                    senderName = "张学长"
                )
            ),
            canInlineReply = true,
            smartReplies = listOf("完全对齐，可以直接跑！", "收到，我马上核对一下")
        ),

        // 3. [办公 - 蓝] 日常 Canvas 课程通知 (54分)
        InboxSession(
            sessionId = "work_canvas",
            packageName = "com.instructure.candroid.canvas",
            appDisplayName = "Canvas",
            latestPostTime = now - 28 * min,
            messageType = 1,
            isMentioned = false,
            senderName = "HCI 课程助教",
            isAnalyzing = false,
            urgencyScore = 54,
            aiSummary = "[周五截止] 下周三中期答辩顺序表已公示，请在线确认",
            unreadCount = 1,
            messages = listOf(
                InboxMessage(
                    systemKey = "k6",
                    packageName = "com.instructure.candroid.canvas",
                    postTime = now - 28 * min,
                    title = "HCI 课程助教",
                    content = "各位同学好，下周三上午将举行毕设中期检查，请于本周五 17:00 前在 Canvas 确认汇报顺序。",
                    messageType = 1,
                    senderName = "HCI 课程助教"
                )
            ),
            canInlineReply = true,
            smartReplies = listOf("已收到并确认，谢谢助教")
        ),

        // 4. [社交 - 紫] 日常小红书私信互动 (46分)
        InboxSession(
            sessionId = "social_xhs",
            packageName = "com.xingin.xhs",
            appDisplayName = "小红书",
            latestPostTime = now - 35 * min,
            messageType = 1,
            isMentioned = false,
            senderName = "北欧设计笔记",
            isAnalyzing = false,
            urgencyScore = 46,
            aiSummary = "[互动] 询问你分享的莫兰迪色板 HEX 色值参数",
            unreadCount = 1,
            messages = listOf(
                InboxMessage(
                    systemKey = "k_xhs",
                    packageName = "com.xingin.xhs",
                    postTime = now - 35 * min,
                    title = "北欧设计笔记",
                    content = "博主你好！请问你那篇防焦虑 App 笔记里的莫兰迪鼠尾草绿 HEX 色号是多少呀？太好看了！",
                    messageType = 1,
                    senderName = "北欧设计笔记"
                )
            ),
            canInlineReply = true,
            smartReplies = listOf("色号是 #8FA89B 哦！", "谢谢喜欢，稍后发你全套色板")
        ),

        // 5. [服务 - 黄] 低优电商促销 (15分 -> 默认折叠进底部收纳舱)
        InboxSession(
            sessionId = "noise_jd",
            packageName = "com.jingdong.app.mall",
            appDisplayName = "京东",
            latestPostTime = now - 45 * min,
            messageType = 3,
            senderName = "京东数码特惠",
            isAnalyzing = false,
            urgencyScore = 15,
            aiSummary = "双十一数码神券即将过期提醒 · 满 3000 减 300",
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

        // 6. [聊天 - 绿] 低优闲聊大群 (22分 -> 当阈值设为40分时自动折叠进底部收纳舱)
        InboxSession(
            sessionId = "noise_community",
            packageName = "com.tencent.mm",
            appDisplayName = "微信",
            latestPostTime = now - 60 * min,
            messageType = 2,
            groupName = "A栋业主团购群",
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
                    title = "A栋业主团购群",
                    content = "今天的赣南脐橙特别甜，还差 3 份成团！",
                    attachmentType = 1,
                    messageType = 2,
                    groupName = "A栋业主团购群",
                    senderName = "团长小刘"
                ),
                InboxMessage(
                    systemKey = "k9",
                    packageName = "com.tencent.mm",
                    postTime = now - 60 * min,
                    title = "A栋业主团购群",
                    content = "给我留两箱，晚上散步过去拿",
                    attachmentType = 2,
                    messageType = 2,
                    groupName = "A栋业主团购群",
                    senderName = "302王阿姨"
                )
            ),
            canInlineReply = true,
            smartReplies = listOf("+1")
        ),

        // 7. [社交 - 紫] 低优游戏资讯 (12分 -> 默认折叠进底部收纳舱)
        InboxSession(
            sessionId = "noise_steam",
            packageName = "com.valvesoftware.android.steam.community",
            appDisplayName = "Steam",
            latestPostTime = now - 90 * min,
            messageType = 3,
            senderName = "Steam 愿望单",
            isAnalyzing = false,
            urgencyScore = 12,
            aiSummary = "愿望单中的 2 款独立游戏开启秋季周末 6 折特惠",
            unreadCount = 1,
            messages = listOf(
                InboxMessage(
                    systemKey = "k10",
                    packageName = "com.valvesoftware.android.steam.community",
                    postTime = now - 90 * min,
                    title = "Steam 愿望单",
                    content = "您愿望单中的《Dave the Diver》现已开启 30% 折扣特惠！",
                    messageType = 3,
                    senderName = "Steam 愿望单"
                )
            ),
            canInlineReply = false
        )
    )
}