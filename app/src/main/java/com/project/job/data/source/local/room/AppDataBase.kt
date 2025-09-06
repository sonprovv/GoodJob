package com.project.job.data.source.local.room

//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import com.client.moviezz.db.dao.WatchHistoryDao
//
//@Database(entities = [HistoryMovie::class], version = 1)
//abstract class AppDatabase: RoomDatabase() {
//    abstract fun getWatchHistoryDao(): WatchHistoryDao
//
//    companion object {
//        // @Volatile đảm bảo rằng thay đổi của biến sẽ được thấy ngay ở các thread khác
//        @Volatile
//        private var instance : AppDatabase ?= null
//        // Object khóa để đảm bảo chỉ tạo 1 instance duy nhất
//        private var LOCK = Any()
//        // Hàm invoke giúp gọi trực tiếp bằng NoteDatabase(context), trả về instance nếu đã có
//        operator fun invoke(context : Context) = instance ?:
//        synchronized(LOCK) {
//            instance ?: createDatabase(context).also {
//                instance = it
//            }
//        }
//        // Hàm tạo database
//        private fun createDatabase(context: Context) = Room.databaseBuilder(
//            context.applicationContext, AppDatabase::class.java,
//            "mo_db"
//        )
//            .fallbackToDestructiveMigration()
//            .build()
//        private fun getDatabaseName(): String {
//            return "mo_db"
//        }
//    }
//}