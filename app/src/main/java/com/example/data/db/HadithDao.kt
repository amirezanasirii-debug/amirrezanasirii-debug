package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BookEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.HadithEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HadithDao {

    @Query("SELECT * FROM books ORDER BY orderIndex ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE collection = :collection ORDER BY orderIndex ASC")
    fun getBooksByCollection(collection: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    fun getBookById(bookId: String): Flow<BookEntity?>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY orderIndex ASC")
    fun getChaptersByBook(bookId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :chapterId LIMIT 1")
    fun getChapterById(chapterId: String): Flow<ChapterEntity?>

    @Query("SELECT * FROM hadiths WHERE chapterId = :chapterId ORDER BY numberInBook ASC")
    fun getHadithsByChapter(chapterId: String): Flow<List<HadithEntity>>

    @Query("SELECT * FROM hadiths WHERE bookId = :bookId ORDER BY numberInBook ASC")
    fun getHadithsByBook(bookId: String): Flow<List<HadithEntity>>

    @Query("SELECT * FROM hadiths WHERE id = :id LIMIT 1")
    fun getHadithById(id: Long): Flow<HadithEntity?>

    @Query("SELECT * FROM hadiths WHERE id IN (:ids)")
    fun getHadithsByIds(ids: List<Long>): Flow<List<HadithEntity>>

    @Query("SELECT * FROM hadiths ORDER BY id ASC LIMIT 1")
    fun getFeaturedHadith(): Flow<HadithEntity?>

    @Query("""
        SELECT * FROM hadiths 
        WHERE (:collectionFilter IS NULL OR collection = :collectionFilter)
          AND (searchableText LIKE '%' || :query || '%' 
               OR textPersian LIKE '%' || :query || '%' 
               OR isnadChain LIKE '%' || :query || '%')
        ORDER BY numberInBook ASC
        LIMIT 100
    """)
    fun searchHadiths(query: String, collectionFilter: String?): Flow<List<HadithEntity>>

    @Query("SELECT * FROM hadiths WHERE collection = :collection LIMIT 50")
    fun getSampleHadithsByCollection(collection: String): Flow<List<HadithEntity>>

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBookCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHadiths(hadiths: List<HadithEntity>)
}
