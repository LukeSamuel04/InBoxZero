package com.example.inboxzero.ui.components.analytics

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

/**
 * 极简防焦虑胶囊分段切换器 (Segmented Pill Row)
 *
 * 核心 HCI 设计特性：
 * 1. 原地维度切换：用于在单个图表卡片内切换统计维度（如：紧急度 / 平台 / 耗时），避免页面堆砌多张图表。
 * 2. 零阴影扁平降噪：采用柔和的半透明轨道凹槽，被选中的胶囊通过纯净表面色 (Surface) 与莫兰迪主色微边框形成视觉锚定。
 * 3. 无障碍兼容：单行自适应等宽分布，自动响应全局适老大字缩放。
 */
@Composable
fun SegmentedPillRow(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 外层凹槽轨道：全圆角胶囊形，采用极浅的次级表面色作为底槽
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                shape = CircleShape
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = (index == selectedIndex)

            // 选中态滑块背景色过渡动画
            val pillBgColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.surface
                } else {
                    Color.Transparent
                },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "SegmentPillBg"
            )

            // 选中态滑块微边框过渡动画
            val pillBorderColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                } else {
                    Color.Transparent
                },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "SegmentPillBorder"
            )

            // 文字颜色过渡动画
            val textColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "SegmentPillText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(pillBgColor)
                    .border(
                        width = 1.dp,
                        color = pillBorderColor,
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // 禁用突兀的方形水波纹，依靠胶囊底色丝滑过渡
                        onClick = { onOptionSelected(index) }
                    )
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}