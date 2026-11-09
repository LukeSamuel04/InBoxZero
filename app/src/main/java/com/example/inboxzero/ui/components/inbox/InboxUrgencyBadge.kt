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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inboxzero.ui.theme.LocalAppStrings
import com.example.inboxzero.ui.theme.MorandiAmber
import com.example.inboxzero.ui.theme.MorandiGreen
import com.example.inboxzero.ui.theme.MorandiRed
import com.example.inboxzero.ui.theme.MorandiTeal

/**
 * 极简纯数字 AI 紧急度角标 (适配四级同色系卡片背景)
 */
@Composable
fun InboxUrgencyBadge(
    isAnalyzing: Boolean,
    score: Int,
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
                    .background(MorandiTeal.copy(alpha = 0.20f))
                    .border(1.dp, MorandiTeal.copy(alpha = 0.55f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = strings.badgeAnalyzingShort,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            // 四级色彩对应：>=70 绯红，45..69 琥珀橙，20..44 鼠尾草绿，<20 浅冷灰
            val tierAccentColor = when {
                score >= 70 -> MorandiRed
                score >= 45 -> MorandiAmber
                score >= 20 -> MorandiGreen
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            val bgColor = when {
                isRead || score < 20 -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.10f)
                score >= 70 -> tierAccentColor.copy(alpha = 0.90f)
                else -> tierAccentColor.copy(alpha = 0.82f)
            }

            val textColor = when {
                isRead || score < 20 -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                else -> Color.White
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bgColor)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = if (score >= 70) 11.5.sp else 10.5.sp
                    ),
                    fontWeight = if (isRead) FontWeight.Medium else FontWeight.ExtraBold,
                    color = textColor
                )
            }
        }
    }
}