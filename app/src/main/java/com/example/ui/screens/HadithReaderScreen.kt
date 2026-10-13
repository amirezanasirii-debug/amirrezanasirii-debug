package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HadithEntity
import com.example.ui.components.SectBadge
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SepiaAccent
import com.example.ui.theme.SepiaBackground
import com.example.ui.theme.SepiaCard
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HadithReaderScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedHadith by viewModel.selectedHadith.collectAsState()
    val selectedChapter by viewModel.selectedChapter.collectAsState()
    val selectedBook by viewModel.selectedBook.collectAsState()
    val currentHadiths by viewModel.currentHadiths.collectAsState()

    val fontSizeSp by viewModel.fontSizeSp.collectAsState()
    val showTranslation by viewModel.showTranslation.collectAsState()
    val showIsnad by viewModel.showIsnad.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    var activeNoteDialogHadithId by remember { mutableStateOf<Long?>(null) }
    var noteInputText by remember { mutableStateOf("") }

    // اگر حدیث مشخصی انتخاب نشده، تمام احادیث باب نمایش داده می‌شوند
    val hadithsToDisplay = if (selectedHadith != null && currentHadiths.isEmpty()) {
        listOf(selectedHadith!!)
    } else if (currentHadiths.isNotEmpty()) {
        currentHadiths
    } else if (selectedHadith != null) {
        listOf(selectedHadith!!)
    } else {
        emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("hadith_reader_screen")
    ) {
        // ۱. نوار ابزار تنظیمات خواننده (قلم، ترجمه، سند، تم سپیا)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // تنظیم اندازه قلم
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.changeFontSize(-2) },
                        modifier = Modifier.size(32.dp).testTag("decrease_font_size")
                    ) {
                        Text("A-", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text(
                        text = "$fontSizeSp",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(
                        onClick = { viewModel.changeFontSize(+2) },
                        modifier = Modifier.size(32.dp).testTag("increase_font_size")
                    ) {
                        Text("A+", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // فعال/غیرفعال ترجمه فارسی
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (showTranslation) EmeraldPrimary.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { viewModel.toggleTranslation() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("toggle_translation")
                ) {
                    Text(
                        text = "ترجمه (فا)",
                        color = if (showTranslation) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // فعال/غیرفعال سند
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (showIsnad) EmeraldPrimary.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { viewModel.toggleIsnad() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("toggle_isnad")
                ) {
                    Text(
                        text = "سلسله سند",
                        color = if (showIsnad) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // انتخاب حالت سپیا (پوست‌نوشته)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (themeMode == AppThemeMode.SEPIA) SepiaAccent.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable {
                            viewModel.setThemeMode(
                                if (themeMode == AppThemeMode.SEPIA) AppThemeMode.SYSTEM else AppThemeMode.SEPIA
                            )
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("toggle_sepia")
                ) {
                    Text(
                        text = "سپیا 📜",
                        color = if (themeMode == AppThemeMode.SEPIA) SepiaAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ۲. محتوای احادیث
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(hadithsToDisplay) { hadith ->
                HadithDisplayCard(
                    hadith = hadith,
                    fontSizeSp = fontSizeSp,
                    showTranslation = showTranslation,
                    showIsnad = showIsnad,
                    onToggleBookmark = { viewModel.toggleBookmark(hadith.id) },
                    onToggleComparison = { viewModel.toggleComparison(hadith.id) },
                    onAddNote = {
                        activeNoteDialogHadithId = hadith.id
                        noteInputText = ""
                    },
                    onCopy = {
                        val textToCopy = """
                            ${hadith.textArabic}
                            
                            ترجمه: ${hadith.textPersian}
                            
                            سند: ${hadith.isnadChain}
                            منبع: ${hadith.sourceReference}
                            (برگرفته از نرم‌افزار جامع‌الأحادیث منصور)
                        """.trimIndent()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Hadith Mansour", textToCopy)
                        clipboard.setPrimaryClip(clip)
                    },
                    onShare = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${hadith.textArabic}\n\nترجمه: ${hadith.textPersian}\n\nمنبع: ${hadith.sourceReference}\n(جامع‌الأحادیث منصور)"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری حدیث"))
                    }
                )
            }
        }
    }

    // دیالوگ ثبت یادداشت علمی
    if (activeNoteDialogHadithId != null) {
        AlertDialog(
            onDismissRequest = { activeNoteDialogHadithId = null },
            title = { Text("ثبت یادداشت پژوهشی", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "نکات تحلیلی، دلالت‌های فقهی و شواهد سندی خود را پیرامون این روایت یادداشت فرمایید:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = noteInputText,
                        onValueChange = { noteInputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("متن یادداشت علمی…") },
                        minLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addNote(activeNoteDialogHadithId!!, noteInputText)
                        activeNoteDialogHadithId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("ذخیره یادداشت")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { activeNoteDialogHadithId = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun HadithDisplayCard(
    hadith: HadithEntity,
    fontSizeSp: Int,
    showTranslation: Boolean,
    showIsnad: Boolean,
    onToggleBookmark: () -> Unit,
    onToggleComparison: () -> Unit,
    onAddNote: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hadith_item_${hadith.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // سربرگ شماره و مذهب
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "حدیث شماره ${hadith.numberInBook}",
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = hadith.grade,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldAccent,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                SectBadge(collection = hadith.collection)
            }

            // سلسله سند روایی (در صورت فعال بودن)
            AnimatedVisibility(visible = showIsnad) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "سلسله راویان و سند:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = hadith.isnadChain,
                                fontSize = 12.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // متن شریف عربی حدیث
            Text(
                text = hadith.textArabic,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSizeSp.sp,
                    lineHeight = (fontSizeSp * 1.8).sp,
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // ترجمه فارسی (در صورت فعال بودن)
            AnimatedVisibility(visible = showTranslation) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "ترجمه فارسی:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = hadith.textPersian,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = (fontSizeSp - 4).coerceAtLeast(13).sp,
                                    lineHeight = ((fontSizeSp - 4) * 1.7).coerceAtLeast(22.0).sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Justify
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ارجاع کتابشناسی دقیق
            Text(
                text = hadith.sourceReference,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // نوار دکمه‌های عملیات (نشان، مقایسه، یادداشت، کپی، اشتراک)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(36.dp).testTag("action_bookmark_${hadith.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "نشان کردن",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onToggleComparison,
                        modifier = Modifier.size(36.dp).testTag("action_compare_${hadith.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "افزودن به مقایسه",
                            tint = GoldAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick = onAddNote,
                        modifier = Modifier.size(36.dp).testTag("action_note_${hadith.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "یادداشت علمی",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(36.dp).testTag("action_copy_${hadith.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "کپی با ارجاع",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(36.dp).testTag("action_share_${hadith.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "اشتراک‌گذاری",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
