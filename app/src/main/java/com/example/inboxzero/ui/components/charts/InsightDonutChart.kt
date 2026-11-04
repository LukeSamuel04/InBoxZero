package com.example.inboxzero.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

/**
 * 环形图单个扇区的数据契约
 */
data class DonutSliceData(
    val id: String,
    val label: String,          // 分类名称（如：高优核心 / 微信）
    val value: Float,           // 原始数值（用于计算角度比例）
    val formattedValue: String, // 选中时展示的具体数值文本（如："45 条 (32%)"）
    val color: Color            // 莫兰迪色值
)

/**
 * InboxZero 莫兰迪交互式环形饼图 (Insight Donut Chart)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InsightDonutChart(
    slices: List<DonutSliceData>,
    centerDefaultTitle: String,
    centerDefaultValue: String,
    selectedSliceIndex: Int?,
    onSliceSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    chartSize: Dp = 200.dp,
    baseStrokeWidth: Dp = 26.dp
) {
    val totalValue = remember(slices) { slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f) }

    // 每次切换数据源 (slices) 时，触发 0 -> 1 的顺时针展开过渡动画
    val sweepProgress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        sweepProgress.snapTo(0f)
        sweepProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    // 预先计算每个扇区的起始角度与扫过角度（从正上方 -90° 开始绘制）
    val gapAngle = if (slices.size > 1) 4f else 0f
    val sliceAngles = remember(slices, totalValue) {
        var currentStart = -90f
        slices.map { slice ->
            val rawSweep = (slice.value / totalValue) * 360f
            val actualSweep = (rawSweep - gapAngle).coerceAtLeast(1f)
            val range = currentStart to actualSweep
            currentStart += rawSweep
            range
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. 上半部分：圆环画布 + 中心镂空动态文字 ---
        Box(
            modifier = Modifier.size(chartSize + 24.dp),
            contentAlignment = Alignment.Center
        ) {
            val strokeWidths = slices.indices.map { index ->
                val isSelected = (selectedSliceIndex == index)
                animateDpAsState(
                    targetValue = if (isSelected) baseStrokeWidth + 7.dp else baseStrokeWidth,
                    animationSpec = tween(250),
                    label = "DonutStroke_$index"
                ).value
            }

            val alphas = slices.indices.map { index ->
                val isAlphaDimmed = (selectedSliceIndex != null && selectedSliceIndex != index)
                animateFloatAsState(
                    targetValue = if (isAlphaDimmed) 0.35f else 1f,
                    animationSpec = tween(250),
                    label = "DonutAlpha_$index"
                ).value
            }

            Canvas(
                modifier = Modifier
                    .size(chartSize)
                    .pointerInput(slices, selectedSliceIndex) {
                        detectTapGestures { tapOffset ->
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val dx = tapOffset.x - centerX
                            val dy = tapOffset.y - centerY
                            val distance = hypot(dx, dy)

                            // ✅ 修复：使用 min(size.width, size.height) 兼容 IntSize
                            val minDim = min(size.width, size.height).toFloat()
                            val outerRadius = minDim / 2f + 16.dp.toPx()
                            val innerRadius = minDim / 2f - baseStrokeWidth.toPx() - 16.dp.toPx()

                            if (distance in innerRadius..outerRadius) {
                                var touchAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (touchAngle < -90f) touchAngle += 360f

                                val hitIndex = sliceAngles.indexOfFirst { (start, sweep) ->
                                    touchAngle >= start && touchAngle <= (start + sweep + gapAngle)
                                }

                                if (hitIndex != -1) {
                                    onSliceSelected(if (selectedSliceIndex == hitIndex) null else hitIndex)
                                }
                            } else {
                                onSliceSelected(null)
                            }
                        }
                    }
            ) {
                val canvasSize = size.minDimension
                slices.forEachIndexed { index, slice ->
                    val (startAngle, sweepAngle) = sliceAngles[index]
                    val currentStrokePx = strokeWidths[index].toPx()
                    val diameter = canvasSize - currentStrokePx
                    val topLeftOffset = currentStrokePx / 2f

                    drawArc(
                        color = slice.color.copy(alpha = alphas[index]),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle * sweepProgress.value,
                        useCenter = false,
                        topLeft = Offset(topLeftOffset, topLeftOffset),
                        size = Size(diameter, diameter),
                        style = Stroke(width = currentStrokePx, cap = StrokeCap.Butt)
                    )
                }
            }

            // 圆环正中央的动态汇总文本
            val activeSlice = selectedSliceIndex?.let { slices.getOrNull(it) }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 36.dp)
            ) {
                Text(
                    text = activeSlice?.label ?: centerDefaultTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activeSlice?.formattedValue ?: centerDefaultValue,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = activeSlice?.color ?: MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. 下半部分：自适应流式换行图例 (FlowRow Legend) ---
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slices.forEachIndexed { index, slice ->
                val isSelected = (selectedSliceIndex == index)
                val percentage = ((slice.value / totalValue) * 100).toInt()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isSelected) slice.color.copy(alpha = 0.16f)
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.06f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) slice.color else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onSliceSelected(if (isSelected) null else index)
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(slice.color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${slice.label} $percentage%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}