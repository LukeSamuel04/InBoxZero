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
import com.example.inboxzero.ui.components.shared.notificationapplogo.NotificationAppLogo
import com.example.inboxzero.ui.theme.LocalAppStrings
import com.example.inboxzero.ui.theme.MorandiAmber
import com.example.inboxzero.ui.theme.MorandiBlue
import com.example.inboxzero.ui.theme.MorandiGreen
import com.example.inboxzero.ui.theme.MorandiPurple
import com.example.inboxzero.ui.theme.MorandiRed
import com.example.inboxzero.ui.theme.MorandiTeal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * UI 层软件性质四分类枚举 (用于顶部筛选栏过滤，不污染 data/model)
 */
enum class UiAppCategory(val tabIndex: Int, val color: Color) {
    CHAT(1, MorandiGreen),     // 聊天通讯
    WORK(2, MorandiBlue),      // 办公学业
    SOCIAL(3, MorandiPurple),  // 社交内容
    SERVICE(4, MorandiAmber)   // 生活服务
}

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

val InboxSession.uiDisplayTitle: String
    get() = if (messageType == 2 && !groupName.isNullOrBlank()) groupName else senderName

/**
 * 看板会话核心卡片 (方案 A：四级同色系深浅拼色 + 三级阶梯高度 + 左侧宽色带 Logo 剪影)
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

    val archiveButtonWidthDp = 76.dp
    val maxRevealPx = with(density) { archiveButtonWidthDp.toPx() }
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(isArchiveRevealed) {
        val target = if (isArchiveRevealed) -maxRevealPx else 0f
        offsetX.animateTo(
            targetValue = target,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )
    }

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

    // =========================================================================
    // 1. 四级重要度色彩编码 (红: 高优 >=70 | 橙: 中等 45..69 | 绿: 中低 20..44 | 白: 噪音 <20)
    // =========================================================================
    val score = session.urgencyScore
    val baseSurface = MaterialTheme.colorScheme.surface

    val (targetRibbonColor, targetCardBg, targetBorderColor) = remember(
        score,
        session.isRead,
        session.isAnalyzing,
        isBaselineMode,
        baseSurface
    ) {
        when {
            // Baseline 模式或已读状态：统一褪色为纯净白底 + 柔和浅灰侧翼
            isBaselineMode || session.isRead -> Triple(
                MorandiTeal.copy(alpha = 0.45f),
                baseSurface,
                Color.Gray.copy(alpha = 0.16f)
            )
            // AI 解析中：呼吸青色
            session.isAnalyzing -> Triple(
                MorandiTeal.copy(alpha = 0.85f),
                MorandiTeal.copy(alpha = 0.14f).compositeOver(baseSurface),
                MorandiTeal.copy(alpha = 0.38f)
            )
            // 第一级：高优 (>= 70分) -> 浓郁莫兰迪绯红宽色带 + 醒目暖红卡片背景 (24% 浓度)
            score >= 70 -> Triple(
                MorandiRed.copy(alpha = 0.88f),
                MorandiRed.copy(alpha = 0.24f).compositeOver(baseSurface),
                MorandiRed.copy(alpha = 0.48f)
            )
            // 第二级：中等 (45..69分) -> 莫兰迪琥珀橙宽色带 + 柔和暖橙卡片背景 (22% 浓度)
            score >= 45 -> Triple(
                MorandiAmber.copy(alpha = 0.88f),
                MorandiAmber.copy(alpha = 0.22f).compositeOver(baseSurface),
                MorandiAmber.copy(alpha = 0.45f)
            )
            // 第三级：中低 (20..44分) -> 莫兰迪鼠尾草绿宽色带 + 清浅灰绿卡片背景 (18% 浓度)
            score >= 20 -> Triple(
                MorandiGreen.copy(alpha = 0.82f),
                MorandiGreen.copy(alpha = 0.18f).compositeOver(baseSurface),
                MorandiGreen.copy(alpha = 0.38f)
            )
            // 第四级：纯噪音 (< 20分) -> 浅冷灰宽色带 + 纯白底色
            else -> Triple(
                Color.Gray.copy(alpha = 0.35f),
                baseSurface,
                Color.Gray.copy(alpha = 0.18f)
            )
        }
    }

    val ribbonColor by animateColorAsState(targetValue = targetRibbonColor, label = "RibbonColor")
    val cardBgColor by animateColorAsState(targetValue = targetCardBg, label = "CardBgColor")
    val borderColor by animateColorAsState(targetValue = targetBorderColor, label = "BorderColor")

    val cardAlpha by animateFloatAsState(
        targetValue = if (session.isRead) 0.72f else 1.0f,
        label = "CardReadAlpha"
    )

    // =========================================================================
    // 2. 三级物理高度与字号阶梯 (高优最大 > 中等其次 > 中低最紧凑)
    // =========================================================================
    val verticalPad = when {
        isBaselineMode -> 13.dp
        score >= 70 -> 18.dp
        score >= 45 -> 13.dp
        else -> 9.dp
    }

    val titleFontSize = when {
        score >= 70 -> 15.5.sp
        score >= 45 -> 14.2.sp
        else -> 13.2.sp
    }

    val summaryFontSize = when {
        score >= 70 -> 13.2.sp
        score >= 45 -> 12.5.sp
        else -> 11.8.sp
    }

    val collapsedMaxLines = if (score >= 70) 2 else 1

    val latestRawText = remember(session.messages) {
        val lastMsg = session.messages.lastOrNull()
        if (lastMsg != null) formatAttachmentPrefix(lastMsg.attachmentType) + lastMsg.content else ""
    }

    val summaryDisplay = if (!session.isAnalyzing && !session.aiSummary.isNullOrBlank() && !isBaselineMode) {
        session.aiSummary
    } else {
        latestRawText
    }

    val cardShape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // 底层右侧：左划露出的红色 [归档] 按钮
        if (offsetX.value < -2f || isArchiveRevealed) {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .matchParentSize()
                    .clip(cardShape)
                    .background(MorandiRed.copy(alpha = 0.16f))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(archiveButtonWidthDp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp))
                        .background(MorandiRed)
                        .clickable {
                            onArchiveRevealChange(false)
                            onArchiveConfirmed(session.sessionId)
                        }
                ) {
                    Text(
                        text = strings.inboxBtnArchive,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // 上层前景：左侧 46.dp Logo 剪影宽色带 + 右侧同色系浅底卡片主体
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
            // =================================================================
            // 左侧 46.dp 宽色带：居中绘制纯白应用 Logo 剪影 (彻底代替文字标签)
            // =================================================================
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(46.dp)
                    .fillMaxHeight()
                    .background(ribbonColor)
            ) {
                NotificationAppLogo(
                    packageName = session.packageName,
                    tint = Color.White,
                    size = if (score >= 70) 22.dp else 19.dp
                )
            }

            // =================================================================
            // 右侧内容区
            // =================================================================
            Column(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
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
                        .padding(horizontal = 14.dp, vertical = verticalPad)
                ) {
                    // 第 1 行：干净的联系人/群名 + [@] + 未读数 <---> 纯数字紧急度角标
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = session.uiDisplayTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = titleFontSize),
                                fontWeight = if (session.isRead) FontWeight.Medium else FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            // 群聊 @我 标记
                            if (session.isMentioned && !session.isRead) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MorandiRed.copy(alpha = 0.22f))
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

                            // 未读计数 (已读后自动隐藏)
                            if (!session.isRead && session.unreadCount > 1) {
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "· ${session.unreadCount}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // 右上角：展开态时补充显示软件名与时间，默认折叠态仅显示纯数字胶囊
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isExpanded || isBaselineMode) {
                                Text(
                                    text = "${session.appDisplayName} · ${formatPostTimeMillis(session.latestPostTime)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            if (!isBaselineMode) {
                                InboxUrgencyBadge(
                                    isAnalyzing = session.isAnalyzing,
                                    score = session.urgencyScore,
                                    isRead = session.isRead
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(if (score >= 70) 6.dp else 4.dp))

                    // 第 2 行：无方括号前缀的自然短句摘要
                    Text(
                        text = summaryDisplay,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = summaryFontSize,
                            lineHeight = (summaryFontSize.value + 4.5f).sp
                        ),
                        color = if (!session.isRead && score >= 45) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.90f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                        },
                        maxLines = if (isExpanded) 4 else collapsedMaxLines,
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
                            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                    ) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // 原始消息时间轴
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(2.5.dp)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(ribbonColor)
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

                        if (isReplySuccess) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MorandiGreen.copy(alpha = 0.22f))
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = strings.inboxReplySentToast,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
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
                                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                                    .border(
                                                        width = 0.8.dp,
                                                        color = ribbonColor.copy(alpha = 0.55f),
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
                                                    color = MaterialTheme.colorScheme.onSurface,
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
                                                if (canSend) ribbonColor
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
                                            color = if (canSend) Color.White
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }

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
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
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
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .border(0.8.dp, accentColor.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
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