package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.quran.QuranDataProvider
import com.example.ui.screens.DhikrDuasScreen
import com.example.ui.screens.MemorizationScreen
import com.example.ui.screens.MushafScreen
import com.example.ui.screens.PrayerQiblaScreen
import com.example.ui.screens.ReciterAudioScreen
import com.example.ui.screens.SurahReaderScreen
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicEmeraldMedium
import com.example.ui.theme.IslamicEmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.QuranGoldPrimary
import com.example.ui.viewmodel.QuranViewModel

sealed class BottomNavItem(
    val route: String,
    val titleArabic: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    val icon: ImageVector get() = selectedIcon

    object Mushaf : BottomNavItem(
        route = "mushaf",
        titleArabic = "المصحف",
        selectedIcon = Icons.Filled.AutoStories,
        unselectedIcon = Icons.Outlined.AutoStories,
        testTag = "nav_mushaf"
    )
    object Reciter : BottomNavItem(
        route = "reciter",
        titleArabic = "التلاوة",
        selectedIcon = Icons.Filled.Headphones,
        unselectedIcon = Icons.Outlined.Headphones,
        testTag = "nav_reciter"
    )
    object Memorization : BottomNavItem(
        route = "memorization",
        titleArabic = "الحفظ",
        selectedIcon = Icons.Filled.Psychology,
        unselectedIcon = Icons.Outlined.Psychology,
        testTag = "nav_memorization"
    )
    object Prayer : BottomNavItem(
        route = "prayer",
        titleArabic = "المواقيت",
        selectedIcon = Icons.Filled.Mosque,
        unselectedIcon = Icons.Outlined.Mosque,
        testTag = "nav_prayer"
    )
    object Dhikr : BottomNavItem(
        route = "dhikr",
        titleArabic = "الأذكار",
        selectedIcon = Icons.Filled.Spa,
        unselectedIcon = Icons.Outlined.Spa,
        testTag = "nav_dhikr"
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        QuranDataProvider.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            val viewModel: QuranViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            val systemInDark = isSystemInDarkTheme()
            val isDark = when (uiState.themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> systemInDark
            }
            MyApplicationTheme(darkTheme = isDark) {
                QuranApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun QuranApp(viewModel: QuranViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = listOf(
        BottomNavItem.Mushaf,
        BottomNavItem.Reciter,
        BottomNavItem.Memorization,
        BottomNavItem.Prayer,
        BottomNavItem.Dhikr
    )

    val isReaderScreen = currentRoute?.startsWith("reader") == true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isReaderScreen) {
                ModernBottomBar(
                    navItems = navItems,
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Mushaf.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(BottomNavItem.Mushaf.route) {
                MushafScreen(
                    viewModel = viewModel,
                    onOpenSurah = { surahNum, ayahNum ->
                        viewModel.selectSurah(surahNum, ayahNum)
                        navController.navigate("reader/$surahNum/$ayahNum")
                    }
                )
            }

            composable(BottomNavItem.Reciter.route) {
                ReciterAudioScreen(
                    viewModel = viewModel,
                    onOpenSurah = { surahNum, ayahNum ->
                        viewModel.selectSurah(surahNum, ayahNum)
                        navController.navigate("reader/$surahNum/$ayahNum")
                    }
                )
            }

            composable(BottomNavItem.Memorization.route) {
                MemorizationScreen(viewModel = viewModel)
            }

            composable(BottomNavItem.Prayer.route) {
                PrayerQiblaScreen(viewModel = viewModel)
            }

            composable(BottomNavItem.Dhikr.route) {
                DhikrDuasScreen(viewModel = viewModel)
            }

            composable(
                route = "reader/{surahNumber}/{ayahNumber}",
                arguments = listOf(
                    navArgument("surahNumber") { type = NavType.IntType },
                    navArgument("ayahNumber") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val surahNumber = backStackEntry.arguments?.getInt("surahNumber") ?: 1
                val ayahNumber = backStackEntry.arguments?.getInt("ayahNumber") ?: 1
                SurahReaderScreen(
                    surahNumber = surahNumber,
                    initialAyahNumber = ayahNumber,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun ModernBottomBar(
    navItems: List<BottomNavItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val barBgColor = MaterialTheme.colorScheme.surface
    val topBorderColor = if (isDark) {
        IslamicEmeraldMedium.copy(alpha = 0.35f)
    } else {
        QuranGoldPrimary.copy(alpha = 0.3f)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                spotColor = if (isDark) IslamicEmeraldDark else QuranGoldPrimary.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = barBgColor,
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, topBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val selected = currentRoute == item.route
                ModernBottomNavigationItem(
                    item = item,
                    selected = selected,
                    isDark = isDark,
                    onClick = { onNavigate(item.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ModernBottomNavigationItem(
    item: BottomNavItem,
    selected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = if (isDark) IslamicEmeraldLight else IslamicEmeraldPrimary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
    val goldAccent = QuranGoldPrimary

    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        label = "iconColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        label = "textColor"
    )

    val pillBgAlpha by animateFloatAsState(
        targetValue = if (selected) 0.16f else 0.0f,
        label = "pillBgAlpha"
    )

    val indicatorWidth by animateDpAsState(
        targetValue = if (selected) 16.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicatorWidth"
    )

    Column(
        modifier = modifier
            .testTag(item.testTag)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Modern Pill Container for the Icon
        Box(
            modifier = Modifier
                .scale(iconScale)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (selected) {
                        Brush.horizontalGradient(
                            listOf(
                                activeColor.copy(alpha = pillBgAlpha),
                                goldAccent.copy(alpha = pillBgAlpha * 0.8f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                )
                .then(
                    if (selected) {
                        Modifier.border(
                            width = 1.dp,
                            color = goldAccent.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(20.dp)
                        )
                    } else Modifier
                )
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.titleArabic,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Arabic Label with dynamic font weight and styling
        Text(
            text = item.titleArabic,
            color = textColor,
            fontSize = if (selected) 11.5.sp else 10.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )

        // Micro-indicator pill/capsule underneath active tab
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .height(3.dp)
                .width(indicatorWidth)
                .clip(RoundedCornerShape(1.5.dp))
                .background(
                    if (indicatorWidth > 0.dp) {
                        Brush.horizontalGradient(
                            listOf(
                                goldAccent,
                                activeColor
                            )
                        )
                    } else {
                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                )
        )
    }
}
