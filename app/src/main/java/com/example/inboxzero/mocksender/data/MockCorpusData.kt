package com.example.inboxzero.mocksender.data

/**
 * 静态测试语料库 (Mock Database)：
 * 存放用于 HCI 实验的真实模拟数据。
 * 包含 5 个独立的故事线 (Thread) 以及穿插的随机噪音，共 30 条数据。
 */
val CORPUS_MESSAGES = listOf(
    // ==========================================
    // 剧本 1：导师催论文 (threadId = "paper_review") - 高优私聊
    // ==========================================
    MockMessage(
        appName = "微信", sender = "导师",
        content = "下周把那篇 HCI 论文的实验设计初稿发给我看一下。",
        threadId = "paper_review", step = 1
    ),
    MockMessage(
        appName = "微信", sender = "导师",
        content = "刚才发的 PDF 我批注了，摘要部分逻辑需要重写。",
        threadId = "paper_review", step = 2
    ),
    MockMessage(
        appName = "微信", sender = "导师",
        content = "第三节的数据图表不够清晰，重新跑一下实验数据。",
        threadId = "paper_review", step = 3
    ),
    MockMessage(
        appName = "微信", sender = "导师",
        content = "修改好了发到我邮箱，明天下午 3 点前我要看。",
        threadId = "paper_review", step = 4
    ),
    MockMessage(
        appName = "微信", sender = "导师",
        content = "收到，我看后给你答复。",
        threadId = "paper_review", step = 5
    ),

    // ==========================================
    // 剧本 2：项目组会通知 (threadId = "group_meeting") - 中优群聊
    // ==========================================
    MockMessage(
        appName = "微信", sender = "大师兄",
        content = "@全体成员 明天下午两点在 302 开组会，准备好汇报 PPT。",
        groupName = "HCI 实验室大群", threadId = "group_meeting", step = 1
    ),
    MockMessage(
        appName = "微信", sender = "张三",
        content = "收到，我的 PPT 已经做好了。",
        groupName = "HCI 实验室大群", threadId = "group_meeting", step = 2
    ),
    MockMessage(
        appName = "微信", sender = "李四",
        content = "大师兄，我明天有个面试，可能要晚点到。",
        groupName = "HCI 实验室大群", threadId = "group_meeting", step = 3
    ),
    MockMessage(
        appName = "微信", sender = "大师兄",
        content = "@李四 没问题，你尽量赶过来。",
        groupName = "HCI 实验室大群", threadId = "group_meeting", step = 4
    ),
    MockMessage(
        appName = "微信", sender = "王五",
        content = "我带个投影仪转接头过去吧。",
        groupName = "HCI 实验室大群", threadId = "group_meeting", step = 5
    ),
    MockMessage(
        appName = "微信", sender = "大师兄",
        content = "好的，麻烦了。大家记得准时。",
        groupName = "HCI 实验室大群", threadId = "group_meeting", step = 6
    ),

    // ==========================================
    // 剧本 3：外卖拼单 (threadId = "takeaway_order") - 低优群聊，跨 App
    // ==========================================
    MockMessage(
        appName = "微信", sender = "吃货小分队队长",
        content = "中午吃什么？麦当劳有人拼吗？差 20 起送。",
        groupName = "干饭魂", threadId = "takeaway_order", step = 1
    ),
    MockMessage(
        appName = "微信", sender = "赵六",
        content = "我要一个板烧鸡腿堡套餐。",
        groupName = "干饭魂", threadId = "takeaway_order", step = 2
    ),
    MockMessage(
        appName = "微信", sender = "钱七",
        content = "加我一个，我要麦辣鸡翅。",
        groupName = "干饭魂", threadId = "takeaway_order", step = 3
    ),
    MockMessage(
        appName = "微信", sender = "吃货小分队队长",
        content = "够了够了，我下单了啊，大概 40 分钟到。",
        groupName = "干饭魂", threadId = "takeaway_order", step = 4
    ),
    MockMessage(
        appName = "美团外卖", sender = "订单通知",
        content = "您的订单已由骑手接单，正在赶往商家。",
        threadId = "takeaway_order", step = 5
    ),
    MockMessage(
        appName = "美团外卖", sender = "送达提醒",
        content = "您的外卖已送达至宿舍楼下保温柜，请尽快取餐。",
        threadId = "takeaway_order", step = 6
    ),

    // ==========================================
    // 剧本 4：周末出游计划 (threadId = "weekend_trip") - 闲聊群
    // ==========================================
    MockMessage(
        appName = "微信", sender = "小红",
        content = "这周末天气不错，要去郊野公园烧烤吗？",
        groupName = "周末浪起来", threadId = "weekend_trip", step = 1
    ),
    MockMessage(
        appName = "微信", sender = "小明",
        content = "好啊！我负责带烧烤架和碳。",
        groupName = "周末浪起来", threadId = "weekend_trip", step = 2
    ),
    MockMessage(
        appName = "微信", sender = "小刚",
        content = "那我负责买肉和饮料吧，大家想喝什么？",
        groupName = "周末浪起来", threadId = "weekend_trip", step = 3
    ),
    MockMessage(
        appName = "微信", sender = "小红",
        content = "可乐！多买点鸡翅！",
        groupName = "周末浪起来", threadId = "weekend_trip", step = 4
    ),
    MockMessage(
        appName = "微信", sender = "小刚",
        content = "收到，周六早上 9 点校门口集合怎么样？",
        groupName = "周末浪起来", threadId = "weekend_trip", step = 5
    ),
    MockMessage(
        appName = "微信", sender = "小明",
        content = "没问题，不见不散。",
        groupName = "周末浪起来", threadId = "weekend_trip", step = 6
    ),

    // ==========================================
    // 剧本 5：面试流程通知 (threadId = "job_interview") - 跨 App 核心流程
    // ==========================================
    MockMessage(
        appName = "牛客网", sender = "招聘助手",
        content = "您的简历已通过初筛，请于明晚 8 点参加在线笔试。",
        threadId = "job_interview", step = 1
    ),
    MockMessage(
        appName = "牛客网", sender = "考试提醒",
        content = "在线笔试即将于 30 分钟后开始，请提前调试好摄像头。",
        threadId = "job_interview", step = 2
    ),
    MockMessage(
        appName = "邮件", sender = "HR 专员",
        content = "【面试邀请】恭喜您通过笔试，请预约一面时间。",
        threadId = "job_interview", step = 3
    ),
    MockMessage(
        appName = "日历", sender = "日程提醒",
        content = "明天下午 2:00 有一场视频面试，请做好准备。",
        threadId = "job_interview", step = 4
    ),

    // ==========================================
    // 随机无主线噪音 (无 threadId 和 step，可随时乱序插入)
    // ==========================================
    MockMessage(
        appName = "京东", sender = "促销小助手",
        content = "【限时秒杀】您的购物车商品降价 20 元，点击立即抢购！"
    ),
    MockMessage(
        appName = "淘宝", sender = "物流助手",
        content = "您的包裹已到达本市转运中心，预计明天送达。"
    ),
    MockMessage(
        appName = "抖音", sender = "视频推荐",
        content = "你关注的 @罗翔说刑法 刚刚更新了视频，快去看看吧！"
    )
)