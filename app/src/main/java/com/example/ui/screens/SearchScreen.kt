package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HadithEntity
import com.example.data.util.HadithTextNormalizer
import com.example.ui.components.SectBadge
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchCollectionFilter by viewModel.searchCollectionFilter.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val allBooks by viewModel.allBooks.collectAsState()

    var isExactPhrase by remember { mutableStateOf(false) }
    var selectedBookFilter by remember { mutableStateOf<String?>(null) }
    var narratorFilter by remember { mutableStateOf("") }
    var showAdvancedFilters by remember { mutableStateOf(false) }
    var isBookDropdownOpen by remember { mutableStateOf(false) }

    val recentSearches = remember {
        mutableStateListOf(
            "العقل",
            "الأعمال بالنيات",
            "حسن الخلق",
            "العلماء ورثة الأنبیاء",
            "الصبر"
        )
    }

    // فیلتر کردن نتایج بر اساس گزینه‌های تکمیلی کتاب، راوی و عبارت دقیق
    val filteredResults = remember(
        searchResults,
        isExactPhrase,
        selectedBookFilter,
        narratorFilter,
        searchQuery
    ) {
        searchResults.filter { hadith ->
            val matchesBook = selectedBookFilter == null || hadith.bookId == selectedBookFilter
            val matchesNarrator = narratorFilter.isBlank() ||
                    hadith.isnadChain.contains(narratorFilter, ignoreCase = true) ||
                    HadithTextNormalizer.normalize(hadith.isnadChain)
                        .contains(HadithTextNormalizer.normalize(narratorFilter))

            val matchesPhrase = if (isExactPhrase && searchQuery.isNotBlank()) {
                val normalizedQuery = HadithTextNormalizer.normalize(searchQuery)
                val normalizedArabic = HadithTextNormalizer.normalize(hadith.textArabic)
                val normalizedPersian = HadithTextNormalizer.normalize(hadith.textPersian)
                normalizedArabic.contains(normalizedQuery) || normalizedPersian.contains(normalizedQuery)
            } else {
                true
            }

            matchesBook && matchesNarrator && matchesPhrase
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("search_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ۱. نوار جستجوی پیشرفته
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    viewModel.setSearchQuery(it)
                    if (it.isNotBlank() && !recentSearches.contains(it) && it.length > 2) {
                        if (recentSearches.size > 8) recentSearches.removeLast()
                        recentSearches.add(0, it)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input_field"),
                placeholder = {
                    Text(
                        "جستجو در کتب، احادیث، ترجمه‌ها و راویان…",
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "جستجو",
                        tint = EmeraldPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.setSearchQuery("") },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "پاک کردن")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )
        }

        // ۲. ردیف فیلترهای مذهبی اصلی (فریقین)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = searchCollectionFilter == null,
                    onClick = { viewModel.setSearchCollectionFilter(null) },
                    label = { Text("همه مصادر", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_all")
                )
                FilterChip(
                    selected = searchCollectionFilter == "SHIA",
                    onClick = { viewModel.setSearchCollectionFilter("SHIA") },
                    label = { Text("مصادر شیعه", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_shia")
                )
                FilterChip(
                    selected = searchCollectionFilter == "SUNNI",
                    onClick = { viewModel.setSearchCollectionFilter("SUNNI") },
                    label = { Text("مصادر اهل‌سنت", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_sunni")
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { showAdvancedFilters = !showAdvancedFilters },
                    modifier = Modifier.testTag("toggle_advanced_filters")
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "فیلترهای پیشرفته",
                        tint = if (showAdvancedFilters) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ۳. پنل فیلترهای پیشرفته (کتاب، راوی، عبارت دقیق)
        item {
            AnimatedVisibility(visible = showAdvancedFilters) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "فیلترهای اختصاصی پژوهش",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // الف) انتخاب کتاب خاص
                        Box {
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isBookDropdownOpen = true },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedBookFilter?.let { id ->
                                            allBooks.find { it.id == id }?.title
                                        } ?: "همه کتاب‌ها (نامحدود)",
                                        fontSize = 12.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = EmeraldPrimary
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isBookDropdownOpen,
                                onDismissRequest = { isBookDropdownOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("همه کتاب‌ها") },
                                    onClick = {
                                        selectedBookFilter = null
                                        isBookDropdownOpen = false
                                    }
                                )
                                allBooks.forEach { book ->
                                    DropdownMenuItem(
                                        text = { Text("${book.title} (${if (book.collection == "SHIA") "شیعه" else "سنی"})") },
                                        onClick = {
                                            selectedBookFilter = book.id
                                            isBookDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // ب) فیلتر نام راوی یا محدوده سند
                        OutlinedTextField(
                            value = narratorFilter,
                            onValueChange = { narratorFilter = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("نام راوی در سند (مثال: اصبغ بن نباته، علقمه…)", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // ج) گزینه عبارت دقیق
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isExactPhrase,
                                onCheckedChange = { isExactPhrase = it },
                                colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                            )
                            Text(
                                text = "جستجوی عبارت دقیق عیناً پشت سر هم",
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { isExactPhrase = !isExactPhrase }
                            )
                        }
                    }
                }
            }
        }

        // ۴. جستجوهای اخیر (وقتی کادر خالی است)
        if (searchQuery.isBlank()) {
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "جستجوهای پرکاربرد و اخیر",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recentSearches.forEach { keyword ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { viewModel.setSearchQuery(keyword) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = keyword,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // راهنمای علمی اعراب‌زدایی
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "نکته پژوهشی در موتور جستجوی منصور:",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "موتور جستجوی نرم‌افزار به صورت خودکار انواع اعراب، تنوین، تشدید و تفاوت‌های حروف (ی/ي، ک/ك، آ/ا) را یکسان‌سازی می‌کند؛ بنابراین می‌توانید با کیبورد فارسی یا عربی به آسانی جستجو فرمایید.",
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // ۵. نمایش تعداد نتایج یافت‌شده
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "نتایج پژوهش: ${filteredResults.size} روایت",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = EmeraldPrimary
                    )
                    if (isExactPhrase) {
                        Text(
                            text = "(حالت عبارت دقیق)",
                            fontSize = 11.sp,
                            color = GoldAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ۶. وضعیت بدون نتیجه
            if (filteredResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "هیچ روایتی مطابق با شرایط جستجو یافت نشد.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            } else {
                // ۷. لیست کارت‌های روایات منطبق با هایلایت عبارت جستجو
                items(filteredResults) { hadith ->
                    SearchResultCard(
                        hadith = hadith,
                        query = searchQuery,
                        onOpen = { viewModel.openHadith(hadith) },
                        onToggleBookmark = { viewModel.toggleBookmark(hadith.id) },
                        onToggleComparison = { viewModel.toggleComparison(hadith.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    hadith: HadithEntity,
    query: String,
    onOpen: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleComparison: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("search_result_item_${hadith.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hadith.sourceReference,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        fontSize = 12.sp
                    )
                )
                SectBadge(collection = hadith.collection)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // متن عربی با هایلایت کردن کلیدواژه‌های جستجو
            val highlightedArabic = highlightQuery(hadith.textArabic, query)
            Text(
                text = highlightedArabic,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ترجمه فارسی با هایلایت
            val highlightedPersian = highlightQuery(hadith.textPersian, query)
            Text(
                text = highlightedPersian,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "نشان کردن",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onToggleComparison,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "افزودن به مقایسه",
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "مشاهده سند و متن کامل ←",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
            }
        }
    }
}

/**
 * هایلایت کردن واژه‌های منطبق در متن عربی یا فارسی با در نظر گرفتن نرمال‌سازی
 */
fun highlightQuery(text: String, query: String): AnnotatedString {
    if (query.isBlank() || text.isBlank()) {
        return AnnotatedString(text)
    }

    val normalizedText = HadithTextNormalizer.normalize(text)
    val normalizedQuery = HadithTextNormalizer.normalize(query)

    val words = normalizedQuery.split("\\s+".toRegex()).filter { it.isNotBlank() }
    val spanStyles = mutableListOf<AnnotatedString.Range<SpanStyle>>()

    // پیدا کردن مطابقت‌ها
    words.forEach { word ->
        var startIndex = 0
        while (startIndex < text.length) {
            val index = normalizedText.indexOf(word, startIndex)
            if (index == -1) break

            // محاسبه حدودی ایندکس روی متن اصلی به دلیل نزدیکی طول
            val matchEnd = (index + word.length).coerceAtMost(text.length)
            spanStyles.add(
                AnnotatedString.Range(
                    item = SpanStyle(
                        background = GoldAccent.copy(alpha = 0.38f),
                        fontWeight = FontWeight.Bold
                    ),
                    start = index.coerceAtMost(text.length),
                    end = matchEnd
                )
            )
            startIndex = index + word.length
        }
    }

    return AnnotatedString(text = text, spanStyles = spanStyles)
}
