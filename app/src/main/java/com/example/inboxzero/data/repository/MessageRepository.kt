package com.example.inboxzero.data.repository

import com.example.inboxzero.data.local.messages.MessageDao
import com.example.inboxzero.data.local.messages.MessageEntity
import com.example.inboxzero.data.local.messages.SenderInfo
import com.example.inboxzero.data.model.InboxMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 消息数据仓库：作为单一事实来源（Single Source of Truth）。
 * 负责业务模型 (InboxMessage) 与底层数据模型 (MessageEntity) 的双向映射及数据操作。
 */
class MessageRepository(private val messageDao: MessageDao) {

    // ==========================================
    // 1. 写入操作 (将 InboxMessage 转换为 Entity 入库)
    // ==========================================

    /**
     * 接收 Scheduler 传递的标准包裹，转换为底层 Entity 后存入数据库。
     */
    suspend fun insertMessage(inboxMessage: InboxMessage) {
        val entity = mapToEntity(inboxMessage)
        messageDao.insertMessage(entity)
    }

    // ==========================================
    // 2. 读取操作 (将 Entity 转换为 InboxMessage 供 UI 使用)
    // ==========================================

    /**
     * 获取所有活跃消息（未归档），并将其映射回通用的 InboxMessage。
     */
    fun getAllActiveMessages(): Flow<List<InboxMessage>> {
        return messageDao.getAllActiveMessages().map { entityList ->
            entityList.map { entity -> mapToModel(entity) }
        }
    }

    /**
     * 根据包名、发件人和消息类型获取特定会话，同样映射回 InboxMessage。
     */
    fun getChatThread(packageName: String, senderName: String, messageType: Int): Flow<List<InboxMessage>> {
        return messageDao.getChatThread(packageName, senderName, messageType).map { entityList ->
            entityList.map { entity -> mapToModel(entity) }
        }
    }

    /**
     * 获取全局未读消息数量。由于返回的是基础类型 Int，无需映射。
     */
    fun getGlobalUnreadCount(): Flow<Int> {
        return messageDao.getGlobalUnreadCount()
    }

    // ==========================================
    // 3. 状态快操
    // ==========================================

    suspend fun markThreadAsRead(packageName: String, senderName: String, messageType: Int) {
        messageDao.markThreadAsRead(packageName, senderName, messageType)
    }

    suspend fun clearAllMessages() {
        messageDao.clearAllMessages()
    }

    // ==========================================
    // 4. 数据映射器 (Mappers) - 核心翻译逻辑
    // ==========================================

    /**
     * 将业务层流通的 InboxMessage 映射为存入数据库的 MessageEntity。
     * 注意：对于 AiMetadata 和 InteractionState，我们依赖 Entity 中预设的默认值。
     */
    private fun mapToEntity(model: InboxMessage): MessageEntity {
        return MessageEntity(
            systemKey = model.systemKey,
            packageName = model.packageName,
            postTime = model.postTime,
            title = model.title,
            content = model.content,
            attachmentType = model.attachmentType,
            rawDataLog = model.rawDataLog,
            senderInfo = SenderInfo(
                messageType = model.messageType,
                isMentioned = model.isMentioned,
                groupName = model.groupName,
                senderName = model.senderName,
                senderId = model.senderId
            )
            // aiMetadata 和 interactionState 未显式传入，触发默认构造
        )
    }

    /**
     * 将从数据库读取的 MessageEntity 映射回业务层使用的 InboxMessage。
     */
    private fun mapToModel(entity: MessageEntity): InboxMessage {
        return InboxMessage(
            systemKey = entity.systemKey,
            packageName = entity.packageName,
            postTime = entity.postTime,
            title = entity.title,
            content = entity.content,
            attachmentType = entity.attachmentType,
            rawDataLog = entity.rawDataLog,
            messageType = entity.senderInfo.messageType,
            isMentioned = entity.senderInfo.isMentioned,
            groupName = entity.senderInfo.groupName,
            senderName = entity.senderInfo.senderName,
            senderId = entity.senderInfo.senderId
        )
    }
}