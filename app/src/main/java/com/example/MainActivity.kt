package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.MansourBottomBar
import com.example.ui.components.MansourTopBar
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.BookmarksAndNotesScreen
import com.example.ui.screens.BooksScreen
import com.example.ui.screens.ChaptersScreen
import com.example.ui.screens.ComparisonScreen
import com.example.ui.screens.HadithReaderScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MansourHadithTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()

            MansourHadithTheme(themeMode = themeMode) {
                // اعمال چیدمان قطعی راست‌به‌چپ (RTL) برای کل رابط کاربری فارسی
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MansourApp(viewModel = viewModel, onFinishActivity = { finish() })
                }
            }
        }
    }
}

@Composable
fun MansourApp(
    viewModel: MainViewModel,
    onFinishActivity: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedBook by viewModel.selectedBook.collectAsState()
    val selectedChapter by viewModel.selectedChapter.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // بازخورد پیام‌های کاربر در اسنک‌بار
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // مدیریت دکمه بازگشت فیزیکی یا نرم‌افزاری اندروید
    BackHandler(enabled = true) {
        val handled = viewModel.handleBack()
        if (!handled) {
            onFinishActivity()
        }
    }

    val screenTitle = when (currentScreen) {
        Screen.HOME -> "جامع‌الأحادیث منصور"
        Screen.BOOKS -> "کتابخانه مصادر روایی"
        Screen.CHAPTERS -> selectedBook?.title ?: "فهرست ابواب"
        Screen.READER -> selectedChapter?.titleArabic ?: "متن شریف روایات"
        Screen.SEARCH -> "جستجوی پیشرفته احادیث"
        Screen.COMPARE -> "میزکار تطبیق روایات"
        Screen.BOOKMARKS -> "احادیث نشان‌شده"
        Screen.NOTES -> "یادداشت‌های پژوهشی"
        Screen.ASSISTANT -> "دستیار پژوهشی حدیث"
        Screen.SETTINGS -> "تنظیمات و منابع"
    }

    val canNavigateBack = currentScreen != Screen.HOME

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MansourTopBar(
                title = screenTitle,
                canNavigateBack = canNavigateBack,
                onNavigateBack = {
                    if (!viewModel.handleBack()) onFinishActivity()
                },
                onOpenSettings = {
                    viewModel.navigateTo(Screen.SETTINGS)
                }
            )
        },
        bottomBar = {
            MansourBottomBar(
                currentScreen = currentScreen,
                onSelectScreen = { screen ->
                    viewModel.navigateTo(screen)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(viewModel = viewModel)
                Screen.BOOKS -> BooksScreen(viewModel = viewModel)
                Screen.CHAPTERS -> ChaptersScreen(viewModel = viewModel)
                Screen.READER -> HadithReaderScreen(viewModel = viewModel)
                Screen.SEARCH -> SearchScreen(viewModel = viewModel)
                Screen.COMPARE -> ComparisonScreen(viewModel = viewModel)
                Screen.BOOKMARKS -> BookmarksAndNotesScreen(viewModel = viewModel, initialTab = 0)
                Screen.NOTES -> BookmarksAndNotesScreen(viewModel = viewModel, initialTab = 1)
                Screen.ASSISTANT -> AssistantScreen(viewModel = viewModel)
                Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
