package com.example.inboxzero.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 支持的语言枚举 (中文 / 英文 / 瑞典语)
 */
enum class AppLanguage(val displayName: String) {
    ZH("简体中文"),
    EN("English"),
    SV("Svenska");

    companion object {
        fun fromDisplayName(name: String): AppLanguage {
            return entries.firstOrNull { it.displayName == name } ?: ZH
        }
    }
}

/**
 * 全局强类型多语言词典结构 (代替容易拼错的 String Key 映射表)
 */
data class AppStrings(
    // 1. 底部导航栏与占位页
    val navInbox: String,
    val navInsights: String,
    val navSettings: String,
    val placeholderSwipeHint: String,

    // 2. 分区一：服务与权限
    val sectionService: String,
    val itemNotificationService: String,
    val itemNotificationServiceSub: String,
    val statusRunning: String,
    val statusUnauthorized: String,

    // 3. 分区二：AI 引擎与过滤策略
    val sectionAiEngine: String,
    val itemAiEngine: String,
    val itemAiEngineSubOn: String,
    val itemAiEngineSubOff: String,
    val itemAutoFold: String,
    val itemAutoFoldSub: String,
    val itemThreshold: String,
    val itemThresholdSub: String,
    val thresholdOptions: List<String>,

    // 4. 分区三：外观与无障碍交互
    val sectionAppearance: String,
    val itemLanguage: String,
    val itemLanguageSub: String,
    val itemDarkMode: String,
    val itemDarkModeSub: String,
    val itemFontScale: String,
    val itemFontScaleSub: String,
    val fontScaleOptions: List<String>,
    val itemHaptic: String,
    val itemHapticSub: String,

    // 5. 分区四：实验数据与系统操作
    val sectionData: String,
    val itemExportCsv: String,
    val itemExportCsvSub: String,
    val toastExportPending: String,
    val itemClearDb: String,
    val itemClearDbSub: String,

    // 6. 防呆二次确认弹窗
    val dialogClearTitle: String,
    val dialogClearMessage: String,
    val dialogConfirm: String,
    val dialogCancel: String,
    val toastDbCleared: String
)

// ==========================================
// 🇨🇳 简体中文词典
// ==========================================
val ZhStrings = AppStrings(
    navInbox = "看板",
    navInsights = "统计",
    navSettings = "设置",
    placeholderSwipeHint = "👈 尝试左右滑动屏幕，或点击下方悬浮胶囊切换 👉",

    sectionService = "服务与权限状态",
    itemNotificationService = "通知监听服务",
    itemNotificationServiceSub = "拦截系统通知并写入本地安全数据库的核心管线",
    statusRunning = "● 运行中",
    statusUnauthorized = "● 点击授权",

    sectionAiEngine = "AI 引擎与过滤策略",
    itemAiEngine = "AI 混合紧急度引擎",
    itemAiEngineSubOn = "已启用 LLM + 泊松规则混合排序 (实验组模式)",
    itemAiEngineSubOff = "已关闭 AI 干预，按时间倒序排列 (Baseline 模式)",
    itemAutoFold = "自动折叠低优杂音",
    itemAutoFoldSub = "将评分低于阈值的群聊与营销通知静默收纳至底部",
    itemThreshold = "免打扰过滤阈值",
    itemThresholdSub = "设定判定为“低优杂音”的紧急度分数分界线",
    thresholdOptions = listOf("宽松 (20分)", "标准 (40分)", "严格 (60分)"),

    sectionAppearance = "外观与无障碍交互",
    itemLanguage = "界面语言 (Language)",
    itemLanguageSub = "切换应用显示语言 (支持中 / 英 / 瑞典语)",
    itemDarkMode = "深色模式",
    itemDarkModeSub = "强制开启深色防眩光主题界面",
    itemFontScale = "适老大字与加粗模式",
    itemFontScaleSub = "全局放大字号并增加字重，降低长辈阅读视觉疲劳",
    fontScaleOptions = listOf("标准 (1.0x)", "大号加粗 (1.2x)", "特大关怀 (1.35x)"),
    itemHaptic = "全局触觉反馈",
    itemHapticSub = "转动表盘或点击按钮时的马达震动",

    sectionData = "实验数据与系统操作",
    itemExportCsv = "导出实验行为日志",
    itemExportCsvSub = "将脱敏后的紧急度评分与交互时延导出为 CSV 文件",
    toastExportPending = "日志导出功能将在统计模块完成后接入",
    itemClearDb = "清空本地消息数据库",
    itemClearDbSub = "物理抹除所有已拦截的历史通知记录（不可逆）",

    dialogClearTitle = "确认清空本地数据库？",
    dialogClearMessage = "此操作将物理删除 messages 表中已收集的所有历史通知与 AI 评分记录，且无法恢复。\n\n在 User Study 实验进行期间请勿轻易执行。",
    dialogConfirm = "确认清空",
    dialogCancel = "取消",
    toastDbCleared = "✅ 本地消息数据库已彻底清空"
)

