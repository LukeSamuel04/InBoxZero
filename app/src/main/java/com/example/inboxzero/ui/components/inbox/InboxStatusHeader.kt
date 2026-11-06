package com.example.inboxzero.ui.components.inbox

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inboxzero.ui.components.containers.CompactContainer
import com.example.inboxzero.ui.theme.LocalAppStrings
import com.example.inboxzero.ui.theme.MorandiAmber
import com.example.inboxzero.ui.theme.MorandiGreen

/**
 * 看板顶部态势感知状态条 (Inbox Status Header)
 *
 * 核心 HCI 特性：
 * 1. 模式透明化：实时反映当前处于 AI 混合降噪模式 (鼠尾草绿) 还是 Baseline 时间倒序模式 (暖沙黄)。
 * 2. 实验注入入口：右侧提供 [+ 模拟来信] 胶囊，便于在无外部通知时一键验证两阶段乐观 UI 与 AI 延迟回填。
 */
@Composable
fun InboxStatusHeader(
    isAiEngineEnabled: Boolean,
    currentThreshold: Int,
    onSimulateMessageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    val statusColor by animateColorAsState(
        targetValue = if (isAiEngineEnabled) MorandiGreen else MorandiAmber,
        label = "InboxStatusColor"
    )

    val statusText = if (isAiEngineEnabled) {
        "${strings.inboxStatusAiActive} (${strings.inboxStatusThresholdPrefix} $currentThreshold${strings.badgeUnitScore})"
    } else {
        strings.inboxStatusBaseline
    }

    CompactContainer(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 10.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：模式状态文字
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                fontWeight = FontWeight.Bold,
                color = statusColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 右侧：[+ 模拟来信] 微边框胶囊按钮
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSimulateMessageClick
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = strings.inboxBtnSimulate,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}