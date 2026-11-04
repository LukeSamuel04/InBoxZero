package com.example.inboxzero.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.inboxzero.InboxZeroApplication
import com.example.inboxzero.data.local.UserPreferences
import com.example.inboxzero.ui.components.dropdowns.SecondaryDropdown
import com.example.inboxzero.ui.components.settings.SettingActionItem
import com.example.inboxzero.ui.components.settings.SettingItemBase
import com.example.inboxzero.ui.components.settings.SettingSectionCard
import com.example.inboxzero.ui.components.settings.SettingSwitchItem
import com.example.inboxzero.ui.theme.AppLanguage
import com.example.inboxzero.ui.theme.LocalAppStrings
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    userPreferences: UserPreferences,
    isDarkModeEffective: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val strings = LocalAppStrings.current

    // 1. 从 UserPreferences 收集所有持久化状态 (杀后台重进依然保留！)
    val currentLanguage by userPreferences.language.collectAsState()
    val fontScaleIndex by userPreferences.fontScaleIndex.collectAsState()
    val isAiEngineEnabled by userPreferences.isAiEngineEnabled.collectAsState()
    val isAutoFoldNoise by userPreferences.isAutoFoldNoise.collectAsState()
    val selectedThresholdIndex by userPreferences.thresholdIndex.collectAsState()
    val isHapticEnabled by userPreferences.isHapticEnabled.collectAsState()

    // 2. 系统通知监听权限实时检测
    var isNotificationListenerEnabled by remember {
        mutableStateOf(checkNotificationListenerPermission(context))
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isNotificationListenerEnabled = checkNotificationListenerPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 3. 防呆确认弹窗状态 (弹窗本身不需要持久化到磁盘)
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    val languageOptions = remember { AppLanguage.entries.map { it.displayName } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ==========================================
        // 分区一：服务与权限状态
        // ==========================================
        SettingSectionCard(title = strings.sectionService) {
            SettingItemBase(
                title = strings.itemNotificationService,
                subtitle = strings.itemNotificationServiceSub,
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                },
                trailingContent = {
                    Text(
                        text = if (isNotificationListenerEnabled) strings.statusRunning else strings.statusUnauthorized,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (isNotificationListenerEnabled) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        }
                    )
                }
            )
        }

        // ==========================================
        // 分区二：AI 引擎与过滤策略
        // ==========================================
        SettingSectionCard(title = strings.sectionAiEngine) {
            SettingSwitchItem(
                title = strings.itemAiEngine,
                subtitle = if (isAiEngineEnabled) strings.itemAiEngineSubOn else strings.itemAiEngineSubOff,
                isChecked = isAiEngineEnabled,
                onCheckedChange = { userPreferences.setAiEngineEnabled(it) },
                usePrimarySwitch = true,
                thumbIcon = Icons.Filled.AutoAwesome
            )

            SettingSwitchItem(
                title = strings.itemAutoFold,
                subtitle = strings.itemAutoFoldSub,
                isChecked = isAutoFoldNoise,
                onCheckedChange = { userPreferences.setAutoFoldNoise(it) },
                enabled = isAiEngineEnabled
            )

            SettingItemBase(
                title = strings.itemThreshold,
                subtitle = strings.itemThresholdSub,
                trailingContent = {
                    val currentThresholdText = strings.thresholdOptions.getOrElse(selectedThresholdIndex) { strings.thresholdOptions[0] }
                    SecondaryDropdown(
                        options = strings.thresholdOptions,
                        selectedOption = currentThresholdText,
                        onOptionSelected = { selectedText ->
                            val newIndex = strings.thresholdOptions.indexOf(selectedText)
                            if (newIndex != -1) userPreferences.setThresholdIndex(newIndex)
                        }
                    )
                }
            )
        }

        // ==========================================
        // 分区三：外观与无障碍交互
        // ==========================================
        SettingSectionCard(title = strings.sectionAppearance) {
            SettingItemBase(
                title = strings.itemLanguage,
                subtitle = strings.itemLanguageSub,
                trailingContent = {
                    SecondaryDropdown(
                        options = languageOptions,
                        selectedOption = currentLanguage.displayName,
                        onOptionSelected = { selectedName ->
                            userPreferences.setLanguage(AppLanguage.fromDisplayName(selectedName))
                        }
                    )
                }
            )

            SettingSwitchItem(
                title = strings.itemDarkMode,
                subtitle = strings.itemDarkModeSub,
                isChecked = isDarkModeEffective,
                onCheckedChange = { userPreferences.setDarkMode(it) },
                usePrimarySwitch = true,
                thumbIcon = if (isDarkModeEffective) Icons.Filled.DarkMode else Icons.Filled.LightMode
            )

            SettingItemBase(
                title = strings.itemFontScale,
                subtitle = strings.itemFontScaleSub,
                trailingContent = {
                    val currentFontText = strings.fontScaleOptions.getOrElse(fontScaleIndex) { strings.fontScaleOptions[0] }
                    SecondaryDropdown(
                        options = strings.fontScaleOptions,
                        selectedOption = currentFontText,
                        onOptionSelected = { selectedText ->
                            val newIndex = strings.fontScaleOptions.indexOf(selectedText)
                            if (newIndex != -1) userPreferences.setFontScaleIndex(newIndex)
                        }
                    )
                }
            )

            SettingSwitchItem(
                title = strings.itemHaptic,
                subtitle = strings.itemHapticSub,
                isChecked = isHapticEnabled,
                onCheckedChange = { userPreferences.setHapticEnabled(it) }
            )
        }

        // ==========================================
        // 分区四：实验数据与系统操作
        // ==========================================
        SettingSectionCard(title = strings.sectionData) {
            SettingActionItem(
                title = strings.itemExportCsv,
                subtitle = strings.itemExportCsvSub,
                onClick = {
                    Toast.makeText(context, strings.toastExportPending, Toast.LENGTH_SHORT).show()
                }
            )

            SettingActionItem(
                title = strings.itemClearDb,
                subtitle = strings.itemClearDbSub,
                titleColor = MaterialTheme.colorScheme.error,
                onClick = { showClearConfirmDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(96.dp))
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = strings.dialogClearTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = strings.dialogClearMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmDialog = false
                        coroutineScope.launch {
                            val app = context.applicationContext as? InboxZeroApplication
                            app?.database?.messageDao()?.clearAllMessages()
                            Toast.makeText(context, strings.toastDbCleared, Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(
                        text = strings.dialogConfirm,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(
                        text = strings.dialogCancel,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}

private fun checkNotificationListenerPermission(context: Context): Boolean {
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat != null && flat.contains(context.packageName)
}