// ==========================================
// 🇬🇧 英文词典 (English)
// ==========================================
val EnStrings = AppStrings(
    navInbox = "Inbox",
    navInsights = "Insights",
    navSettings = "Settings",
    placeholderSwipeHint = "👈 Swipe left/right or tap the floating pill bar below 👉",

    sectionService = "Service & Permissions",
    itemNotificationService = "Notification Listener",
    itemNotificationServiceSub = "Core pipeline for intercepting and storing notifications locally",
    statusRunning = "● Active",
    statusUnauthorized = "● Grant Access",

    sectionAiEngine = "AI Engine & Filtering (User Study)",
    itemAiEngine = "Hybrid AI Urgency Engine",
    itemAiEngineSubOn = "LLM + Poisson hybrid ranking enabled (Experimental Group)",
    itemAiEngineSubOff = "AI disabled, sorted chronologically (Baseline Group)",
    itemAutoFold = "Auto-Fold Low Priority Noise",
    itemAutoFoldSub = "Silently group notifications below the urgency threshold",
    itemThreshold = "Noise Filter Threshold",
    itemThresholdSub = "Score cutoff for folding low-priority notifications",
    thresholdOptions = listOf("Lenient (20)", "Standard (40)", "Strict (60)"),

    sectionAppearance = "Appearance & Accessibility",
    itemLanguage = "App Language",
    itemLanguageSub = "Switch interface language (ZH / EN / SV)",
    itemDarkMode = "Dark Mode",
    itemDarkModeSub = "Force low-glare dark theme interface",
    itemFontScale = "Senior Large & Bold Text",
    itemFontScaleSub = "Scale up font size and weight to reduce visual fatigue",
    fontScaleOptions = listOf("Standard (1.0x)", "Large Bold (1.2x)", "Extra Large (1.35x)"),
    itemHaptic = "Global Haptic Feedback",
    itemHapticSub = "Subtle vibration when rotating dials or tapping buttons",

    sectionData = "Study Data & System Actions",
    itemExportCsv = "Export Interaction Logs (CSV)",
    itemExportCsvSub = "Export anonymized urgency scores and latency logs",
    toastExportPending = "CSV export will be connected with the Insights module",
    itemClearDb = "Clear Local Message Database",
    itemClearDbSub = "Permanently wipe all intercepted notification records",

    dialogClearTitle = "Clear Local Database?",
    dialogClearMessage = "This will permanently delete all intercepted notifications and AI scores in the 'messages' table. This action cannot be undone.\n\nPlease do not clear during an active User Study.",
    dialogConfirm = "Clear All",
    dialogCancel = "Cancel",
    toastDbCleared = "✅ Local message database cleared"
)

// ==========================================
// 🇸🇪 瑞典语词典 (Svenska)
// ==========================================
val SvStrings = AppStrings(
    navInbox = "Inkorg",
    navInsights = "Insikter",
    navSettings = "Inställningar",
    placeholderSwipeHint = "👈 Svep åt sidan eller tryck på menyn nedan 👉",

    sectionService = "Tjänster & Behörigheter",
    itemNotificationService = "Aviseringstjänst",
    itemNotificationServiceSub = "Kärnpipeline för att fånga och lagra aviseringar lokalt",
    statusRunning = "● Aktiv",
    statusUnauthorized = "● Ge behörighet",

    sectionAiEngine = "AI-motor & Filtrering (Studie)",
    itemAiEngine = "Hybrid AI-brådskemotor",
    itemAiEngineSubOn = "LLM + Poisson-rangordning aktiverad (Experimentgrupp)",
    itemAiEngineSubOff = "AI inaktiverad, kronologisk ordning (Baslinjegrupp)",
    itemAutoFold = "Dölj lågprioriterat brus",
    itemAutoFoldSub = "Samla automatiskt aviseringar under tröskelvärdet",
    itemThreshold = "Tröskelvärde för brusfilter",
    itemThresholdSub = "Poänggräns för att dölja mindre viktiga aviseringar",
    thresholdOptions = listOf("Mild (20p)", "Standard (40p)", "Strikt (60p)"),

    sectionAppearance = "Utseende & Tillgänglighet",
    itemLanguage = "Språk (Language)",
    itemLanguageSub = "Byt visningsspråk för appen (ZH / EN / SV)",
    itemDarkMode = "Mörkt läge",
    itemDarkModeSub = "Aktivera mörkt tema för mindre bländning",
    itemFontScale = "Stor & fet text (Seniorläge)",
    itemFontScaleSub = "Förstora text och öka fetstil för enklare läsning",
    fontScaleOptions = listOf("Standard (1.0x)", "Stor fet (1.2x)", "Extra stor (1.35x)"),
    itemHaptic = "Haptisk反馈 (Vibration)",
    itemHapticSub = "Mjuk vibration vid knapptryck och reglage",

    sectionData = "Studiedata & Systemåtgärder",
    itemExportCsv = "Exportera interaktionsloggar (CSV)",
    itemExportCsvSub = "Exportera anonymiserade poäng och svarstider",
    toastExportPending = "CSV-export aktiveras när statistikmodulen är klar",
    itemClearDb = "Rensa lokal meddelandedatabas",
    itemClearDbSub = "Radera alla sparade aviseringar permanent (kan ej ångras)",

    dialogClearTitle = "Rensa lokal databas?",
    dialogClearMessage = "Detta raderar permanent alla sparade aviseringar och AI-poäng i tabellen 'messages'. Åtgärden kan inte ångras.\n\nRensa inte under pågående användarstudie.",
    dialogConfirm = "Rensa allt",
    dialogCancel = "Avbryt",
    toastDbCleared = "✅ Den lokala databasen har rensats"
)

/**
 * 全局 CompositionLocal 管道，默认提供中文词典
 */
val LocalAppStrings = staticCompositionLocalOf { ZhStrings }