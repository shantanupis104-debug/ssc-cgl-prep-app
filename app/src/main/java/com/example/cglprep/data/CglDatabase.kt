package com.example.cglprep.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        StudentProfileEntity::class,
        ErrorDiaryEntity::class,
        ExamAttemptEntity::class,
        BookmarkEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CglDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun errorDiaryDao(): ErrorDiaryDao
    abstract fun examAttemptDao(): ExamAttemptDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: CglDatabase? = null

        fun getDatabase(context: Context): CglDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CglDatabase::class.java,
                    "cgl_prep_pro.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
