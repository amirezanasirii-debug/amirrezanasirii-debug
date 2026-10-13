package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SepiaAccent
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val fontSizeSp by viewModel.fontSizeSp.collectAsState()
    val showTranslation by viewModel.showTranslation.collectAsState()
    val showIsnad by viewModel.showIsnad.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ۱. پوسته و ظاهر برنامه
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "پوسته و حالت مطالعه",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionBox(
                            title = "هماهنگ سیستم",
                            isSelected = themeMode == AppThemeMode.SYSTEM,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.setThemeMode(AppThemeMode.SYSTEM)
                        }
                        ThemeOptionBox(
                            title = "روشن ☀️",
                            isSelected = themeMode == AppThemeMode.LIGHT,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.setThemeMode(AppThemeMode.LIGHT)
                        }
                        ThemeOptionBox(
                            title = "تاریک 🌙",
                            isSelected = themeMode == AppThemeMode.DARK,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.setThemeMode(AppThemeMode.DARK)
                        }
                        ThemeOptionBox(
                            title = "سپیا 📜",
                            isSelected = themeMode == AppThemeMode.SEPIA,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.setThemeMode(AppThemeMode.SEPIA)
                        }
                    }
                }
            }
        }

        // ۲. تنظیمات اندازه قلم و نمایش
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "خوانش و قلم روایات",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "اندازه قلم متون عربی: $fontSizeSp sp", fontSize = 13.sp)
                    }

                    Slider(
                        value = fontSizeSp.toFloat(),
                        onValueChange = { viewModel.changeFontSize((it.toInt() - fontSizeSp)) },
                        valueRange = 14f..32f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldPrimary,
                            activeTrackColor = EmeraldPrimary
                        )
                    )

                    // پیش‌نمایش زنده قلم
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ • إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ",
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 1.8).sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // سوییچ‌های نمایش ترجمه و سند
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "نمایش ترجمه فارسی زیر متن", fontSize = 13.sp)
                        Switch(
                            checked = showTranslation,
                            onCheckedChange = { viewModel.toggleTranslation() },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "نمایش سلسله راویان و اسناد", fontSize = 13.sp)
                        Switch(
                            checked = showIsnad,
                            onCheckedChange = { viewModel.toggleIsnad() },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                        )
                    }
                }
            }
        }

        // ۳. درباره نرم‌افزار، منابع و مجوزها
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "درباره جامع‌الأحادیث منصور",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "نرم‌افزار «جامع‌الأحادیث منصور» بستری علمی و مدرن جهت پژوهش، مقایسه سندی و مطالعه متون روایی فریقین است. ویژگی‌های بنیادین این نسخه:",
                        fontSize = 12.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val highlights = listOf(
                        "تفکیک شفاف و بدون تداخل کتب اربعه شیعه و صحاح اهل‌سنت",
                        "موتور جستجوی پیشرفته با اعراب‌زدایی و تطبیق هوشمند ی/ک و آ/ا",
                        "پایگاه‌داده محلی و آفلاین Room SQLite بدون نیاز به اینترنت",
                        "میزکار مقایسه و تطبیق روایات متناظر در یک نگاه",
                        "دستیار هوش مصنوعی مستند با مدل Gemini 3.5 Flash بدون تحریف یا جعل سند",
                        "طراحی کاملاً فارسی، راست‌به‌چپ (RTL) و منطبق بر Material Design 3"
                    )

                    highlights.forEach { h ->
                        Text(
                            text = "• $h",
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "سازنده و پژوهشگر: حجت الاسلام امیررضا نصیری",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "توسعه‌یافته با رویکرد تحقیق تطبیقی اسناد و حفظ امانت علمی فریقین",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "نسخه: 1.0.0 (۲۰۲۶) • جامع‌الأحادیث منصور",
                        fontSize = 11.sp,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeOptionBox(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
