package com.example.inboxzero.ui.components.shared.containers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * InboxZero 紧凑型扁平容器 (Compact Container)
 *
 * 核心设计规则 (HCI)：
 * 1. 专为高密度单行指标条 (BASE METRICS) 与折叠摘要卡片打造，彻底摆脱 20.dp 的厚重内边距。
 * 2. 视觉克制：采用 10.dp 利落小圆角 + Surface 背景色 + 1.dp 极细微边框，零阴影降噪。
 * 3. 灵活边距：默认采用 vertical = 10.dp, horizontal = 12.dp 的黄金紧凑比例，杜绝文字被挤压成省略号。
 */
@Composable
fun CompactContainer(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f),
                shape = shape
            )
            .padding(contentPadding),
        content = content
    )
}