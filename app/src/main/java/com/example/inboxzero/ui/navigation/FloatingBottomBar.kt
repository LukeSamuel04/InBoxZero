package com.example.inboxzero.ui.navigation
import com.example.inboxzero.ui.theme.LocalAppStrings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * FatSecret 风格悬浮半透明胶囊导航栏 (Floating Translucent Pill Bar)
 *
 * 核心 HCI 设计特性：
 * 1. 真·半透明玻璃态：移除会导致 Android 底层强制垫实心底板的原生 shadow，
 *    采用 65% 透明度材质配合高光渐变边缘，当底部有列表滚过时可清晰透出背景色彩。
 * 2. 视觉锚定 (Visual Anchoring)：被选中的 Tab 拥有独立的半透明莫兰迪色椭圆内底座与微边框。
 */
@Composable
fun FloatingBottomBar(
    currentDestination: AppDestination,
    onTabSelected: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    // 外层定位容器：负责处理系统底部小白条安全距离，并提供四周的悬浮留白
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // 胶囊主壳体：真半透明 (alpha = 0.65f) + 细腻的玻璃感边缘边框 (elevation 设为 0 避免系统填充实心底色)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        // 顶部边缘稍亮（模拟光线折射），底部边缘柔和加深（模拟悬浮立体感）
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f),
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppDestination.entries.forEach { destination ->
                    val isSelected = (destination == currentDestination)

                    FloatingBarItem(
                        destination = destination,
                        isSelected = isSelected,
                        onClick = { onTabSelected(destination) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * 单个导航图标槽位（包含选中态的半透明椭圆底座与色彩过渡动画）
 */
@Composable
private fun FloatingBarItem(
    destination: AppDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val localizedLabel = when (destination) {
        AppDestination.INBOX -> strings.navInbox
        AppDestination.ANALYTICS -> strings.navInsights
        AppDestination.SETTINGS -> strings.navSettings
    }
    // 图标与文字颜色的丝滑过渡动画
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "NavIconColorAnimation"
    )

    // 选中态背后的椭圆底座颜色（直接使用你的莫兰迪主色 + 16% 透明度，拒绝系统默认的突兀紫色）
    val pillBackgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        } else {
            Color.Transparent
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "NavPillBgAnimation"
    )

    // 选中态内胶囊的微高光边缘色
    val pillBorderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
        } else {
            Color.Transparent
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "NavPillBorderAnimation"
    )

    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clip(CircleShape)
            .background(pillBackgroundColor)
            .border(
                width = 1.dp,
                color = pillBorderColor,
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // 禁用默认生硬的方形水波纹，完全依靠胶囊底座变色提供高级反馈
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                // 选中时切换为实心 Filled 图标，未选中时为空心 Outlined 图标
                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                contentDescription = destination.label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = localizedLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}