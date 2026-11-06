// 负责定义低饱和度的莫兰迪色卡与数据可视化专属色板
package com.example.inboxzero.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * InboxZero 极简防焦虑色板 (Morandi Palette)
 * 核心设计理念：
 * 1. 低饱和度、低视觉刺激、降低长期阅读与图表浏览时的认知负荷 (Cognitive Load)。
 * 2. 语义明确但情绪克制：用温和的灰粉、鼠尾草绿代替刺眼的纯红纯绿。
 * 3. 多维图表和谐共生：扩展色板保持统一的明度与灰阶质感，任意组合绘制饼图/柱状图均不产生视觉冲突。
 */

// ==========================================
// 1. 主色调 (Primary) - 柔和莫兰迪色系
// ==========================================
// 莫兰迪蓝：用于 PrimaryButton、核心确认操作、图表主数据序列。传递宁静、理智的暗示。
val MorandiBlue = Color(0xFF86A3B8)
val MorandiBlueDark = Color(0xFF6B8A9E)

// ==========================================
// 2. 状态色 (State / Secondary) - 避免高亮大红大绿
// ==========================================
// 莫兰迪红（灰粉色）：用于“停止”状态、高优紧急警示或危险操作。温和的制止感，不引发心跳加速。
val MorandiRed = Color(0xFFD4A3A3)
// 莫兰迪绿（鼠尾草绿）：用于“运行中”、成功状态、或心智减负正向收益指标。
val MorandiGreen = Color(0xFFA8C2A7)

// ==========================================
// 3. 中性背景色 (Background & Surface) - 杜绝纯黑纯白的高对比眩光
// ==========================================
// 浅色模式 (Light Theme)
val BackgroundLight = Color(0xFFF7F8FA) // 极浅灰白，给界面提供呼吸感
val SurfaceLight = Color(0xFFFFFFFF)    // 纯白，用于 PrimaryContainer 的底色，与背景产生极弱层级

// 深色模式 (Dark Theme)
val BackgroundDark = Color(0xFF1E1E20)  // 炭灰，绝不使用纯黑 (#000000)
val SurfaceDark = Color(0xFF28282B)     // 略亮的深灰，用于深色模式下的卡片底色

// ==========================================
// 4. 文字排版色 (Typography) - 控制对比度
// ==========================================
// 浅色模式下的文字
val TextPrimaryLight = Color(0xFF2C3E50)   // 深灰蓝，代替纯黑（减轻视觉疲劳）
val TextSecondaryLight = Color(0xFF8898AA) // 中灰，用于次要提示信息与图表坐标轴标签

// 深色模式下的文字
val TextPrimaryDark = Color(0xFFE2E8F0)    // 柔和灰白，代替纯白（防止黑暗环境下的散光光晕）
val TextSecondaryDark = Color(0xFFA0AEC0)  // 暗灰，深色模式次要文字与图表坐标轴标签

// ==========================================
// 5. 数据可视化扩展色 (Chart & Analytics Accents)
// ==========================================
// 专用于环形饼图 (Donut Chart) 与多维平台分布统计，保持与主色一致的灰调柔和感：
val MorandiAmber = Color(0xFFD9C5A0)  // 莫兰迪暖沙黄：用于中等优先级 (Medium) 或邮件/办公类平台占比
val MorandiPurple = Color(0xFFB5A8C2) // 莫兰迪丁香紫：用于社交娱乐类平台或次要分类占比
val MorandiTeal = Color(0xFF94B4B4)   // 莫兰迪雾霾青：用于系统通知、工具类平台或耗时对比序列
val MorandiSlate = Color(0xFFB8C2CC)  // 莫兰迪暖岩灰：用于图表中的“其他 (Others)”汇总扇区或未选中态柱体

/**
 * 预设的多维图表标准取色队列 (按视觉辨识度与柔和度最优排序)
 * 在绘制多平台占比环形图时，可直接通过 MorandiChartPalette[index % size] 循环取色
 */
val MorandiChartPalette = listOf(
    MorandiBlue,   // 1. 莫兰迪蓝
    MorandiGreen,  // 2. 鼠尾草绿
    MorandiAmber,  // 3. 暖沙黄
    MorandiRed,    // 4. 灰粉红
    MorandiPurple, // 5. 丁香紫
    MorandiTeal,   // 6. 雾霾青
    MorandiSlate   // 7. 暖岩灰 (兜底)
)