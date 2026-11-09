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
import com.example.inboxzero.ui.components.analytics.DailyBarData
import com.example.inboxzero.ui.components.analytics.DonutSliceData
import com.example.inboxzero.ui.components.analytics.InsightBarChart
import com.example.inboxzero.ui.components.analytics.InsightDonutChart
import com.example.inboxzero.ui.components.analytics.SegmentedPillRow
import com.example.inboxzero.ui.components.analytics.StatSummaryRow
import com.example.inboxzero.ui.components.shared.containers.CollapsibleContainer
import com.example.inboxzero.ui.theme.LocalAppStrings
import com.example.inboxzero.ui.theme.MorandiAmber
import com.example.inboxzero.ui.theme.MorandiBlue
import com.example.inboxzero.ui.theme.MorandiGreen
import com.example.inboxzero.ui.theme.MorandiPurple
import com.example.inboxzero.ui.theme.MorandiRed
import com.example.inboxzero.ui.theme.MorandiTeal

/**
 * InboxZero 统计与心智洞察主屏幕 (Tab 1: Analytics / Insights Screen)
 */
@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    var isTopMetricsExpanded by rememberSaveable { mutableStateOf(false) }
    var isDonutExpanded by rememberSaveable { mutableStateOf(true) }
    var isBarExpanded by rememberSaveable { mutableStateOf(false) }

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedSliceIndex by remember { mutableStateOf<Int?>(null) }
    var selectedBarIndex by remember { mutableStateOf<Int?>(6) }

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
        // 模块一：顶部心智减负双指标卡 (BASE METRICS)
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

        // 模块二：中部多维构成环形图 (复用 CollapsibleContainer)
        CollapsibleContainer(
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

        // 模块三：底部每日趋势柱状图 (复用 CollapsibleContainer)
        CollapsibleContainer(
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

        Spacer(modifier = Modifier.height(96.dp))
    }
}