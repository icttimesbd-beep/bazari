package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppMode
import com.example.utils.L10n
import com.example.utils.LanguageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BazariTopBar(
    title: String,
    mode: AppMode = AppMode.PERSONAL,
    onToggleMode: (() -> Unit)? = null,
    showBackButton: Boolean = false,
    showLanguageToggle: Boolean = true,
    showLogo: Boolean = title.contains("বাজারি") || title.contains("Bazari"),
    onBackClick: () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showLogo) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_bazari_logo),
                        contentDescription = "Bazari Logo",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .border(
                                0.8.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                RoundedCornerShape(7.dp)
                            )
                            .testTag("bazari_logo_image")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                )
                if (onToggleMode != null) {
                    Box(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (mode == AppMode.STORE) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (mode == AppMode.STORE) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onToggleMode() }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("mode_toggle_badge")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (mode == AppMode.STORE) Icons.Default.Store else Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = if (mode == AppMode.STORE) MaterialTheme.colorScheme.onTertiaryContainer
                                else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = L10n.modeTitle(mode, currentLang),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = if (mode == AppMode.STORE) MaterialTheme.colorScheme.onTertiaryContainer
                                    else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = if (currentLang == AppLanguage.BN) "ফিরে যান" else "Back",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        actions = {
            if (showLanguageToggle) {
                LanguageToggleBadge(
                    currentLang = currentLang,
                    onToggle = { LanguageManager.toggleLanguage() }
                )
            }
            actions()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun LanguageToggleBadge(
    currentLang: AppLanguage,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = modifier
            .padding(end = 6.dp)
            .testTag("language_toggle_btn")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Language",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (currentLang == AppLanguage.BN) "বাং | EN" else "EN | বাং",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

enum class NavigationItem(
    val route: String,
    val titleBn: String,
    val titleEn: String,
    val icon: ImageVector,
    val testTag: String
) {
    HOME("home", "ফর্দ", "Lists", Icons.Default.Home, "nav_home"),
    EVENTS("events", "ইভেন্ট", "Events", Icons.Default.Celebration, "nav_events"),
    TEMPLATES("templates", "রেডিমেড", "Templates", Icons.Default.Widgets, "nav_templates"),
    HISTORY("history", "হিস্ট্রি", "History", Icons.Default.History, "nav_history"),
    SETTINGS("settings", "সেটিংস", "Settings", Icons.Default.Settings, "nav_settings")
}

@Composable
fun BazariBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        NavigationItem.values().forEach { item ->
            val isSelected = currentRoute == item.route
            val itemLabel = if (currentLang == AppLanguage.BN) item.titleBn else item.titleEn
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = itemLabel,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = itemLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
