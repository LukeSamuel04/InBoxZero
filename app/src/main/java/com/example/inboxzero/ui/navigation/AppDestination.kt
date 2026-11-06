package com.example.inboxzero.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Drafts
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * InboxZero 全局底部导航路由字典
 *
 * 核心职责：
 * 1. 严格定义 HorizontalPager 的页面顺序 (pageIndex: 0, 1, 2)。
 * 2. 提供双态图标支持 (未选中时使用轻盈的 Outlined 线条，选中时切换为厚重的 Filled 实心图标)。
 */
enum class AppDestination(
    val pageIndex: Int,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    // Tab 0: 智能优先级看板 (使用 Drafts 打开的信封图标)
    INBOX(
        pageIndex = 0,
        label = "Inbox",
        selectedIcon = Icons.Filled.Drafts,
        unselectedIcon = Icons.Outlined.Drafts
    ),

    // Tab 1: 统计与心智洞察页 (使用 BarChart 纯柱状图图标)
    ANALYTICS(
        pageIndex = 1,
        label = "Insights",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    ),

    // Tab 2: 实验控制与规则设置页 (使用 Settings 经典齿轮图标)
    SETTINGS(
        pageIndex = 2,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    );

    companion object {
        /**
         * 根据 HorizontalPager 的当前页码反向查找对应的路由枚举
         */
        fun fromPageIndex(index: Int): AppDestination {
            return entries.firstOrNull { it.pageIndex == index } ?: INBOX
        }
    }
}