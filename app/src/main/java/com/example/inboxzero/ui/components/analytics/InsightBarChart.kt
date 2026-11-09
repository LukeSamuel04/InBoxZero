package com.example.inboxzero.ui.components.analytics

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 每日趋势柱状图单根柱子的数据契约
 */
data class DailyBarData(
    val dayLabel: String,       // 横轴星期标签（如："周一" / "Mon"）
    val value: Float,           // 数值（分钟数或消息条数）
    val topValueText: String    // 柱顶展示的简短文本（如："32m" 或 "45"）
)

/**
 * InboxZero 极简圆角趋势柱状图 (Insight Bar Chart)
 *
 * 核心 HCI 设计特性：
 * 1. 零网格线降噪：去除传统图表繁杂的 X/Y 坐标轴刻度线，采用柔和的胶囊轨道槽提供高度参照。
 * 2. 日均基线锚定：支持绘制一条贯穿全图的莫兰迪色水平虚线 (Average Line)，一眼判断当天负荷是否超标。
 * 3. 点按反馈：点击任意一天的柱体可将其高亮为深莫兰迪色，并突出显示顶部具体数值。
 */
@Composable
fun InsightBarChart(
    bars: List<DailyBarData>,
    selectedBarIndex: Int?,
    onBarSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 160.dp,
    averageValue: Float? = null,
    averageLabel: String = "Avg",
    activeBarColor: Color = MaterialTheme.colorScheme.primary,
    inactiveBarColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.50f)
) {
    val maxValue = remember(bars) {
        (bars.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f) * 1.15f // 顶部预留 15% 呼吸空间
    }

    val trackBackgroundColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.07f)
    val avgLineColor = MaterialTheme.colorScheme.secondary

    Column(modifier = modifier.fillMaxWidth()) {
        // --- 1. 图表主繪制区（背景日均虚线 + 7 根等距胶囊柱） ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            // 底层：如果传入了平均值，绘制贯穿横向的日均水平参考虚线
            if (averageValue != null && averageValue > 0f) {
                val avgRatio = (averageValue / maxValue).coerceIn(0f, 1f)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // 扣除顶部数字标签占用的约 24.dp 高度后计算虚线 Y 坐标
                    val topReservedPx = 24.dp.toPx()
                    val availableBarHeight = size.height - topReservedPx
                    val lineY = topReservedPx + availableBarHeight * (1f - avgRatio)

                    drawLine(
                        color = avgLineColor.copy(alpha = 0.75f),
                        start = Offset(0f, lineY),
                        end = Offset(size.width, lineY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    )
                }
            }

            // 顶层：7 根交互式胶囊柱体
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                bars.forEachIndexed { index, bar ->
                    val isSelected = (selectedBarIndex == index)
                    val targetRatio = (bar.value / maxValue).coerceIn(0.06f, 1f)

                    // 柱子生长高度动画
                    val animatedRatio by animateFloatAsState(
                        targetValue = targetRatio,
                        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
                        label = "BarHeight_$index"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    onBarSelected(if (isSelected) null else index)
                                }
                            )
                    ) {
                        // 柱顶数值小字（被选中时加粗变亮，未选中时保持克制的浅灰）
                        Text(
                            text = bar.topValueText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) activeBarColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        // 胶囊轨道与实心圆角柱画布
                        Canvas(
                            modifier = Modifier
                                .width(22.dp)
                                .weight(1f)
                        ) {
                            val cornerRadius = CornerRadius(size.width / 2f, size.width / 2f)

                            // A. 绘制满高浅灰温度计背景轨道槽
                            drawRoundRect(
                                color = trackBackgroundColor,
                                topLeft = Offset.Zero,
                                size = size,
                                cornerRadius = cornerRadius
                            )

                            // B. 绘制从底部向上生长的莫兰迪数据柱
                            val barHeight = size.height * animatedRatio
                            val topY = size.height - barHeight
                            drawRoundRect(
                                color = if (isSelected) activeBarColor else inactiveBarColor,
                                topLeft = Offset(0f, topY),
                                size = Size(size.width, barHeight),
                                cornerRadius = cornerRadius
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 2. 底部横轴星期标签行 ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            bars.forEachIndexed { index, bar ->
                val isSelected = (selectedBarIndex == index)
                Text(
                    text = bar.dayLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) activeBarColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- 3. 底部日均虚线图例微说明 ---
        if (averageValue != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "--- $averageLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = avgLineColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}