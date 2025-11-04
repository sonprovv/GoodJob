package com.project.job.data.source.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.project.job.data.source.local.room.converter.ListStringConverter
import com.project.job.data.source.local.room.entity.ChatEntity
import com.project.job.data.source.local.room.entity.JobEntity

@Database(entities = [JobEntity::class, ChatEntity::class], version = 2, exportSchema = false)
@TypeConverters(ListStringConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun getJobDAO(): JobDAO
    abstract fun getChatDAO(): ChatDAO
    companion object {
        // @Volatile đảm bảo rằng thay đổi của biến sẽ được thấy ngay ở các thread khác
        @Volatile
        private var instance : AppDatabase ?= null
        // Object khóa để đảm bảo chỉ tạo 1 instance duy nhất
        private var LOCK = Any()
        // Hàm invoke giúp gọi trực tiếp bằng NoteDatabase(context), trả về instance nếu đã có
        operator fun invoke(context : Context) = instance ?:
        synchronized(LOCK) {
            instance ?: createDatabase(context).also {
                instance = it
            }
        }
        // Hàm tạo database
        private fun createDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, AppDatabase::class.java,
            "m_db"
        )
            .fallbackToDestructiveMigration()
            .build()
        private fun getDatabaseName(): String {
            return "m_db"
        }
    }
}