package com.example.inboxzero.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.inboxzero.ui.theme.LocalAppStrings
import kotlinx.coroutines.launch

/**
 * InboxZero 全局主壳界面 (Main Scaffold)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScaffold(
    modifier: Modifier = Modifier,
    // 默认占位页直接读取当前语言的导航标题
    inboxPageContent: @Composable () -> Unit = {
        PlaceholderPage(title = LocalAppStrings.current.navInbox)
    },
    analyticsPageContent: @Composable () -> Unit = {
        PlaceholderPage(title = LocalAppStrings.current.navInsights)
    },
    settingsPageContent: @Composable () -> Unit = {
        PlaceholderPage(title = LocalAppStrings.current.navSettings)
    }
) {
    val pagerState = rememberPagerState(
        initialPage = AppDestination.INBOX.pageIndex,
        pageCount = { AppDestination.entries.size }
    )
    val coroutineScope = rememberCoroutineScope()
    val currentDestination = AppDestination.fromPageIndex(pagerState.currentPage)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            beyondViewportPageCount = 1
        ) { pageIndex ->
            when (AppDestination.fromPageIndex(pageIndex)) {
                AppDestination.INBOX -> inboxPageContent()
                AppDestination.ANALYTICS -> analyticsPageContent()
                AppDestination.SETTINGS -> settingsPageContent()
            }
        }

        FloatingBottomBar(
            currentDestination = currentDestination,
            onTabSelected = { targetDestination ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(targetDestination.pageIndex)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * 临时占位页面组件（已接入 LocalAppStrings 多语言词典）
 */
@Composable
private fun PlaceholderPage(title: String) {
    val strings = LocalAppStrings.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = strings.placeholderSwipeHint, // ✨ 动态读取三语滑动提示！
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}