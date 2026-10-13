package com.example.data.repository

import com.example.data.db.HadithDao
import com.example.data.db.UserContentDao
import com.example.data.model.BookEntity
import com.example.data.model.BookmarkEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.ComparisonItemEntity
import com.example.data.model.HadithEntity
import com.example.data.model.NoteEntity
import com.example.data.util.HadithTextNormalizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

import com.example.data.db.NarratorDao
import com.example.data.model.HadithWithNarrators
import com.example.data.model.NarratorEntity

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HadithRepository(
    private val hadithDao: HadithDao,
    private val userContentDao: UserContentDao,
    private val narratorDao: NarratorDao
) {

    fun getNarratorsForHadith(hadithId: Long): Flow<List<NarratorEntity>> =
        narratorDao.getNarratorsForHadith(hadithId)

    fun getHadithWithNarrators(hadithId: Long): Flow<HadithWithNarrators?> =
        narratorDao.getHadithWithNarrators(hadithId)


    val allBooks: Flow<List<BookEntity>> = hadithDao.getAllBooks()
    val shiaBooks: Flow<List<BookEntity>> = hadithDao.getBooksByCollection("SHIA")
    val sunniBooks: Flow<List<BookEntity>> = hadithDao.getBooksByCollection("SUNNI")
    val featuredHadith: Flow<HadithEntity?> = hadithDao.getFeaturedHadith()

    fun getBook(bookId: String): Flow<BookEntity?> = hadithDao.getBookById(bookId)

    fun getChapters(bookId: String): Flow<List<ChapterEntity>> = hadithDao.getChaptersByBook(bookId)

    fun getChapter(chapterId: String): Flow<ChapterEntity?> = hadithDao.getChapterById(chapterId)

    fun getHadithsByChapter(chapterId: String): Flow<List<HadithEntity>> =
        hadithDao.getHadithsByChapter(chapterId)

    fun getHadithsByBook(bookId: String): Flow<List<HadithEntity>> =
        hadithDao.getHadithsByBook(bookId)

    fun getHadith(id: Long): Flow<HadithEntity?> = hadithDao.getHadithById(id)

    fun searchHadiths(rawQuery: String, collectionFilter: String?): Flow<List<HadithEntity>> {
        val normalized = HadithTextNormalizer.normalize(rawQuery)
        return hadithDao.searchHadiths(normalized, collectionFilter)
    }

    // نشان‌شده‌ها (Bookmarks)
    val bookmarks: Flow<List<BookmarkEntity>> = userContentDao.getAllBookmarks()

    fun isBookmarked(hadithId: Long): Flow<Boolean> = userContentDao.isBookmarked(hadithId)

    suspend fun toggleBookmark(hadithId: Long) {
        val currentlyBookmarked = userContentDao.isBookmarked(hadithId).first()
        if (currentlyBookmarked) {
            userContentDao.deleteBookmark(hadithId)
        } else {
            userContentDao.insertBookmark(BookmarkEntity(hadithId = hadithId))
        }
    }

    // یادداشت‌ها (Personal Notes)
    fun getNotesForHadith(hadithId: Long): Flow<List<NoteEntity>> =
        userContentDao.getNotesForHadith(hadithId)

    val allNotes: Flow<List<NoteEntity>> = userContentDao.getAllNotes()

    suspend fun addNote(hadithId: Long, content: String): Long {
        return userContentDao.insertNote(NoteEntity(hadithId = hadithId, content = content))
    }

    suspend fun deleteNote(noteId: Long) {
        userContentDao.deleteNote(noteId)
    }

    // میزکار مقایسه (Comparison Workspace)
    val comparisonItems: Flow<List<ComparisonItemEntity>> = userContentDao.getAllComparisonItems()

    fun isInComparison(hadithId: Long): Flow<Boolean> = userContentDao.isInComparison(hadithId)

    suspend fun toggleComparison(hadithId: Long) {
        val inComp = userContentDao.isInComparison(hadithId).first()
        if (inComp) {
            userContentDao.removeFromComparison(hadithId)
        } else {
            userContentDao.addToComparison(ComparisonItemEntity(hadithId = hadithId))
        }
    }

    suspend fun clearComparison() {
        userContentDao.clearComparison()
    }

    fun getComparisonHadiths(): Flow<List<HadithEntity>> {
        return userContentDao.getAllComparisonItems().flatMapLatest { items ->
            if (items.isEmpty()) {
                flowOf(emptyList())
            } else {
                hadithDao.getHadithsByIds(items.map { it.hadithId })
            }
        }
    }

    fun getBookmarkedHadiths(): Flow<List<HadithEntity>> {
        return userContentDao.getAllBookmarks().flatMapLatest { bookmarks ->
            if (bookmarks.isEmpty()) {
                flowOf(emptyList())
            } else {
                hadithDao.getHadithsByIds(bookmarks.map { it.hadithId })
            }
        }
    }
}
