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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inboxzero.data.model.InboxSession
import com.example.inboxzero.ui.components.containers.CompactContainer
import com.example.inboxzero.ui.theme.LocalAppStrings
import com.example.inboxzero.ui.theme.MorandiGreen

/**
 * 底部低优杂音静默收纳舱 (Inbox Noise Vault Card)
 *
 * 核心 HCI 特性：
 * 1. 防漏报安全预览 (Safety Peek)：折叠状态下直接展示被拦截的发送者名单摘要，消除用户对 AI 误判的黑盒焦虑。
 * 2. 一键纠偏与一键清零：展开后支持将误判会话一键提权回主看板 (onPromoteSession)，或一键清空全部低优杂音。
 */
@Composable
fun InboxNoiseVaultCard(
    noiseSessions: List<InboxSession>,
    threshold: Int,
    isVaultExpanded: Boolean,
    onVaultExpandChange: (Boolean) -> Unit,
    expandedSessionId: String?,
    onSessionExpandChange: (String?) -> Unit,
    onSendInlineReply: (sessionId: String, replyText: String) -> Unit,
    onMarkSessionDone: (sessionId: String) -> Unit,
    onPromoteSession: (sessionId: String) -> Unit,
    onClearAllNoise: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (noiseSessions.isEmpty()) return

    val strings = LocalAppStrings.current

    val arrowRotation by animateFloatAsState(
        targetValue = if (isVaultExpanded) 90f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "VaultArrowRotation"
    )

    // 拼接前 3 个被折叠会话的名称，提供防漏报安全感预览
    val safetyPeekText = remember(noiseSessions, strings) {
        val names = noiseSessions.take(3).joinToString("、") { it.displayTitle }
        val ellipsis = if (noiseSessions.size > 3) " ..." else ""
        "${strings.inboxVaultPeekPrefix}$names$ellipsis"
    }

    CompactContainer(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 12.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // =================================================================
            // 一、 抽屉头部摘要区 (点击整块区域展开/收起收纳舱)
            // =================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onVaultExpandChange(!isVaultExpanded) }
                    )
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.inboxVaultTitle,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${strings.inboxVaultFoldedPrefix} ${noiseSessions.size} ${strings.inboxVaultFoldedSuffix}",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            fontWeight = FontWeight.Bold,
                            color = MorandiGreen
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

                Spacer(modifier = Modifier.height(4.dp))

                // 安全感预览小字：直接列出被折叠的联系人/群聊名称
                Text(
                    text = safetyPeekText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // =================================================================
            // 二、 抽屉展开态：一键清零按钮 + 被折叠的低优会话卡片列表
            // =================================================================
            AnimatedVisibility(
                visible = isVaultExpanded,
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
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                    )

                    // 顶部工具栏：[🧹 一键已读全部杂音]
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
                                .clickable(onClick = onClearAllNoise)
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

                    // 遍历渲染每一张被折叠的低优卡片（附带 onPromotePriority 一键提权回调）
                    noiseSessions.forEach { session ->
                        val isCardExpanded = (expandedSessionId == session.sessionId)
                        InboxSessionCard(
                            session = session,
                            threshold = threshold,
                            isExpanded = isCardExpanded,
                            onExpandChange = { expand ->
                                onSessionExpandChange(if (expand) session.sessionId else null)
                            },
                            onSendInlineReply = onSendInlineReply,
                            onMarkAsDone = onMarkSessionDone,
                            onPromotePriority = onPromoteSession
                        )
                    }
                }
            }
        }
    }
}