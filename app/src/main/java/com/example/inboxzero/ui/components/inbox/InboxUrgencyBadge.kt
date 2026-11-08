package com.example.inboxzero.ui.components.inbox

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inboxzero.ui.theme.LocalAppStrings
import com.example.inboxzero.ui.theme.MorandiAmber
import com.example.inboxzero.ui.theme.MorandiGreen
import com.example.inboxzero.ui.theme.MorandiRed
import com.example.inboxzero.ui.theme.MorandiTeal

/**
 * 极简纯数字 AI 紧急度角标 (Inbox Urgency Badge)
 *
 * 人机工效改进：
 * 1. 零文字噪音：移除“分 · 高优/日常”冗余后缀，仅保留纯数字与语义色彩，0.1 秒直觉扫视。
 * 2. 已读视觉降权：当 isRead = true 时，角标自动褪去高饱和色彩，转为低对比度浅灰胶囊。
 */
@Composable
fun InboxUrgencyBadge(
    isAnalyzing: Boolean,
    score: Int,
    threshold: Int,
    modifier: Modifier = Modifier,
    isRead: Boolean = false
) {
    val strings = LocalAppStrings.current

    Crossfade(
        targetState = isAnalyzing,
        animationSpec = tween(durationMillis = 300),
        label = "UrgencyBadgeCrossfade",
        modifier = modifier
    ) { analyzing ->
        if (analyzing) {
            val infiniteTransition = rememberInfiniteTransition(label = "AnalyzingPulse")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.35f,
                targetValue = 0.95f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "PulseAlpha"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .alpha(pulseAlpha)
                    .clip(CircleShape)
                    .background(MorandiTeal.copy(alpha = 0.16f))
                    .border(1.dp, MorandiTeal.copy(alpha = 0.45f), CircleShape)
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = strings.badgeAnalyzingShort,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            val rawBadgeColor = when {
                score >= 70 -> MorandiRed
                score >= threshold -> MorandiAmber
                else -> MorandiGreen
            }

            val bgColor = if (isRead) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.10f)
            } else {
                rawBadgeColor.copy(alpha = if (score >= 70) 0.85f else 0.20f)
            }

            val borderColor = if (isRead) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.20f)
            } else {
                rawBadgeColor.copy(alpha = 0.55f)
            }

            val textColor = when {
                isRead -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                score >= 70 -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.onSurface
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bgColor)
                    .border(width = 1.dp, color = borderColor, shape = CircleShape)
                    .padding(horizontal = 7.dp, vertical = 1.5.dp)
            ) {
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = if (isRead) FontWeight.Medium else FontWeight.ExtraBold,
                    color = textColor
                )
            }
        }
    }
}