package com.example.inboxzero.ui.components.shared.pillrows

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 全站公用分段式胶囊切换器 (Segmented Pill Row)
 *
 * 复用场景：
 * 1. AnalyticsScreen: 3 维度切换 (不传 optionColors，默认使用主题色)
 * 2. InboxScreen: 5 类软件性质筛选栏 (传入 optionColors，展示莫兰迪性质色点并与卡片左竖线呼应)
 */
@Composable
fun SegmentedPillRow(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    optionColors: List<Color?>? = null
) {
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(trackColor)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = (index == selectedIndex)
            val categoryColor = optionColors?.getOrNull(index)
            val activeAccent = categoryColor ?: MaterialTheme.colorScheme.primary

            val pillBgColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                animationSpec = tween(durationMillis = 200),
                label = "SegmentPillBg"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) {
                    activeAccent
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                },
                animationSpec = tween(durationMillis = 200),
                label = "SegmentPillText"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(pillBgColor)
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                width = 1.dp,
                                color = activeAccent.copy(alpha = 0.30f),
                                shape = CircleShape
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onOptionSelected(index) }
                    )
                    .padding(vertical = 6.dp, horizontal = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // 若该选项配置了软件性质颜色，展示一颗 5.5dp 的莫兰迪指示圆点
                    if (categoryColor != null) {
                        Box(
                            modifier = Modifier
                                .size(5.5.dp)
                                .clip(CircleShape)
                                .background(
                                    categoryColor.copy(alpha = if (isSelected) 1f else 0.65f)
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}