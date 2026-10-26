package com.example.inboxzero.data.local.messages

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    // ==========================================
    // 1. 基础写入与修改 (供后台 Worker 或拦截器调用)
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()

    // ==========================================
    // 2. 核心业务数据流 (返回 Flow，供 UI 层持续监听)
    // ==========================================

    @Query("SELECT * FROM messages WHERE isArchived = 0 ORDER BY postTime DESC")
    fun getAllActiveMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE packageName = :packageName AND senderName = :senderName AND isGroup = :isGroup ORDER BY postTime ASC")
    fun getChatThread(packageName: String, senderName: String, isGroup: Boolean): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE isRead = 0 AND isArchived = 0")
    fun getGlobalUnreadCount(): Flow<Int>

    // ==========================================
    // 3. 针对性状态快操
    // ==========================================

    @Query("UPDATE messages SET isRead = 1 WHERE packageName = :packageName AND senderName = :senderName AND isGroup = :isGroup")
    suspend fun markThreadAsRead(packageName: String, senderName: String, isGroup: Boolean)

    // ==========================================
    // 4. AI 调度专属接口 (新增)
    // ==========================================

    /**
     * 获取所有等待 AI 处理的消息 (状态为 0)
     * 注意：这里返回 List 而不是 Flow，因为后台 Worker 是一次性拉取任务，不需要像 UI 那样持续监听。
     */
    @Query("SELECT * FROM messages WHERE aiProcessStatus = 0")
    suspend fun getPendingAiMessages(): List<MessageEntity>

    /**
     * AI 分析完成后的“回填接口”。
     * 由于我们用了 @Embedded，Room 会自动把 AiMetadata 的属性名映射为列名，这里直接更新对应的列即可。
     */
    @Query("UPDATE messages SET aiProcessStatus = :status, aiSummary = :summary, urgencyScore = :score WHERE id = :id")
    suspend fun updateAiResult(id: Long, status: Int, summary: String?, score: Int?)
}