package com.example.inboxzero.ui.components.inbox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inboxzero.data.model.InboxMessage
import com.example.inboxzero.data.model.InboxSession
import com.example.inboxzero.ui.theme.LocalAppStrings
import com.example.inboxzero.ui.theme.MorandiAmber
import com.example.inboxzero.ui.theme.MorandiBlue
import com.example.inboxzero.ui.theme.MorandiGreen
import com.example.inboxzero.ui.theme.MorandiPurple
import com.example.inboxzero.ui.theme.MorandiRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * UI 层软件性质四分类枚举 (仅用于视图层色彩编码与顶部 Tab 过滤，不污染 data/model)
 */
enum class UiAppCategory(val tabIndex: Int, val color: Color) {
    CHAT(1, MorandiGreen),     // 聊天通讯：微信、QQ、WhatsApp
    WORK(2, MorandiBlue),      // 办公学业：钉钉、领英、Canvas、Outlook
    SOCIAL(3, MorandiPurple),  // 社交内容：小红书、Instagram、TikTok、Steam
    SERVICE(4, MorandiAmber)   // 生活服务：京东、淘宝、快递、系统通知
}

/**
 * UI 层扩展函数：根据 packageName 推导软件性质分类
 */
fun InboxSession.resolveUiCategory(): UiAppCategory {
    val pkg = packageName.lowercase(Locale.ROOT)
    return when {
        pkg.contains("mm") || pkg.contains("wechat") || pkg.contains("qq") ||
                pkg.contains("whatsapp") || pkg.contains("telegram") -> UiAppCategory.CHAT

        pkg.contains("ding") || pkg.contains("linkedin") || pkg.contains("canvas") ||
                pkg.contains("outlook") || pkg.contains("mail") || pkg.contains("slack") ||
                pkg.contains("teams") -> UiAppCategory.WORK

        pkg.contains("xhs") || pkg.contains("instagram") || pkg.contains("tiktok") ||
                pkg.contains("weibo") || pkg.contains("bili") || pkg.contains("steam") -> UiAppCategory.SOCIAL

        else -> UiAppCategory.SERVICE
    }
}

/**
 * UI 层扩展属性：推导卡片主标题（群聊优先显示群名，单聊显示联系人）
 */
val InboxSession.uiDisplayTitle: String
    get() = if (messageType == 2 && !groupName.isNullOrBlank()) groupName else senderName

