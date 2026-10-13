package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BookmarkEntity
import com.example.data.model.ComparisonItemEntity
import com.example.data.model.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserContentDao {

    // نشان‌شده‌ها
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE hadithId = :hadithId)")
    fun isBookmarked(hadithId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE hadithId = :hadithId")
    suspend fun deleteBookmark(hadithId: Long)

    // یادداشت‌ها
    @Query("SELECT * FROM notes WHERE hadithId = :hadithId ORDER BY timestamp DESC")
    fun getNotesForHadith(hadithId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: Long)

    // میزکار مقایسه روایات
    @Query("SELECT * FROM comparisons ORDER BY timestamp DESC")
    fun getAllComparisonItems(): Flow<List<ComparisonItemEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM comparisons WHERE hadithId = :hadithId)")
    fun isInComparison(hadithId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToComparison(item: ComparisonItemEntity)

    @Query("DELETE FROM comparisons WHERE hadithId = :hadithId")
    suspend fun removeFromComparison(hadithId: Long)

    @Query("DELETE FROM comparisons")
    suspend fun clearComparison()
}
