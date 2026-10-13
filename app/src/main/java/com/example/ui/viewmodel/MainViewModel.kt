package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.HadithAssistantService
import com.example.data.db.AppDatabase
import com.example.data.model.BookEntity
import com.example.data.model.BookmarkEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.HadithEntity
import com.example.data.model.NoteEntity
import com.example.data.repository.HadithRepository
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    BOOKS,
    CHAPTERS,
    READER,
    SEARCH,
    COMPARE,
    BOOKMARKS,
    NOTES,
    ASSISTANT,
    SETTINGS
}

data class AssistantMessage(
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = HadithRepository(database.hadithDao(), database.userContentDao(), database.narratorDao())
    private val assistantService = HadithAssistantService()

    // ناوبری و پشته صفحات
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenStack = mutableListOf<Screen>()

    // تنظیمات خواننده حدیث
    private val _fontSizeSp = MutableStateFlow(20)
    val fontSizeSp: StateFlow<Int> = _fontSizeSp.asStateFlow()

    private val _showTranslation = MutableStateFlow(true)
    val showTranslation: StateFlow<Boolean> = _showTranslation.asStateFlow()

    private val _showIsnad = MutableStateFlow(true)
    val showIsnad: StateFlow<Boolean> = _showIsnad.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    // انتخاب‌های فعال
    private val _selectedBook = MutableStateFlow<BookEntity?>(null)
    val selectedBook: StateFlow<BookEntity?> = _selectedBook.asStateFlow()

    private val _selectedChapter = MutableStateFlow<ChapterEntity?>(null)
    val selectedChapter: StateFlow<ChapterEntity?> = _selectedChapter.asStateFlow()

    private val _selectedHadith = MutableStateFlow<HadithEntity?>(null)
    val selectedHadith: StateFlow<HadithEntity?> = _selectedHadith.asStateFlow()

    private val _collectionFilter = MutableStateFlow<String?>(null) // null = ALL, "SHIA", "SUNNI"
    val collectionFilter: StateFlow<String?> = _collectionFilter.asStateFlow()

    // داده‌های کتب و احادیث
    val allBooks = repository.allBooks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val shiaBooks = repository.shiaBooks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val sunniBooks = repository.sunniBooks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val featuredHadith = repository.featuredHadith.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val currentChapters: StateFlow<List<ChapterEntity>> = _selectedBook
        .flatMapLatest { book ->
            if (book != null) repository.getChapters(book.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentHadiths: StateFlow<List<HadithEntity>> = _selectedChapter
        .flatMapLatest { chapter ->
            if (chapter != null) repository.getHadithsByChapter(chapter.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // نشان‌شده‌ها، یادداشت‌ها و مقایسه
    val bookmarkedHadiths = repository.getBookmarkedHadiths().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allNotes = repository.allNotes.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val comparisonHadiths = repository.getComparisonHadiths().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // جستجوی پیشرفته
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchCollectionFilter = MutableStateFlow<String?>(null)
    val searchCollectionFilter: StateFlow<String?> = _searchCollectionFilter.asStateFlow()

    val searchResults: StateFlow<List<HadithEntity>> = combine(
        _searchQuery.debounce(250).distinctUntilChanged(),
        _searchCollectionFilter
    ) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        if (query.isBlank()) {
            flowOf(emptyList())
        } else {
            repository.searchHadiths(query, filter)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // دستیار پژوهشی
    private val _assistantMessages = MutableStateFlow<List<AssistantMessage>>(
        listOf(
            AssistantMessage(
                isUser = false,
                text = "سلام و درود بر شما پژوهشگر گرامی. من دستیار تخصصی «جامع‌الأحادیث منصور» هستم. می‌توانید درباره مفاهیم، راویان یا موضوعات احادیث شیعه و اهل‌سنت از من بپرسید."
            )
        )
    )
    val assistantMessages: StateFlow<List<AssistantMessage>> = _assistantMessages.asStateFlow()

    private val _isAssistantLoading = MutableStateFlow(false)
    val isAssistantLoading: StateFlow<Boolean> = _isAssistantLoading.asStateFlow()

    // پیام نوتیفیکیشن / اسنک‌بار موقت
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // متدهای ناوبری
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun handleBack(): Boolean {
        if (screenStack.isNotEmpty()) {
            _currentScreen.value = screenStack.removeAt(screenStack.size - 1)
            return true
        }
        if (_currentScreen.value != Screen.HOME) {
            _currentScreen.value = Screen.HOME
            return true
        }
        return false
    }

    fun openBook(book: BookEntity) {
        _selectedBook.value = book
        navigateTo(Screen.CHAPTERS)
    }

    fun openChapter(chapter: ChapterEntity) {
        _selectedChapter.value = chapter
        viewModelScope.launch {
            val hadiths = repository.getHadithsByChapter(chapter.id)
            hadiths.collect { list ->
                if (list.isNotEmpty()) {
                    _selectedHadith.value = list.first()
                }
            }
        }
        navigateTo(Screen.READER)
    }

    fun openHadith(hadith: HadithEntity) {
        _selectedHadith.value = hadith
        navigateTo(Screen.READER)
    }

    fun setCollectionFilter(filter: String?) {
        _collectionFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchCollectionFilter(filter: String?) {
        _searchCollectionFilter.value = filter
    }

    // تنظیمات خوانش
    fun changeFontSize(delta: Int) {
        _fontSizeSp.value = (_fontSizeSp.value + delta).coerceIn(14, 34)
    }

    fun toggleTranslation() {
        _showTranslation.value = !_showTranslation.value
    }

    fun toggleIsnad() {
        _showIsnad.value = !_showIsnad.value
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    // عملیات روی احادیث
    fun toggleBookmark(hadithId: Long) {
        viewModelScope.launch {
            repository.toggleBookmark(hadithId)
            _userMessage.value = "وضعیت نشان‌گذاری روایت تغییر یافت."
        }
    }

    fun toggleComparison(hadithId: Long) {
        viewModelScope.launch {
            repository.toggleComparison(hadithId)
            _userMessage.value = "روایت به میزکار مقایسه اضافه/حذف شد."
        }
    }

    fun clearComparison() {
        viewModelScope.launch {
            repository.clearComparison()
            _userMessage.value = "میزکار مقایسه پاکسازی شد."
        }
    }

    fun addNote(hadithId: Long, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.addNote(hadithId, content)
            _userMessage.value = "یادداشت پژوهشی با موفقیت ذخیره شد."
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
            _userMessage.value = "یادداشت حذف گردید."
        }
    }

    // پرسش از دستیار هوشمند
    fun askAssistant(question: String) {
        if (question.isBlank()) return
        val currentList = _assistantMessages.value.toMutableList()
        currentList.add(AssistantMessage(isUser = true, text = question))
        _assistantMessages.value = currentList
        _isAssistantLoading.value = true

        viewModelScope.launch {
            // واکشی احادیث مرتبط از پایگاه داده به عنوان بافتار متنی مستند
            val normalizedQuery = com.example.data.util.HadithTextNormalizer.normalize(question)
            val matchedHadiths = repository.searchHadiths(normalizedQuery, null)
            val retrieved = mutableListOf<HadithEntity>()
            matchedHadiths.collect { list ->
                retrieved.clear()
                retrieved.addAll(list)
            }

            val result = assistantService.answerResearchQuery(question, retrieved)
            val replyText = result.getOrDefault("پاسخی از پایگاه داده دریافت نشد.")
            val updated = _assistantMessages.value.toMutableList()
            updated.add(AssistantMessage(isUser = false, text = replyText))
            _assistantMessages.value = updated
            _isAssistantLoading.value = false
        }
    }
}
