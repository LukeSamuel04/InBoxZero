package com.example.inboxzero.ui.screens.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.inboxzero.ui.components.charts.DailyBarData
import com.example.inboxzero.ui.components.charts.DonutSliceData
import com.example.inboxzero.ui.components.charts.InsightBarChart
import com.example.inboxzero.ui.components.charts.InsightCollapsibleCard
import com.example.inboxzero.ui.components.charts.InsightDonutChart
import com.example.inboxzero.ui.components.charts.SegmentedPillRow
import com.example.inboxzero.ui.components.charts.StatSummaryRow
import com.example.inboxzero.ui.theme.LocalAppStrings
import com.example.inboxzero.ui.theme.MorandiAmber
import com.example.inboxzero.ui.theme.MorandiBlue
import com.example.inboxzero.ui.theme.MorandiGreen
import com.example.inboxzero.ui.theme.MorandiPurple
import com.example.inboxzero.ui.theme.MorandiRed
import com.example.inboxzero.ui.theme.MorandiTeal

/**
 * InboxZero 统计与心智洞察主屏幕 (Tab 1: Analytics / Insights Screen)
 *
 * 核心 HCI 架构设计：
 * 1. 首屏一屏尽览 (Above-the-Fold)：采用“上折 (BASE METRICS) + 中展 (环形图) + 下折 (趋势柱状图)”的渐进式披露布局。
 * 2. 全量多语言响应式：所有图表维度、图例标签、星期轴均绑定 LocalAppStrings，支持中/英/瑞三语零延迟切换。
 */
@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    // --- 1. 三大模块的折叠/展开状态 (使用 rememberSaveable 跨 Tab 保持状态) ---
    var isTopMetricsExpanded by rememberSaveable { mutableStateOf(false) }
    var isDonutExpanded by rememberSaveable { mutableStateOf(true) }
    var isBarExpanded by rememberSaveable { mutableStateOf(false) }

    // --- 2. 图表内部交互状态 ---
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedSliceIndex by remember { mutableStateOf<Int?>(null) }
    var selectedBarIndex by remember { mutableStateOf<Int?>(6) } // 默认高亮最后一天 (周日)

    // --- 3. 根据当前选中维度与当前语言，动态组装莫兰迪环形图扇区数据 ---
    val currentSlices = remember(selectedTabIndex, strings) {
        val uCount = strings.insightUnitCount
        val uMin = strings.insightUnitMin
        when (selectedTabIndex) {
            0 -> listOf(
                DonutSliceData("high", strings.sliceHighUrgency, 18f, "18 $uCount (13%)", MorandiRed),
                DonutSliceData("med", strings.sliceMedUrgency, 27f, "27 $uCount (19%)", MorandiAmber),
                DonutSliceData("low", strings.sliceLowUrgency, 97f, "97 $uCount (68%)", MorandiGreen)
            )
            1 -> listOf(
                DonutSliceData("wechat", strings.slicePlatformWeChat, 62f, "62 $uCount (44%)", MorandiBlue),
                DonutSliceData("mail", strings.slicePlatformMail, 34f, "34 $uCount (24%)", MorandiAmber),
                DonutSliceData("social", strings.slicePlatformSocial, 28f, "28 $uCount (20%)", MorandiPurple),
                DonutSliceData("sys", strings.slicePlatformSystem, 18f, "18 $uCount (12%)", MorandiTeal)
            )
            else -> listOf(
                DonutSliceData("t_wechat", strings.sliceTimeWeChat, 48f, "48 $uMin (52%)", MorandiBlue),
                DonutSliceData("t_mail", strings.sliceTimeMail, 26f, "26 $uMin (28%)", MorandiAmber),
                DonutSliceData("t_other", strings.sliceTimeOther, 18f, "18 $uMin (20%)", MorandiPurple)
            )
        }
    }

    // --- 4. 根据当前语言，动态组装近 7 日趋势柱状图数据 ---
    val dailyBars = remember(strings) {
        val rawMinutes = listOf(38f, 45f, 29f, 52f, 34f, 22f, 19f)
        strings.weekDaysShort.mapIndexed { index, dayName ->
            val mins = rawMinutes.getOrElse(index) { 30f }
            DailyBarData(
                dayLabel = dayName,
                value = mins,
                topValueText = "${mins.toInt()}m"
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // ==========================================
        // 模块一：顶部心智减负双指标卡 (BASE METRICS)
        // ==========================================
        StatSummaryRow(
            sectionTitle = strings.insightSectionBaseMetrics,
            leftTitle = strings.insightFoldRateTitle,
            leftValue = "68.4%",
            leftSubtitle = strings.insightFoldRateSub,
            rightTitle = strings.insightTimeSavedTitle,
            rightValue = "+42m",
            rightSubtitle = strings.insightTimeSavedSub,
            isExpanded = isTopMetricsExpanded,
            onExpandChange = { isTopMetricsExpanded = it }
        )

        // ==========================================
        // 模块二：中部多维构成环形图 (默认展开)
        // ==========================================
        InsightCollapsibleCard(
            title = strings.insightSectionComposition,
            summaryLeftText = strings.insightDonutSummaryLeft.getOrElse(selectedTabIndex) { "" },
            summaryRightHighlight = strings.insightDonutSummaryRight.getOrElse(selectedTabIndex) { "" },
            isExpanded = isDonutExpanded,
            onExpandChange = { isDonutExpanded = it }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                SegmentedPillRow(
                    options = strings.insightTabOptions,
                    selectedIndex = selectedTabIndex,
                    onOptionSelected = {
                        selectedTabIndex = it
                        selectedSliceIndex = null
                    }
                )

                InsightDonutChart(
                    slices = currentSlices,
                    centerDefaultTitle = if (selectedTabIndex == 2) {
                        strings.insightCenterTitleTime
                    } else {
                        strings.insightCenterTitleCount
                    },
                    centerDefaultValue = if (selectedTabIndex == 2) {
                        "92 ${strings.insightUnitMin}"
                    } else {
                        "142 ${strings.insightUnitCount}"
                    },
                    selectedSliceIndex = selectedSliceIndex,
                    onSliceSelected = { selectedSliceIndex = it },
                    chartSize = 185.dp
                )
            }
        }

        // ==========================================
        // 模块三：底部每日趋势柱状图 (默认半折叠露出摘要)
        // ==========================================
        InsightCollapsibleCard(
            title = strings.insightSectionDailyTrend,
            summaryLeftText = strings.insightBarSummaryLeft,
            summaryRightHighlight = strings.insightBarSummaryRight,
            isExpanded = isBarExpanded,
            onExpandChange = { isBarExpanded = it }
        ) {
            InsightBarChart(
                bars = dailyBars,
                selectedBarIndex = selectedBarIndex,
                onBarSelected = { selectedBarIndex = it },
                averageValue = 34f,
                averageLabel = "${strings.insightBarAvgPrefix} 34m"
            )
        }

        // 底部留白，供悬浮胶囊导航栏展示毛玻璃透视效果
        Spacer(modifier = Modifier.height(96.dp))
    }
}