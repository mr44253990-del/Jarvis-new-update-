package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ActionLogDao
import com.example.data.local.dao.ChatMessageDao
import com.example.data.local.dao.MemoryDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.ActionLogEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TaskEntity::class,
        MemoryEntity::class,
        ChatMessageEntity::class,
        ActionLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ArcherDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun memoryDao(): MemoryDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun actionLogDao(): ActionLogDao

    companion object {
        @Volatile
        private var INSTANCE: ArcherDatabase? = null

        fun getInstance(context: Context): ArcherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArcherDatabase::class.java,
                    "archer_ai.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial task as seen in the user screenshot!
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            database.taskDao().insertTask(
                                TaskEntity(
                                    title = "upload video in YouTube",
                                    isCompleted = false
                                )
                            )
                            database.memoryDao().insertMemory(
                                MemoryEntity(
                                    factKey = "User Role",
                                    factValue = "Content Creator & Commander"
                                )
                            )
                            database.actionLogDao().insertLog(
                                ActionLogEntity(
                                    actionName = "System Boot",
                                    details = "Archer AI Core initialized and online."
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
