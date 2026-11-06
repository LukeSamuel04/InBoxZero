package com.example.inboxzero.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.inboxzero.ui.theme.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * InboxZero 全局用户偏好与实验参数磁盘持久化管家
 *
 * 核心职责：
 * 1. 使用 Android 原生 SharedPreferences 将设置项物理写入手机闪存 (inboxzero_prefs.xml)，杀后台/重启手机绝不丢失。
 * 2. 对外暴露响应式 StateFlow，让 Compose UI 保持毫秒级无闪烁热更新，同时供后台通知监听服务随时读取实验参数。
 */
class UserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        "inboxzero_user_prefs",
        Context.MODE_PRIVATE
    )

    // --- 1. 界面语言 (ZH / EN / SV) ---
    private val _language = MutableStateFlow(loadLanguage())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private fun loadLanguage(): AppLanguage {
        val savedName = prefs.getString(KEY_LANGUAGE, AppLanguage.ZH.name) ?: AppLanguage.ZH.name
        return runCatching { AppLanguage.valueOf(savedName) }.getOrDefault(AppLanguage.ZH)
    }

    fun setLanguage(newLanguage: AppLanguage) {
        _language.value = newLanguage
        prefs.edit().putString(KEY_LANGUAGE, newLanguage.name).apply()
    }

    // --- 2. 深色模式 (如果用户从未手动设置过，则默认返回 null 以跟随系统) ---
    private val _isDarkMode = MutableStateFlow(loadDarkMode())
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    private fun loadDarkMode(): Boolean? {
        return if (prefs.contains(KEY_DARK_MODE)) {
            prefs.getBoolean(KEY_DARK_MODE, false)
        } else null
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    // --- 3. 适老大字与加粗档位索引 (0: 1.0x, 1: 1.2x, 2: 1.35x) ---
    private val _fontScaleIndex = MutableStateFlow(prefs.getInt(KEY_FONT_SCALE_INDEX, 0))
    val fontScaleIndex: StateFlow<Int> = _fontScaleIndex.asStateFlow()

    fun setFontScaleIndex(index: Int) {
        _fontScaleIndex.value = index
        prefs.edit().putInt(KEY_FONT_SCALE_INDEX, index).apply()
    }

    // --- 4. AI 混合紧急度引擎总开关 (实验组 vs 对照组) ---
    private val _isAiEngineEnabled = MutableStateFlow(prefs.getBoolean(KEY_AI_ENGINE, true))
    val isAiEngineEnabled: StateFlow<Boolean> = _isAiEngineEnabled.asStateFlow()

    fun setAiEngineEnabled(enabled: Boolean) {
        _isAiEngineEnabled.value = enabled
        prefs.edit().putBoolean(KEY_AI_ENGINE, enabled).apply()
    }

    // --- 5. 自动折叠低优杂音开关 ---
    private val _isAutoFoldNoise = MutableStateFlow(prefs.getBoolean(KEY_AUTO_FOLD, true))
    val isAutoFoldNoise: StateFlow<Boolean> = _isAutoFoldNoise.asStateFlow()

    fun setAutoFoldNoise(enabled: Boolean) {
        _isAutoFoldNoise.value = enabled
        prefs.edit().putBoolean(KEY_AUTO_FOLD, enabled).apply()
    }

    // --- 6. 免打扰过滤阈值档位索引 (0: 20分, 1: 40分, 2: 60分) ---
    private val _thresholdIndex = MutableStateFlow(prefs.getInt(KEY_THRESHOLD_INDEX, 1))
    val thresholdIndex: StateFlow<Int> = _thresholdIndex.asStateFlow()

    fun setThresholdIndex(index: Int) {
        _thresholdIndex.value = index
        prefs.edit().putInt(KEY_THRESHOLD_INDEX, index).apply()
    }

    /**
     * 供后台算分或看板过滤直接读取当前实际的阈值分数 (20 / 40 / 60)
     */
    fun getCurrentThresholdScore(): Int {
        return when (_thresholdIndex.value) {
            0 -> 20
            1 -> 40
            2 -> 60
            else -> 40
        }
    }

    // --- 7. 全局触觉反馈开关 ---
    private val _isHapticEnabled = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC, true))
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    fun setHapticEnabled(enabled: Boolean) {
        _isHapticEnabled.value = enabled
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
    }

    companion object {
        private const val KEY_LANGUAGE = "pref_language"
        private const val KEY_DARK_MODE = "pref_dark_mode"
        private const val KEY_FONT_SCALE_INDEX = "pref_font_scale_index"
        private const val KEY_AI_ENGINE = "pref_ai_engine_enabled"
        private const val KEY_AUTO_FOLD = "pref_auto_fold_noise"
        private const val KEY_THRESHOLD_INDEX = "pref_threshold_index"
        private const val KEY_HAPTIC = "pref_haptic_enabled"
    }
}