/**
 * 看板会话核心卡片 (Inbox Session Card)
 *
 * 核心 HCI 与手势设计：
 * 1. 双通道色彩：左侧 4.dp 竖线编码“软件性质”，卡片微底色编码“高优/日常/已读优先级”。
 * 2. 搭配 A 手势：单击仅展开/收起详情；双击展开并标为已读；左划滑出右侧莫兰迪红 [归档] 按钮，点击按钮执行归档。
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun InboxSessionCard(
    session: InboxSession,
    threshold: Int,
    isExpanded: Boolean,
    isArchiveRevealed: Boolean,
    onExpandToggle: () -> Unit,
    onDoubleTapReadAndExpand: () -> Unit,
    onArchiveRevealChange: (Boolean) -> Unit,
    onSendInlineReply: (sessionId: String, replyText: String) -> Unit,
    onArchiveConfirmed: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier,
    isBaselineMode: Boolean = false,
    onPromotePriority: ((sessionId: String) -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // 右侧红色归档按钮抽屉宽度 (74.dp)
    val archiveButtonWidthDp = 74.dp
    val maxRevealPx = with(density) { archiveButtonWidthDp.toPx() }
    val offsetX = remember { Animatable(0f) }

    // 同步外部互斥状态：当其他卡片被拉开时，当前卡片自动平滑收拢
    LaunchedEffect(isArchiveRevealed) {
        val target = if (isArchiveRevealed) -maxRevealPx else 0f
        offsetX.animateTo(
            targetValue = target,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )
    }

    val uiCategory = remember(session.packageName) { session.resolveUiCategory() }
    val categoryAccentColor = uiCategory.color

    var replyDraft by rememberSaveable(session.sessionId) { mutableStateOf("") }
    var isReplySuccess by remember { mutableStateOf(false) }

    LaunchedEffect(isReplySuccess) {
        if (isReplySuccess) {
            delay(600L)
            onSendInlineReply(session.sessionId, replyDraft)
            replyDraft = ""
            isReplySuccess = false
        }
    }

    // --- 计算卡片优先级染色与已读透明度 ---
    val isHighPriorityUnread = !session.isRead && !session.isAnalyzing &&
            session.urgencyScore >= 70 && !isBaselineMode

    val baseSurface = MaterialTheme.colorScheme.surface
    val targetCardBg = when {
        isHighPriorityUnread -> MorandiRed.copy(alpha = 0.09f).compositeOver(baseSurface)
        !session.isRead && !session.isAnalyzing && session.urgencyScore < threshold && !isBaselineMode ->
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f).compositeOver(baseSurface)
        else -> baseSurface
    }

    val cardBgColor by animateColorAsState(targetValue = targetCardBg, label = "CardBgTint")

    val borderColor by animateColorAsState(
        targetValue = if (isHighPriorityUnread) {
            MorandiRed.copy(alpha = 0.32f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f)
        },
        label = "CardBorderTint"
    )

    val cardAlpha by animateFloatAsState(
        targetValue = if (session.isRead) 0.72f else 1.0f,
        label = "CardReadAlpha"
    )

    // 摘要文案（彻底移除“✦ 摘要：”废话前缀）
    val latestRawText = remember(session.messages) {
        val lastMsg = session.messages.lastOrNull()
        if (lastMsg != null) formatAttachmentPrefix(lastMsg.attachmentType) + lastMsg.content else ""
    }

    val summaryDisplay = if (!session.isAnalyzing && !session.aiSummary.isNullOrBlank() && !isBaselineMode) {
        session.aiSummary
    } else {
        latestRawText
    }

    val cardShape = RoundedCornerShape(12.dp)

    // 外层容器：底层右侧放置莫兰迪红 [归档] 按钮，上层为可水平滑动的卡片前景
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // =====================================================================
        // 1. 底层右侧：莫兰迪红 [归档] 按钮（左划卡片时露出，点击才真正归档）
        // =====================================================================
        if (offsetX.value < -2f || isArchiveRevealed) {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .matchParentSize()
                    .clip(cardShape)
                    .background(MorandiRed.copy(alpha = 0.15f))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(archiveButtonWidthDp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                        .background(MorandiRed)
                        .clickable {
                            onArchiveRevealChange(false)
                            onArchiveConfirmed(session.sessionId)
                        }
                ) {
                    Text(
                        text = strings.inboxBtnArchive,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // =====================================================================
        // 2. 上层前景：带左侧 4.dp 软件性质色条与优先级微底色的会话卡片
        // =====================================================================
        Row(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .alpha(cardAlpha)
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(cardShape)
                .background(cardBgColor)
                .border(width = 1.dp, color = borderColor, shape = cardShape)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            val newOffset = (offsetX.value + delta).coerceIn(-maxRevealPx * 1.08f, 0f)
                            offsetX.snapTo(newOffset)
                        }
                    },
                    onDragStopped = { velocity ->
                        val shouldReveal = offsetX.value < -maxRevealPx * 0.4f || velocity < -600f
                        onArchiveRevealChange(shouldReveal)
                        coroutineScope.launch {
                            offsetX.animateTo(
                                targetValue = if (shouldReveal) -maxRevealPx else 0f,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )
                        }
                    }
                )
        ) {
            // --- 左侧 4.5dp 软件性质莫兰迪色条 (聊天绿 / 办公蓝 / 社交紫 / 服务黄) ---
            Box(
                modifier = Modifier
                    .width(4.5.dp)
                    .fillMaxHeight()
                    .background(categoryAccentColor)
            )

            // --- 右侧卡片内容主体 ---
            Column(modifier = Modifier.weight(1f)) {
                // 常驻紧凑头部（支持：单击仅展开偷看 / 双击展开并已读）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                // 防呆设计：若右侧红色归档按钮正处于拉开状态，单击卡片优先收起归档按钮
                                if (isArchiveRevealed) {
                                    onArchiveRevealChange(false)
                                } else {
                                    onExpandToggle()
                                }
                            },
                            onDoubleClick = {
                                if (isArchiveRevealed) {
                                    onArchiveRevealChange(false)
                                }
                                onDoubleTapReadAndExpand()
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    // 第 1 行：联系人/群名 + 轻量来源标签 + [@] + 未读数 <---> 纯数字紧急度角标
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            // 主标题：直接显示人名或群名（砍掉啰嗦的“微信 · ”前缀）
                            Text(
                                text = session.uiDisplayTitle,
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                                fontWeight = if (session.isRead) FontWeight.Medium else FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // 极简平台性质微标签 (如 [微信] / [Canvas])
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(categoryAccentColor.copy(alpha = 0.14f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = session.appDisplayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // 群聊 @我 高亮小标签
                            if (session.isMentioned && !session.isRead) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MorandiRed.copy(alpha = 0.18f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "@",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MorandiRed
                                    )
                                }
                            }

                            // 未读条数（仅在未读且 >1 条时展示，已读后自动隐藏）
                            if (!session.isRead && session.unreadCount > 1) {
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "· ${session.unreadCount}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // 右上角：仅保留极简纯数字角标（展开态或 Baseline 模式才补充显示时间）
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isExpanded || isBaselineMode) {
                                Text(
                                    text = formatPostTimeMillis(session.latestPostTime),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            if (!isBaselineMode) {
                                InboxUrgencyBadge(
                                    isAnalyzing = session.isAnalyzing,
                                    score = session.urgencyScore,
                                    threshold = threshold,
                                    isRead = session.isRead
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // 第 2 行：无前缀精炼摘要
                    Text(
                        text = summaryDisplay,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp
                        ),
                        color = if (!session.isRead && isHighPriorityUnread) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = if (isExpanded) 4 else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // =============================================================
                // 展开态详情区 (原始消息时间轴 + 后台静默回复输入框)
                // =============================================================
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(),
                    exit = shrinkVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                    ) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // 原始消息时间轴
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(categoryAccentColor.copy(alpha = 0.65f))
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                session.messages.forEach { msg ->
                                    RawMessageRow(
                                        message = msg,
                                        isGroupChat = (session.messageType == 2)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 原地回复与操作控制区
                        if (isReplySuccess) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MorandiGreen.copy(alpha = 0.18f))
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = strings.inboxReplySentToast,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MorandiGreen
                                )
                            }
                        } else {
                            if (session.canInlineReply && session.messageType != 3) {
                                if (session.smartReplies.isNotEmpty()) {
                                    FlowRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        session.smartReplies.forEach { quickText ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.09f))
                                                    .border(
                                                        width = 0.8.dp,
                                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                                                        shape = CircleShape
                                                    )
                                                    .clickable {
                                                        replyDraft = quickText
                                                        isReplySuccess = true
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "⚡ $quickText",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.CenterStart,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        if (replyDraft.isEmpty()) {
                                            Text(
                                                text = strings.inboxReplyPlaceholder,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                        }
                                        BasicTextField(
                                            value = replyDraft,
                                            onValueChange = { replyDraft = it },
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    val canSend = replyDraft.isNotBlank()
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (canSend) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                                            )
                                            .clickable(enabled = canSend) {
                                                isReplySuccess = true
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "${strings.inboxBtnSend} ➢",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = if (canSend) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // 底部操作按钮行
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (onPromotePriority != null) {
                                    ActionChipButton(
                                        text = strings.inboxBtnPromote,
                                        accentColor = MorandiAmber,
                                        onClick = { onPromotePriority(session.sessionId) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                ActionChipButton(
                                    text = strings.inboxBtnMarkDone,
                                    accentColor = MorandiGreen,
                                    onClick = { onArchiveConfirmed(session.sessionId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RawMessageRow(
    message: InboxMessage,
    isGroupChat: Boolean
) {
    val timeStr = remember(message.postTime) { formatPostTimeMillis(message.postTime) }
    val attachmentTag = remember(message.attachmentType) { formatAttachmentPrefix(message.attachmentType) }
    val prefix = if (isGroupChat && message.senderName.isNotBlank()) {
        "${message.senderName}: "
    } else {
        ""
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = timeStr,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            modifier = Modifier.width(36.dp)
        )
        Text(
            text = "$prefix$attachmentTag${message.content}",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionChipButton(
    text: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(accentColor.copy(alpha = 0.14f))
            .border(0.8.dp, accentColor.copy(alpha = 0.40f), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatPostTimeMillis(postTime: Long): String {
    return try {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(postTime))
    } catch (e: Exception) {
        "--:--"
    }
}

private fun formatAttachmentPrefix(attachmentType: Int): String {
    return when (attachmentType) {
        1 -> "[图片] "
        2 -> "[语音] "
        else -> ""
    }
}