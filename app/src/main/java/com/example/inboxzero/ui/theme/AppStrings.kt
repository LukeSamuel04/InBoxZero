package com.example.inboxzero.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 支持的界面语言枚举 (简体中文 / English / Svenska)
 *
 * 核心设计原则：
 * 1. 强类型映射：避免使用容易拼写错误的硬编码字符串作为语言标识。
 * 2. 持久化兼容：枚举的 name ("ZH", "EN", "SV") 直接用于 SharedPreferences 磁盘读写。
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
 * InboxZero 全局强类型多语言词典契约 (AppStrings)
 *
 * 架构优势：
 * 1. 编译期安全：任何新增的 UI 词条都必须在三套语言实例中同时实现，绝不会出现运行时漏翻或崩溃。
 * 2. 零闪烁热切换：配合 CompositionLocal (LocalAppStrings)，用户在设置页切换语言时全站毫秒级重组生效。
 */
data class AppStrings(
    // ==========================================
    // 1. 底部悬浮导航栏与通用占位提示
    // ==========================================
    val navInbox: String,
    val navInsights: String,
    val navSettings: String,
    val placeholderSwipeHint: String,

    // ==========================================
    // 2. 设置页 - 分区一：服务与权限状态
    // ==========================================
    val sectionService: String,
    val itemNotificationService: String,
    val itemNotificationServiceSub: String,
    val statusRunning: String,
    val statusUnauthorized: String,

    // ==========================================
    // 3. 设置页 - 分区二：AI 引擎与过滤策略 (User Study 核心变量)
    // ==========================================
    val sectionAiEngine: String,
    val itemAiEngine: String,
    val itemAiEngineSubOn: String,
    val itemAiEngineSubOff: String,
    val itemAutoFold: String,
    val itemAutoFoldSub: String,
    val itemThreshold: String,
    val itemThresholdSub: String,
    val thresholdOptions: List<String>,

    // ==========================================
    // 4. 设置页 - 分区三：外观与无障碍交互
    // ==========================================
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

    // ==========================================
    // 5. 设置页 - 分区四：实验数据与系统操作
    // ==========================================
    val sectionData: String,
    val itemExportCsv: String,
    val itemExportCsvSub: String,
    val toastExportPending: String,
    val itemClearDb: String,
    val itemClearDbSub: String,

    // ==========================================
    // 6. 防呆二次确认弹窗
    // ==========================================
    val dialogClearTitle: String,
    val dialogClearMessage: String,
    val dialogConfirm: String,
    val dialogCancel: String,
    val toastDbCleared: String,

    // ==========================================
    // 7. 统计与心智洞察页 (Insights / AnalyticsScreen)
    // ==========================================
    // 7.1 顶部核心减负双指标卡 (BASE METRICS)
    val insightSectionBaseMetrics: String,
    val insightFoldRateTitle: String,
    val insightFoldRateSub: String,
    val insightTimeSavedTitle: String,
    val insightTimeSavedSub: String,

    // 7.2 中部多维构成环形图卡片 (Donut Chart)
    val insightSectionComposition: String,
    val insightTabOptions: List<String>,       // [按紧急度, 按来源平台, 按耗时分布]
    val insightDonutSummaryLeft: List<String>, // 对应 3 个维度的折叠态左侧摘要
    val insightDonutSummaryRight: List<String>,// 对应 3 个维度的折叠态右侧高亮结论
    val insightCenterTitleCount: String,       // 圆环中心默认标题：累计接收通知
    val insightCenterTitleTime: String,        // 圆环中心默认标题：累计处理耗时
    val insightUnitCount: String,              // 数量单位：条 / msgs / st
    val insightUnitMin: String,                // 时间单位：min
    // 扇区分类名称
    val sliceHighUrgency: String,
    val sliceMedUrgency: String,
    val sliceLowUrgency: String,
    val slicePlatformWeChat: String,
    val slicePlatformMail: String,
    val slicePlatformSocial: String,
    val slicePlatformSystem: String,
    val sliceTimeWeChat: String,
    val sliceTimeMail: String,
    val sliceTimeOther: String,

    // 7.3 底部每日耗时趋势柱状图卡片 (Daily Bar Chart)
    val insightSectionDailyTrend: String,
    val insightBarSummaryLeft: String,
    val insightBarSummaryRight: String,
    val insightBarAvgPrefix: String,
    val weekDaysShort: List<String>            // 周一至周日简写
)

