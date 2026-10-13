package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BookEntity
import com.example.data.model.BookmarkEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.ComparisonItemEntity
import com.example.data.model.HadithEntity
import com.example.data.model.HadithNarratorCrossRef
import com.example.data.model.NarratorEntity
import com.example.data.model.NoteEntity
import com.example.data.seed.InitialHadithData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        HadithEntity::class,
        NarratorEntity::class,
        HadithNarratorCrossRef::class,
        BookmarkEntity::class,
        NoteEntity::class,
        ComparisonItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun hadithDao(): HadithDao
    abstract fun narratorDao(): NarratorDao
    abstract fun userContentDao(): UserContentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mansour_hadith_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // بارگذاری اولیه داده‌های کتاب‌ها، ابواب، احادیث و راویان
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    populateInitialData(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance

                // اطمینان از مقداردهی اولیه حتی در صورت ایجاد قبلی
                CoroutineScope(Dispatchers.IO).launch {
                    val count = instance.hadithDao().getBookCount()
                    if (count == 0) {
                        populateInitialData(instance)
                    }
                }

                instance
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val hadithDao = database.hadithDao()
            val narratorDao = database.narratorDao()

            hadithDao.insertBooks(InitialHadithData.getInitialBooks())
            hadithDao.insertChapters(InitialHadithData.getInitialChapters())
            hadithDao.insertHadiths(InitialHadithData.getInitialHadiths())

            narratorDao.insertNarrators(InitialHadithData.getInitialNarrators())
            narratorDao.insertCrossRefs(InitialHadithData.getInitialCrossRefs())
        }
    }
}
