package com.example.mobile_dev_project.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mobile_dev_project.data.db.dao.*
import com.example.mobile_dev_project.data.models.*

/**
 * defines the central Room database for the app
 *
 * - It ties together all of our @Entity data classes (tables)
 *   and @Dao interfaces (data access objects).
 * - Room automatically generates the code needed to create
 *   and manage these tables at runtime.
 *
 * Following the Singleton pattern, we only ever create ONE(!) instance
 * of this database (shared across the app) to prevent memory leaks
 * or multiple open connections.
 * Implemented according to Week 12 slides / Week 12 code example for RoomDB
 */
@Database(
    entities = [BookEntity::class, ChapterEntity::class, ReadingProgressEntity::class],
    version = 1,
    exportSchema = true
)

abstract class AppDatabase : RoomDatabase() {
    // --- DAO references ---
    //Each gives access to a specific table or related ones
    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun readingProgressDao(): ReadingProgressDao

    //holds the singleton instance of the db
    companion object {
        //any thread reading this var -> gets most up-to-date val
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reader_db"
                ).build().also { INSTANCE = it }
            }
    }
}