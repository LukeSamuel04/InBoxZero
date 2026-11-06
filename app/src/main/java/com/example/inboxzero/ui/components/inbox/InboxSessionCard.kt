package com.example.inboxzero.ui.components.inbox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inboxzero.data.model.InboxMessage
import com.example.inboxzero.data.model.InboxSession
import com.example.inboxzero.ui.components.containers.CompactContainer
import com.example.inboxzero.ui.theme.LocalAppStrings
import com.example.inboxzero.ui.theme.MorandiAmber
import com.example.inboxzero.ui.theme.MorandiBlue
import com.example.inboxzero.ui.theme.MorandiGreen
import com.example.inboxzero.ui.theme.MorandiPurple
import com.example.inboxzero.ui.theme.MorandiRed
import com.example.inboxzero.ui.theme.MorandiTeal
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 看板会话核心卡片 (Inbox Session Card)
 *
 * 核心 HCI 与架构特性：
 * 1. 纯净模型驱动：直接消费 data.model.InboxSession 与 InboxMessage，UI 层自行推导色彩与时间格式。
 * 2. 渐进式披露：默认仅占 ~64dp 高度展示“来源 + 评分角标 + AI 话题摘要”；点击后展开原始消息时间轴与原地回复舱。
 * 3. 零上下文切换回复 (Inline Reply)：支持点击快捷回复胶囊或直接打字后台静默回复，回复后给出温和归档反馈。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InboxSessionCard(
    session: InboxSession,
    threshold: Int,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onSendInlineReply: (sessionId: String, replyText: String) -> Unit,
    onMarkAsDone: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier,
    isBaselineMode: Boolean = false,
    onPromotePriority: ((sessionId: String) -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    val appAccentColor = remember(session.packageName) {
        resolvePackageMorandiColor(session.packageName)
    }

    var replyDraft by rememberSaveable(session.sessionId) { mutableStateOf("") }
    var isReplySuccess by remember { mutableStateOf(false) }

    // 触发回复后的 0.65 秒静默成功反馈与自动归档过渡
    LaunchedEffect(isReplySuccess) {
        if (isReplySuccess) {
            delay(650L)
            onSendInlineReply(session.sessionId, replyDraft)
            replyDraft = ""
            isReplySuccess = false
        }
    }

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "SessionCardArrow"
    )

    // 计算当前显示的摘要文本（若正在 AI 解析或无摘要，则回退展示最新一条原始消息文本）
    val latestRawText = remember(session.messages) {
        val lastMsg = session.messages.lastOrNull()
        if (lastMsg != null) formatAttachmentPrefix(lastMsg.attachmentType) + lastMsg.content else ""
    }

    val summaryDisplay = if (!session.isAnalyzing && !session.aiSummary.isNullOrBlank() && !isBaselineMode) {
        "${strings.inboxAiSummaryPrefix}${session.aiSummary}"
    } else {
        latestRawText
    }

    CompactContainer(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 12.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // =================================================================
            // 一、 常驻紧凑头部区 (点击切换展开/折叠)
            // =================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onExpandChange(!isExpanded) }
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // --- 第 1 行：平台色标 + 标题 + 未读数 + [@] 标记 <---> AI 评分角标 + 时间 + ▸ ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左侧：来源与会话标题信息
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        // 平台莫兰迪圆点
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(appAccentColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        // 应用名称 · 联系人/群名
                        Text(
                            text = "${session.appDisplayName} ·${session.displayTitle}",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // 若群聊被 @提及 (InboxMessage.isMentioned)，高亮展示 [@] 胶囊
                        if (session.isMentioned) {
                            Spacer(modifier = Modifier.width(5.dp))
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

                        // 多条未读计数胶囊
                        if (session.unreadCount > 1) {
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "(${session.unreadCount})",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 右侧：AI 紧急度动态角标 (Baseline 模式下隐藏) + 最新时间 + 小箭头
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isBaselineMode) {
                            InboxUrgencyBadge(
                                isAnalyzing = session.isAnalyzing,
                                score = session.urgencyScore,
                                threshold = threshold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Text(
                            text = formatPostTimeMillis(session.latestPostTime),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "▸",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.rotate(arrowRotation)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // --- 第 2 行：AI 话题摘要 (折叠时单行省略，展开时完整显示) ---
                Text(
                    text = summaryDisplay,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    ),
                    color = if (!session.isAnalyzing && !session.aiSummary.isNullOrBlank() && !isBaselineMode) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = if (isExpanded) 4 else 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // =================================================================
            // 二、 展开态详情区 (原始消息时间轴 + 原地回复舱)
            // =================================================================
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

                    // --- 2.1 原始消息时间轴盒子 (Raw Messages Timeline) ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        // 左侧莫兰迪垂直时间轴细线
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(appAccentColor.copy(alpha = 0.65f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // 右侧逐条原始 InboxMessage 列表
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

                    // --- 2.2 原地回复与操作控制区 (Inline Reply & Action Bar) ---
                    if (isReplySuccess) {
                        // 回复发送成功后的短暂柔和提示条
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
                        // A. 若支持后台直接回复 (如微信/短信，messageType != 3 且 canInlineReply)
                        if (session.canInlineReply && session.messageType != 3) {
                            // 快捷回复建议词胶囊行 (Smart Replies)
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

                            // 紧凑单行原地输入框 + 发送按钮
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

                                // 发送按钮
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

                        // B. 底部通用操作行：[↑ 移出杂音舱 (可选)] + [✓ 标为已处理]
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
                                onClick = { onMarkAsDone(session.sessionId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 渲染单条原始 InboxMessage 时间轴行
 */
@Composable
private fun RawMessageRow(
    message: InboxMessage,
    isGroupChat: Boolean
) {
    val timeStr = remember(message.postTime) { formatPostTimeMillis(message.postTime) }
    val attachmentTag = remember(message.attachmentType) { formatAttachmentPrefix(message.attachmentType) }

    // 群聊时显示具体发言人前缀 (message.senderName)
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
            .border(
                width = 0.8.dp,
                color = accentColor.copy(alpha = 0.40f),
                shape = RoundedCornerShape(6.dp)
            )
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

/**
 * 纯 UI 视觉辅助函数：根据 InboxMessage.packageName 推导莫兰迪点缀色 (保持 data 层零 Color 污染)
 */
fun resolvePackageMorandiColor(packageName: String): Color {
    val pkg = packageName.lowercase(Locale.ROOT)
    return when {
        pkg.contains("mm") || pkg.contains("wechat") -> MorandiGreen
        pkg.contains("mail") || pkg.contains("outlook") || pkg.contains("gm") -> MorandiBlue
        pkg.contains("slack") || pkg.contains("teams") || pkg.contains("work") -> MorandiPurple
        pkg.contains("shop") || pkg.contains("jd") || pkg.contains("taobao") -> MorandiAmber
        pkg.contains("urgent") || pkg.contains("alarm") -> MorandiRed
        else -> MorandiTeal
    }
}

/**
 * 纯 UI 视觉辅助函数：将 InboxMessage.postTime (Long) 转为 "HH:mm"
 */
private fun formatPostTimeMillis(postTime: Long): String {
    return try {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(postTime))
    } catch (e: Exception) {
        "--:--"
    }
}

/**
 * 纯 UI 视觉辅助函数：根据 InboxMessage.attachmentType (0=纯文本, 1=图片, 2=语音) 生成标签前缀
 */
private fun formatAttachmentPrefix(attachmentType: Int): String {
    return when (attachmentType) {
        1 -> "[图片] "
        2 -> "[语音] "
        else -> ""
    }
}