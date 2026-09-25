package com.mosalah.quran.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosalah.quran.data.model.RevelationType
import com.mosalah.quran.data.model.Surah
import com.mosalah.quran.ui.theme.AppThemeMode
import com.mosalah.quran.ui.theme.IslamicEmeraldDark
import com.mosalah.quran.ui.theme.IslamicEmeraldPrimary
import com.mosalah.quran.ui.theme.QuranGoldLight
import com.mosalah.quran.ui.theme.QuranGoldPrimary
import androidx.compose.ui.res.stringResource
import com.mosalah.quran.R
import com.mosalah.quran.ui.viewmodel.QuranUiState
import com.mosalah.quran.ui.viewmodel.QuranViewModel
import com.mosalah.quran.ui.viewmodel.SurahFilterType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MushafScreen(
    viewModel: QuranViewModel,
    onOpenSurah: (surahNumber: Int, ayahNumber: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lastRead by viewModel.lastRead.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val allSurahs = remember { viewModel.getAllSurahs() }
    val filteredSurahs = remember(uiState.surahFilter, bookmarks, allSurahs) {
        when (uiState.surahFilter) {
            SurahFilterType.ALL -> allSurahs
            SurahFilterType.MAKKI -> allSurahs.filter { it.revelationType == RevelationType.MAKKI }
            SurahFilterType.MADANI -> allSurahs.filter { it.revelationType == RevelationType.MADANI }
            SurahFilterType.BOOKMARKED -> {
                val bookmarkedSurahNums = bookmarks.map { it.surahNumber }.toSet()
                allSurahs.filter { bookmarkedSurahNums.contains(it.number) }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mushaf_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Hero Header with Islamic motifs, Hijri Date & Theme Switcher
        item {
            HeroIslamicHeader(
                hijriDate = uiState.hijriDate,
                nextPrayer = uiState.prayerTimes.nextPrayerName,
                timeRemaining = uiState.prayerTimes.nextPrayerRemainingMinutes,
                currentThemeMode = uiState.themeMode,
                onSelectThemeMode = { viewModel.setThemeMode(it) }
            )
        }

        // Last Read Card (if exists)
        if (lastRead != null && !uiState.isSearching) {
            item {
                LastReadCard(
                    lastRead = lastRead!!,
                    onResume = {
                        onOpenSurah(lastRead!!.surahNumber, lastRead!!.ayahNumber)
                    }
                )
            }
        }

        // Search Bar
        item {
            QuranSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onClear = { viewModel.clearSearch() }
            )
        }

        // If searching, show search results
        if (uiState.isSearching) {
            item {
                Text(
                    text = "نتائج البحث (${uiState.searchResults.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            if (uiState.searchResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "لم يتم العثور على نتائج تطابق بحثك",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(uiState.searchResults) { result ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable {
                                onOpenSurah(result.surahNumber, result.ayahNumber)
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(IslamicEmeraldPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = IslamicEmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = result.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicEmeraldPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = result.snippet,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Filter Chips (All, Makki, Madani, Bookmarks)
            item {
                FilterChipsRow(
                    currentFilter = uiState.surahFilter,
                    onSelectFilter = { viewModel.setFilter(it) },
                    bookmarksCount = bookmarks.size
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "فهرس السور المباركة (${toArabicDigits(filteredSurahs.size)})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val filterSummary = when (uiState.surahFilter) {
                        SurahFilterType.ALL -> "١١٤ سورة كاملة"
                        SurahFilterType.MAKKI -> "٨٦ سورة مكية"
                        SurahFilterType.MADANI -> "٢٨ سورة مدنية"
                        SurahFilterType.BOOKMARKED -> "${toArabicDigits(filteredSurahs.size)} سورة مفضلة"
                    }
                    Text(
                        text = filterSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (filteredSurahs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.surahFilter == SurahFilterType.BOOKMARKED) {
                                "لا توجد سور في المفضلة بعد.\nيمكنك إضافة أي آية إلى المفضلة بالضغط على أيقونة الإشارة المرجعية 🔖"
                            } else {
                                "لا توجد سور مطابقة لهذا الفلتر"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }
                }
            } else {
                // List of Surahs
                items(filteredSurahs, key = { it.number }) { surah ->
                    SurahListItem(
                        surah = surah,
                        onClick = { onOpenSurah(surah.number, 1) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeroIslamicHeader(
    hijriDate: String,
    nextPrayer: String,
    timeRemaining: Long,
    currentThemeMode: AppThemeMode,
    onSelectThemeMode: (AppThemeMode) -> Unit
) {
    var showThemeMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        IslamicEmeraldDark,
                        IslamicEmeraldPrimary,
                        Color(0xFF0A3D2A)
                    )
                )
            )
            .padding(22.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "كتابٌ أُنزِلَ إِلَيْكَ مُبَارَكٌ لِيَدَّبَّرُوا آيَاتِهِ",
                        style = MaterialTheme.typography.bodySmall,
                        color = QuranGoldLight.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Theme Switcher Button
                    Box {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { showThemeMenu = true }
                                .testTag("theme_toggle_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (currentThemeMode) {
                                        AppThemeMode.LIGHT -> Icons.Default.LightMode
                                        AppThemeMode.DARK -> Icons.Default.DarkMode
                                        AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                    },
                                    contentDescription = "تبديل المظهر (فاتح / داكن)",
                                    tint = QuranGoldLight,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("الوضع الفاتح ☀️") },
                                trailingIcon = if (currentThemeMode == AppThemeMode.LIGHT) {
                                    { Icon(Icons.Default.Check, contentDescription = null, tint = IslamicEmeraldPrimary) }
                                } else null,
                                onClick = {
                                    onSelectThemeMode(AppThemeMode.LIGHT)
                                    showThemeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("الوضع الداكن 🌙") },
                                trailingIcon = if (currentThemeMode == AppThemeMode.DARK) {
                                    { Icon(Icons.Default.Check, contentDescription = null, tint = IslamicEmeraldPrimary) }
                                } else null,
                                onClick = {
                                    onSelectThemeMode(AppThemeMode.DARK)
                                    showThemeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("تلقائي (حسب النظام) ⚙️") },
                                trailingIcon = if (currentThemeMode == AppThemeMode.SYSTEM) {
                                    { Icon(Icons.Default.Check, contentDescription = null, tint = IslamicEmeraldPrimary) }
                                } else null,
                                onClick = {
                                    onSelectThemeMode(AppThemeMode.SYSTEM)
                                    showThemeMenu = false
                                }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Quran",
                            tint = QuranGoldLight,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Hijri Date and Prayer Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = hijriDate,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    color = QuranGoldPrimary.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Text(
                        text = "القادمة: $nextPrayer ($timeRemaining د)",
                        style = MaterialTheme.typography.labelMedium,
                        color = QuranGoldLight,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LastReadCard(
    lastRead: com.mosalah.quran.data.local.ReadingHistoryEntity,
    onResume: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onResume() }
            .testTag("resume_reading_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = QuranGoldPrimary.copy(alpha = 0.12f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(IslamicEmeraldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = "متابعة القراءة",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "متابعة القراءة (آخر موضع)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "سورة ${lastRead.surahName} - آية ${lastRead.ayahNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                color = IslamicEmeraldPrimary,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "واصل",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun QuranSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("search_text_field"),
        placeholder = { Text("ابحث عن سورة، آية، كلمة أو معنى...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = IslamicEmeraldPrimary
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = IslamicEmeraldPrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}

@Composable
fun FilterChipsRow(
    currentFilter: SurahFilterType,
    onSelectFilter: (SurahFilterType) -> Unit,
    bookmarksCount: Int
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = currentFilter == SurahFilterType.ALL,
                onClick = { onSelectFilter(SurahFilterType.ALL) },
                label = { Text("الكل (١١٤)") }
            )
        }
        item {
            FilterChip(
                selected = currentFilter == SurahFilterType.MAKKI,
                onClick = { onSelectFilter(SurahFilterType.MAKKI) },
                label = { Text("مكية (٨٦) 🕋") }
            )
        }
        item {
            FilterChip(
                selected = currentFilter == SurahFilterType.MADANI,
                onClick = { onSelectFilter(SurahFilterType.MADANI) },
                label = { Text("مدنية (٢٨) 🕌") }
            )
        }
        item {
            FilterChip(
                selected = currentFilter == SurahFilterType.BOOKMARKED,
                onClick = { onSelectFilter(SurahFilterType.BOOKMARKED) },
                label = { Text("المفضلة (${toArabicDigits(bookmarksCount)}) 🔖") }
            )
        }
    }
}

@Composable
fun SurahListItem(
    surah: Surah,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() }
            .testTag("surah_item_${surah.number}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left (or Start) info
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Number badge inside Islamic 8-point star representation
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 1.dp,
                            color = QuranGoldPrimary.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${surah.number}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IslamicEmeraldPrimary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "سُورَةُ ${surah.nameArabic}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IslamicEmeraldPrimary,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${if (surah.revelationType == RevelationType.MAKKI) "مكية" else "مدنية"} • ${surah.versesCount} آيات • ص ${surah.startPage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Surah details in Arabic
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = QuranGoldPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "الجزء ${surah.juzNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = QuranGoldPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ترتيبها ${surah.number}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
