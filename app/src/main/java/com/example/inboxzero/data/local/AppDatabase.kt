package com.example.inboxzero.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.inboxzero.data.local.messages.MessageDao
import com.example.inboxzero.data.local.messages.MessageEntity
// 提前为你未来的 Rule 模块预留的 import
// import com.example.inboxzero.data.local.rules.InterceptRuleEntity
// import com.example.inboxzero.data.local.rules.RuleDao

/**
 * 整个底层数据库的“最高执行官”。
 * 负责圈地建库、划分表结构、并对外派发 DAO 遥控器。
 */
@Database(
    entities = [
        MessageEntity::class
        // InterceptRuleEntity::class // 等你的 RuleEntity 写好后，在这里加上逗号并取消注释
    ],
    version = 1, // 数据库版本号，每次修改 Entity 字段后都需要 +1
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // 1. 暴露 DAO：这是 AppDatabase 吐出“遥控器”的唯一途径
    abstract fun messageDao(): MessageDao

    // abstract fun ruleDao(): RuleDao // 预留给规则模块的 DAO

    // 2. 单例模式 (Singleton)：确保整个 App 只有一个数据库连接实例
    companion object {
        // @Volatile 确保 INSTANCE 的修改对所有线程立即可见
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // 如果 INSTANCE 不为空就直接返回，如果为空就进入 synchronized 同步锁创建
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, // 必须使用 applicationContext，防止内存泄漏
                    AppDatabase::class.java,
                    "inboxzero_db" // 这是最终生成在手机物理硬盘上的文件名
                )
                    // 【开发期神器】：当版本号升级但没写迁移规则时，直接清空旧表重建，防止崩溃。
                    // 仅限开发阶段使用，App 上线前千万记得删掉或替换为正规 Migration！
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}