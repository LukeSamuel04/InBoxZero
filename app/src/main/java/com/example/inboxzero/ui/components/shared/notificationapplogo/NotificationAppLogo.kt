package com.example.inboxzero.ui.components.shared.notificationapplogo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Locale

/**
 * 全站公用应用几何剪影图标 (Notification App Silhouette Logo)
 *
 * 复用场景：
 * 1. InboxSessionCard: 嵌于卡片左侧拓宽色带中央，用图形直觉代替“[微信] / [钉钉]”文字阅读。
 * 2. AnalyticsScreen: 统计页平台排行与图例展示。
 */
enum class AppLogoType {
    CHAT_BUBBLE,   // 微信 / QQ / WhatsApp：双气泡剪影
    WORK_WING,     // 钉钉 / 飞书 / Slack：闪电飞翼剪影
    ACADEMIC_CAP,  // Canvas / 教务邮件：书院学士帽剪影
    SOCIAL_SPARK,  // 小红书 / Instagram / TikTok：四角星芒剪影
    SERVICE_BAG    // 京东 / 淘宝 / Steam / 系统服务：手提购物袋剪影
}

@Composable
fun NotificationAppLogo(
    packageName: String,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 22.dp
) {
    val logoType = remember(packageName) { resolveAppLogoType(packageName) }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = w * 0.095f

        when (logoType) {
            // 1. 聊天通讯类 (双交叠圆角气泡 + 呼吸点)
            AppLogoType.CHAT_BUBBLE -> {
                // 后方大聊天气泡 (实心半透明)
                drawRoundRect(
                    color = tint.copy(alpha = 0.88f),
                    topLeft = Offset(w * 0.08f, h * 0.14f),
                    size = Size(w * 0.62f, h * 0.50f),
                    cornerRadius = CornerRadius(w * 0.22f, w * 0.22f)
                )
                // 前方右下小气泡 (实心高亮)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.36f, h * 0.40f),
                    size = Size(w * 0.56f, h * 0.46f),
                    cornerRadius = CornerRadius(w * 0.20f, w * 0.20f)
                )
            }

            // 2. 办公协同类 (利落纸飞机 / 钉钉飞翼剪影)
            AppLogoType.WORK_WING -> {
                val wingPath = Path().apply {
                    moveTo(w * 0.12f, h * 0.46f)
                    lineTo(w * 0.88f, h * 0.16f)
                    lineTo(w * 0.62f, h * 0.86f)
                    lineTo(w * 0.44f, h * 0.56f)
                    close()
                }
                drawPath(
                    path = wingPath,
                    color = tint.copy(alpha = 0.92f)
                )
            }

            // 3. 学术与邮件类 (极简菱形书院帽剪影)
            AppLogoType.ACADEMIC_CAP -> {
                val capTop = Path().apply {
                    moveTo(w * 0.50f, h * 0.16f)
                    lineTo(w * 0.90f, h * 0.38f)
                    lineTo(w * 0.50f, h * 0.60f)
                    lineTo(w * 0.10f, h * 0.38f)
                    close()
                }
                drawPath(path = capTop, color = tint)

                val capBase = Path().apply {
                    moveTo(w * 0.26f, h * 0.52f)
                    lineTo(w * 0.26f, h * 0.74f)
                    quadraticTo(w * 0.50f, h * 0.88f, w * 0.74f, h * 0.74f)
                    lineTo(w * 0.74f, h * 0.52f)
                }
                drawPath(
                    path = capBase,
                    color = tint.copy(alpha = 0.85f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // 4. 社交内容类 (四角星芒 Sparkle 剪影)
            AppLogoType.SOCIAL_SPARK -> {
                val sparkPath = Path().apply {
                    moveTo(w * 0.50f, h * 0.10f)
                    quadraticTo(w * 0.54f, h * 0.46f, w * 0.90f, h * 0.50f)
                    quadraticTo(w * 0.54f, h * 0.54f, w * 0.50f, h * 0.90f)
                    quadraticTo(w * 0.46f, h * 0.54f, w * 0.10f, h * 0.50f)
                    quadraticTo(w * 0.46f, h * 0.46f, w * 0.50f, h * 0.10f)
                    close()
                }
                drawPath(path = sparkPath, color = tint)
            }

            // 5. 生活服务与电商类 (圆角购物袋剪影)
            AppLogoType.SERVICE_BAG -> {
                // 袋身
                drawRoundRect(
                    color = tint.copy(alpha = 0.90f),
                    topLeft = Offset(w * 0.18f, h * 0.34f),
                    size = Size(w * 0.64f, h * 0.54f),
                    cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
                )
                // 提手弧线
                val handlePath = Path().apply {
                    moveTo(w * 0.35f, h * 0.34f)
                    quadraticTo(w * 0.50f, h * 0.08f, w * 0.65f, h * 0.34f)
                }
                drawPath(
                    path = handlePath,
                    color = tint,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
    }
}

private fun resolveAppLogoType(packageName: String): AppLogoType {
    val pkg = packageName.lowercase(Locale.ROOT)
    return when {
        pkg.contains("mm") || pkg.contains("wechat") || pkg.contains("qq") ||
                pkg.contains("whatsapp") || pkg.contains("telegram") -> AppLogoType.CHAT_BUBBLE

        pkg.contains("canvas") || pkg.contains("outlook") || pkg.contains("mail") ||
                pkg.contains("edu") -> AppLogoType.ACADEMIC_CAP

        pkg.contains("ding") || pkg.contains("linkedin") || pkg.contains("slack") ||
                pkg.contains("teams") || pkg.contains("feishu") -> AppLogoType.WORK_WING

        pkg.contains("xhs") || pkg.contains("instagram") || pkg.contains("tiktok") ||
                pkg.contains("weibo") || pkg.contains("bili") -> AppLogoType.SOCIAL_SPARK

        else -> AppLogoType.SERVICE_BAG
    }
}