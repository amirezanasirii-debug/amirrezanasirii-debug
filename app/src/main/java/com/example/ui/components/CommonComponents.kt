package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.ShiaGreen
import com.example.ui.theme.SunniGold
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MansourTopBar(
    title: String,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                maxLines = 1
            )
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت"
                    )
                }
            }
        },
        actions = {
            actions()
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "تنظیمات"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun MansourBottomBar(
    currentScreen: Screen,
    onSelectScreen: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.HOME,
            onClick = { onSelectScreen(Screen.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "خانه") },
            label = { Text("خانه", fontSize = 12.sp) },
            modifier = Modifier.testTag("nav_home"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPrimary,
                selectedTextColor = EmeraldPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            selected = currentScreen == Screen.BOOKS || currentScreen == Screen.CHAPTERS,
            onClick = { onSelectScreen(Screen.BOOKS) },
            icon = { Icon(Icons.Default.MenuBook, contentDescription = "کتابخانه") },
            label = { Text("کتابخانه", fontSize = 12.sp) },
            modifier = Modifier.testTag("nav_books"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPrimary,
                selectedTextColor = EmeraldPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            selected = currentScreen == Screen.SEARCH,
            onClick = { onSelectScreen(Screen.SEARCH) },
            icon = { Icon(Icons.Default.Search, contentDescription = "جستجو") },
            label = { Text("جستجو", fontSize = 12.sp) },
            modifier = Modifier.testTag("nav_search"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPrimary,
                selectedTextColor = EmeraldPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            selected = currentScreen == Screen.COMPARE,
            onClick = { onSelectScreen(Screen.COMPARE) },
            icon = { Icon(Icons.Default.CompareArrows, contentDescription = "مقایسه") },
            label = { Text("مقایسه", fontSize = 12.sp) },
            modifier = Modifier.testTag("nav_compare"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPrimary,
                selectedTextColor = EmeraldPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            selected = currentScreen == Screen.ASSISTANT,
            onClick = { onSelectScreen(Screen.ASSISTANT) },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "دستیار") },
            label = { Text("دستیار", fontSize = 12.sp) },
            modifier = Modifier.testTag("nav_assistant"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPrimary,
                selectedTextColor = EmeraldPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun SectBadge(
    collection: String,
    modifier: Modifier = Modifier
) {
    val isShia = collection == "SHIA"
    val badgeBg = if (isShia) ShiaGreen.copy(alpha = 0.14f) else SunniGold.copy(alpha = 0.16f)
    val badgeTextColor = if (isShia) ShiaGreen else SunniGold
    val label = if (isShia) "مذهب شیعه" else "مذهب اهل‌سنت"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeBg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = badgeTextColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