// ============================================================================
// 🇨🇳 简体中文词典实例 (ZhStrings)
// ============================================================================
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
    toastDbCleared = "✅ 本地消息数据库已彻底清空",

    // --- 7. 统计与心智洞察页 ---
    insightSectionBaseMetrics = "核心减负指标 (BASE METRICS)",
    insightFoldRateTitle = "杂音折叠率",
    insightFoldRateSub = "已静默收纳 97 条低优通知",
    insightTimeSavedTitle = "节省专注时长",
    insightTimeSavedSub = "避免约 97 次注意力打断",

    insightSectionComposition = "多维消息构成洞察",
    insightTabOptions = listOf("按紧急度", "按来源平台", "按耗时分布"),
    insightDonutSummaryLeft = listOf(
        "按紧急度统计 (共 142 条)",
        "按来源平台统计 (共 4 类)",
        "按处理耗时统计 (共 92m)"
    ),
    insightDonutSummaryRight = listOf(
        "低优杂音占 68%",
        "微信占 44%",
        "微信耗时占 52%"
    ),
    insightCenterTitleCount = "累计接收通知",
    insightCenterTitleTime = "累计处理耗时",
    insightUnitCount = "条",
    insightUnitMin = "min",
    sliceHighUrgency = "高优核心",
    sliceMedUrgency = "普通日常",
    sliceLowUrgency = "低优杂音",
    slicePlatformWeChat = "微信聊天",
    slicePlatformMail = "办公邮件",
    slicePlatformSocial = "社交资讯",
    slicePlatformSystem = "系统与其他",
    sliceTimeWeChat = "微信耗时",
    sliceTimeMail = "邮件处理",
    sliceTimeOther = "其他平台",

    insightSectionDailyTrend = "每日耗时趋势 (近 7 天)",
    insightBarSummaryLeft = "累计处理耗时 239m",
    insightBarSummaryRight = "日均 34m · 今日 19m",
    insightBarAvgPrefix = "日均",
    weekDaysShort = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
)

// ============================================================================
// 🇬🇧 英文词典实例 (EnStrings)
// ============================================================================
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
    toastDbCleared = "✅ Local message database cleared",

    // --- 7. Insights / AnalyticsScreen ---
    insightSectionBaseMetrics = "BASE METRICS",
    insightFoldRateTitle = "Noise Folded",
    insightFoldRateSub = "97 low-priority msgs silenced",
    insightTimeSavedTitle = "Focus Saved",
    insightTimeSavedSub = "97 context switches avoided",

    insightSectionComposition = "MESSAGE COMPOSITION",
    insightTabOptions = listOf("By Urgency", "By Platform", "By Time"),
    insightDonutSummaryLeft = listOf(
        "By Urgency (142 msgs)",
        "By Platform (4 apps)",
        "By Time Spent (92m)"
    ),
    insightDonutSummaryRight = listOf(
        "Low noise 68%",
        "WeChat 44%",
        "WeChat 52%"
    ),
    insightCenterTitleCount = "Total Received",
    insightCenterTitleTime = "Total Time Spent",
    insightUnitCount = "msgs",
    insightUnitMin = "min",
    sliceHighUrgency = "High Priority",
    sliceMedUrgency = "Normal Daily",
    sliceLowUrgency = "Low Noise",
    slicePlatformWeChat = "WeChat",
    slicePlatformMail = "Work Email",
    slicePlatformSocial = "Social Media",
    slicePlatformSystem = "System & Other",
    sliceTimeWeChat = "WeChat",
    sliceTimeMail = "Email",
    sliceTimeOther = "Others",

    insightSectionDailyTrend = "DAILY TIME TREND (7 DAYS)",
    insightBarSummaryLeft = "Total time spent 239m",
    insightBarSummaryRight = "Avg 34m · Today 19m",
    insightBarAvgPrefix = "Avg",
    weekDaysShort = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
)

// ============================================================================
// 🇸🇪 瑞典语词典实例 (SvStrings)
// ============================================================================
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
    itemHaptic = "Haptisk respons (Vibration)",
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
    toastDbCleared = "✅ Den lokala databasen har rensats",

    // --- 7. Insikter / AnalyticsScreen ---
    insightSectionBaseMetrics = "BASMÅTT (BASE METRICS)",
    insightFoldRateTitle = "Brus filtrerat",
    insightFoldRateSub = "97 lågprio-notiser tystade",
    insightTimeSavedTitle = "Sparad fokustid",
    insightTimeSavedSub = "97 avbrott undvikna",

    insightSectionComposition = "MEDDELANDESAMMANSÄTTNING",
    insightTabOptions = listOf("Brådska", "Plattform", "Tidsåtgång"),
    insightDonutSummaryLeft = listOf(
        "Efter brådska (142 st)",
        "Efter plattform (4 appar)",
        "Efter tidsåtgång (92m)"
    ),
    insightDonutSummaryRight = listOf(
        "Lågprio 68%",
        "WeChat 44%",
        "WeChat 52%"
    ),
    insightCenterTitleCount = "Totalt mottagna",
    insightCenterTitleTime = "Total tidsåtgång",
    insightUnitCount = "st",
    insightUnitMin = "min",
    sliceHighUrgency = "Hög prioritet",
    sliceMedUrgency = "Normal vardag",
    sliceLowUrgency = "Lågprio brus",
    slicePlatformWeChat = "WeChat",
    slicePlatformMail = "Jobbmejl",
    slicePlatformSocial = "Sociala medier",
    slicePlatformSystem = "System & Övrigt",
    sliceTimeWeChat = "WeChat",
    sliceTimeMail = "E-post",
    sliceTimeOther = "Övriga",

    insightSectionDailyTrend = "DAGLIG TIDSTREND (7 DAGAR)",
    insightBarSummaryLeft = "Total hanteringstid 239m",
    insightBarSummaryRight = "Snitt 34m · Idag 19m",
    insightBarAvgPrefix = "Snitt",
    weekDaysShort = listOf("Mån", "Tis", "Ons", "Tor", "Fre", "Lör", "Sön")
)

/**
 * 全局 CompositionLocal 管道，默认提供简体中文词典
 */
val LocalAppStrings = staticCompositionLocalOf { ZhStrings }