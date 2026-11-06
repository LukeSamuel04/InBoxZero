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
 * AI 紧急度动态呼吸角标 (Inbox Urgency Badge)
 *
 * 核心 HCI 特性：
 * 1. 无焦虑异步等待：在 LLM/混合引擎计算期间 (isAnalyzing = true)，采用正弦呼吸透明度动画展示“✦ AI 解析中...”，抛弃令人紧张的旋转菊花圈。
 * 2. 莫兰迪语义分级：解析完成后平滑过渡为对应色阶的分数胶囊 (高优灰粉红 / 日常暖沙黄 / 低优鼠尾草绿)。
 */
@Composable
fun InboxUrgencyBadge(
    isAnalyzing: Boolean,
    score: Int,
    threshold: Int,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    Crossfade(
        targetState = isAnalyzing,
        animationSpec = tween(durationMillis = 350),
        label = "UrgencyBadgeCrossfade",
        modifier = modifier
    ) { analyzing ->
        if (analyzing) {
            // 阶段一：AI 异步计算中的柔和呼吸态
            val infiniteTransition = rememberInfiniteTransition(label = "AnalyzingPulse")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.40f,
                targetValue = 0.95f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
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
                    .border(
                        width = 1.dp,
                        color = MorandiTeal.copy(alpha = 0.45f),
                        shape = CircleShape
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = strings.badgeAnalyzing,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            // 阶段二：AI 评分已回填的稳定态
            val (badgeColor, levelLabel) = when {
                score >= 70 -> MorandiRed to strings.badgeLevelHigh
                score >= threshold -> MorandiAmber to strings.badgeLevelMed
                else -> MorandiGreen to strings.badgeLevelLow
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.18f))
                    .border(
                        width = 1.dp,
                        color = badgeColor.copy(alpha = 0.50f),
                        shape = CircleShape
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$score${strings.badgeUnitScore} · $levelLabel",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}