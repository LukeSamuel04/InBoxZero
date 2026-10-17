package com.example.inboxzero.mocksender.data

/**
 * 1. 数据结构定义 (相当于 Python 的 @dataclass 或 NamedTuple)
 * 用来规整每一条假消息的格式：发件人/群名 + 消息正文
 */
data class MockMessage(
    val sender: String,
    val content: String
)

/**
 * 2. 静态测试语料库 (顶层常量列表，无需包裹在 class 里)
 * 涵盖 HCI 实验中常见的三大类通知场景：高优私聊、多人群聊、低优噪音
 */
val CORPUS_MESSAGES = listOf(
    // 【高优场景】：导师与学术核心通知
    MockMessage("导师", "下周把那篇 HCI 论文的实验设计初稿发给我看一下。"),
    MockMessage("导师", "刚才发的 PDF 我批注了，摘要部分逻辑需要重写。"),
    MockMessage("实验室大师兄", "服务器显卡空出来了，你的模型可以挂上去跑了。"),
    MockMessage("教务处王老师", "请于本周五下午 5 点前提交开题报告纸质版签字。"),

    // 【中优/带格式场景】：包含“姓名: 内容”特征的群聊，用于测试后续的正则清洗管道
    MockMessage("HCI 毕业设计讨论群", "张三: 收到，马上修改"),
    MockMessage("HCI 毕业设计讨论群", "李四: @全体成员 明天下午两点在 302 开组会，别迟到"),
    MockMessage("HCI 毕业设计讨论群", "王五: 问卷星的受试者招募链接已经发群里了，大家帮忙转发一下"),
    MockMessage("HCI 毕业设计讨论群", "赵六: +1"),

    // 【低优/噪音场景】：闲聊与营销推销，用于测试 AI 过滤与防打扰降权
    MockMessage("夸夸闲聊水群", "哈哈哈哈笑死我了，你们看这个视频"),
    MockMessage("夸夸闲聊水群", "[动画表情]"),
    MockMessage("校园外卖拼单群", "麦当劳差 5 块钱起送，有一起拼的吗？"),
    MockMessage("京东促销提醒", "【限时秒杀】您的购物车商品降价 20 元，点击立即抢购！")
)

/**
 * 3. 纯函数：从语料库中随机抽取一条假消息
 */
fun getRandomCorpusMessage(): MockMessage {
    return CORPUS_MESSAGES.random()
}