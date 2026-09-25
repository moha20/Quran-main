package com.mosalah.quran.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosalah.quran.data.local.BookmarkEntity
import com.mosalah.quran.data.model.Ayah
import com.mosalah.quran.data.model.RevelationType
import com.mosalah.quran.data.model.Surah
import com.mosalah.quran.service.audio.AudioPlaybackStatus
import com.mosalah.quran.service.audio.AudioPlayerUiState
import com.mosalah.quran.ui.components.AddNoteDialog
import com.mosalah.quran.ui.components.ShareVerseDialog
import com.mosalah.quran.ui.components.TafsirDialog
import com.mosalah.quran.ui.components.WordAnalysisDialog
import com.mosalah.quran.ui.theme.AppThemeMode
import com.mosalah.quran.ui.theme.IslamicEmeraldPrimary
import com.mosalah.quran.ui.theme.QuranGoldLight
import com.mosalah.quran.ui.theme.QuranGoldPrimary
import com.mosalah.quran.ui.viewmodel.QuranViewModel
import com.mosalah.quran.ui.viewmodel.ReaderViewMode
import kotlinx.coroutines.launch

fun toArabicDigits(num: Int): String {
    val arabicNumerals = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return num.toString().map { if (it in '0'..'9') arabicNumerals[it - '0'] else it }.joinToString("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReaderScreen(
    surahNumber: Int,
    initialAyahNumber: Int = 1,
    viewModel: QuranViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val audioState by viewModel.audioState.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    val surah = viewModel.repository.getSurahByNumber(surahNumber)
    val ayahs = remember(surahNumber) { viewModel.getAyahsForSurah(surahNumber) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Group ayahs by Medina Mushaf page
    val pagesMap = remember(ayahs, surah) {
        ayahs.groupBy { if (it.page > 0) it.page else (surah?.startPage ?: 1) }
    }
    val pagesList = remember(pagesMap) {
        pagesMap.keys.sorted().map { pageNum -> pageNum to (pagesMap[pageNum] ?: emptyList()) }
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pagesList.size.coerceAtLeast(1) }
    )

    var showFontSettings by remember { mutableStateOf(false) }

    // Navigate to initial Ayah in both Page and List mode
    LaunchedEffect(initialAyahNumber) {
        if (initialAyahNumber > 1 && initialAyahNumber <= ayahs.size) {
            val target = if (surahNumber != 9) initialAyahNumber else initialAyahNumber - 1
            listState.animateScrollToItem(target)

            val pIdx = pagesList.indexOfFirst { (_, pAyahs) -> pAyahs.any { it.ayahNumber == initialAyahNumber } }
            if (pIdx >= 0) {
                pagerState.animateScrollToPage(pIdx)
            }
        }
    }

    // Auto-scroll / Auto-flip page synchronized with active playing Ayah
    LaunchedEffect(audioState.ayahNumber, audioState.status, audioState.surahNumber) {
        if (audioState.surahNumber == surahNumber &&
            (audioState.status == AudioPlaybackStatus.PLAYING || audioState.status == AudioPlaybackStatus.BUFFERING)
        ) {
            val targetIndex = if (surahNumber != 9) audioState.ayahNumber else audioState.ayahNumber - 1
            val maxIndex = ayahs.size + if (surahNumber != 9) 0 else -1
            if (targetIndex in 0..maxIndex) {
                try {
                    listState.animateScrollToItem(targetIndex)
                } catch (_: Exception) {}
            }

            val pIdx = pagesList.indexOfFirst { (_, pAyahs) -> pAyahs.any { it.ayahNumber == audioState.ayahNumber } }
            if (pIdx >= 0 && pIdx != pagerState.currentPage) {
                try {
                    pagerState.animateScrollToPage(pIdx)
                } catch (_: Exception) {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "سُورَةُ ${surah?.nameArabic ?: ""}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        val totalPages = pagesList.size
                        Text(
                            text = "${if (surah?.revelationType == RevelationType.MAKKI) "مكية" else "مدنية"} • ${surah?.versesCount ?: 0} آيات • ${totalPages} صفحات • الجزء ${surah?.juzNumber ?: 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    // Theme Mode Switcher
                    var showThemeMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { showThemeMenu = true },
                            modifier = Modifier.testTag("reader_theme_toggle")
                        ) {
                            Icon(
                                imageVector = when (uiState.themeMode) {
                                    AppThemeMode.LIGHT -> Icons.Default.LightMode
                                    AppThemeMode.DARK -> Icons.Default.DarkMode
                                    AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                },
                                contentDescription = "مظهر المصحف (فاتح / داكن)",
                                tint = IslamicEmeraldPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("الوضع الفاتح ☀️") },
                                trailingIcon = if (uiState.themeMode == AppThemeMode.LIGHT) {
                                    { Icon(Icons.Default.Check, contentDescription = null, tint = IslamicEmeraldPrimary) }
                                } else null,
                                onClick = {
                                    viewModel.setThemeMode(AppThemeMode.LIGHT)
                                    showThemeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("الوضع الداكن 🌙") },
                                trailingIcon = if (uiState.themeMode == AppThemeMode.DARK) {
                                    { Icon(Icons.Default.Check, contentDescription = null, tint = IslamicEmeraldPrimary) }
                                } else null,
                                onClick = {
                                    viewModel.setThemeMode(AppThemeMode.DARK)
                                    showThemeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("حسب النظام ⚙️") },
                                trailingIcon = if (uiState.themeMode == AppThemeMode.SYSTEM) {
                                    { Icon(Icons.Default.Check, contentDescription = null, tint = IslamicEmeraldPrimary) }
                                } else null,
                                onClick = {
                                    viewModel.setThemeMode(AppThemeMode.SYSTEM)
                                    showThemeMenu = false
                                }
                            )
                        }
                    }

                    // Font Size control
                    IconButton(onClick = { showFontSettings = !showFontSettings }) {
                        Icon(Icons.Default.FormatSize, contentDescription = "حجم الخط")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Audio player bar
            if (audioState.status != AudioPlaybackStatus.IDLE) {
                AudioBottomBar(
                    audioState = audioState,
                    onPlayPause = { viewModel.toggleAudioPlayback() },
                    onNext = { viewModel.audioPlayer.playNextAyah() },
                    onPrev = { viewModel.audioPlayer.playPreviousAyah() },
                    onSpeedChange = {
                        val nextSpeed = when (audioState.playbackSpeed) {
                            1.0f -> 1.25f
                            1.25f -> 1.5f
                            1.5f -> 0.75f
                            else -> 1.0f
                        }
                        viewModel.setAudioSpeed(nextSpeed)
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Font settings expandable panel
            AnimatedVisibility(visible = showFontSettings) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("حجم الخط العربي: ${uiState.arabicFontSize.toInt()} sp", style = MaterialTheme.typography.bodySmall)
                            Text("بِسْمِ اللَّهِ", fontSize = uiState.arabicFontSize.sp, fontWeight = FontWeight.Bold, color = IslamicEmeraldPrimary)
                        }
                        Slider(
                            value = uiState.arabicFontSize,
                            onValueChange = { viewModel.updateFontSize(it, uiState.translationFontSize) },
                            valueRange = 18f..38f
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "مظهر المصحف الشريف:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.themeMode == AppThemeMode.LIGHT,
                                onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) },
                                label = { Text("فاتح ☀️") },
                                modifier = Modifier.testTag("reader_chip_light")
                            )
                            FilterChip(
                                selected = uiState.themeMode == AppThemeMode.DARK,
                                onClick = { viewModel.setThemeMode(AppThemeMode.DARK) },
                                label = { Text("داكن 🌙") },
                                modifier = Modifier.testTag("reader_chip_dark")
                            )
                            FilterChip(
                                selected = uiState.themeMode == AppThemeMode.SYSTEM,
                                onClick = { viewModel.setThemeMode(AppThemeMode.SYSTEM) },
                                label = { Text("تلقائي ⚙️") },
                                modifier = Modifier.testTag("reader_chip_system")
                            )
                        }
                    }
                }
            }

            // Mode Selector: "صفحات المصحف" (Pages View) vs "قائمة الآيات" (Ayah List View)
            TabRow(
                selectedTabIndex = if (uiState.readerViewMode == ReaderViewMode.PAGES) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = IslamicEmeraldPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = uiState.readerViewMode == ReaderViewMode.PAGES,
                    onClick = { viewModel.setReaderViewMode(ReaderViewMode.PAGES) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("صفحات المصحف (${pagesList.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = uiState.readerViewMode == ReaderViewMode.AYAHS,
                    onClick = { viewModel.setReaderViewMode(ReaderViewMode.AYAHS) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.ViewList, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("قائمة الآيات (${ayahs.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // ================= MODE 1: PAGES VIEW (عرض صفحات المصحف) =================
            if (uiState.readerViewMode == ReaderViewMode.PAGES) {
                SurahPagesContent(
                    surah = surah,
                    pagesList = pagesList,
                    pagerState = pagerState,
                    audioState = audioState,
                    arabicFontSize = uiState.arabicFontSize,
                    bookmarks = bookmarks,
                    onPlayAyahAudio = { sNum, aNum ->
                        if (audioState.surahNumber == sNum && audioState.ayahNumber == aNum && audioState.status != AudioPlaybackStatus.IDLE) {
                            viewModel.toggleAudioPlayback()
                        } else {
                            viewModel.playAyahAudio(sNum, aNum)
                        }
                    },
                    onToggleBookmark = { ayah ->
                        viewModel.toggleBookmark(
                            surahNumber = ayah.surahNumber,
                            ayahNumber = ayah.ayahNumber,
                            surahName = surah?.nameArabic ?: "",
                            ayahText = ayah.textArabic
                        )
                    },
                    onTafsir = { viewModel.showTafsirDialog(it) },
                    onWordAnalysis = { viewModel.showWordAnalysisDialog(it) },
                    onAddNote = { viewModel.showNoteDialog(it) },
                    onShare = { viewModel.showShareDialog(it) }
                )
            }
            // ================= MODE 2: AYAHS LIST VIEW (عرض الآيات) =================
            else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("surah_reader_verses_list"),
                    contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
                ) {
                    // Bismillah Banner (except for Surah 9 At-Tawbah)
                    if (surahNumber != 9) {
                        item {
                            BismillahHeader()
                        }
                    }

                    // Ayahs list with Page Separator headers
                    ayahs.forEachIndexed { index, ayah ->
                        val isFirstAyahOfPage = index == 0 || ayah.page != ayahs[index - 1].page
                        if (isFirstAyahOfPage && ayah.page > 0) {
                            item(key = "page_sep_${ayah.page}_$index") {
                                PageSeparatorHeader(
                                    pageNumber = ayah.page,
                                    juzNumber = surah?.juzNumber ?: 1
                                )
                            }
                        }

                        item(key = "${ayah.surahNumber}_${ayah.ayahNumber}") {
                            val isCurrentAyahActive = audioState.surahNumber == surahNumber &&
                                    audioState.ayahNumber == ayah.ayahNumber &&
                                    audioState.status != AudioPlaybackStatus.IDLE

                            val isPlayingThisAyah = isCurrentAyahActive && audioState.status == AudioPlaybackStatus.PLAYING
                            val isBufferingThisAyah = isCurrentAyahActive && audioState.status == AudioPlaybackStatus.BUFFERING

                            val isBookmarked = bookmarks.any {
                                it.surahNumber == surahNumber && it.ayahNumber == ayah.ayahNumber
                            }

                            AyahCardItem(
                                ayah = ayah,
                                surahName = surah?.nameArabic ?: "",
                                isAudioActive = isCurrentAyahActive,
                                isPlaying = isPlayingThisAyah,
                                isBuffering = isBufferingThisAyah,
                                isBookmarked = isBookmarked,
                                arabicFontSize = uiState.arabicFontSize,
                                onPlayAudio = {
                                    if (isCurrentAyahActive) {
                                        viewModel.toggleAudioPlayback()
                                    } else {
                                        viewModel.playAyahAudio(surahNumber, ayah.ayahNumber)
                                    }
                                },
                                onToggleBookmark = {
                                    viewModel.toggleBookmark(
                                        surahNumber = surahNumber,
                                        ayahNumber = ayah.ayahNumber,
                                        surahName = surah?.nameArabic ?: "",
                                        ayahText = ayah.textArabic
                                    )
                                },
                                onTafsir = { viewModel.showTafsirDialog(ayah) },
                                onWordAnalysis = { viewModel.showWordAnalysisDialog(ayah) },
                                onAddNote = { viewModel.showNoteDialog(ayah) },
                                onShare = { viewModel.showShareDialog(ayah) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Active Dialogs
    uiState.activeTafsirAyah?.let { tafsirAyah ->
        TafsirDialog(
            ayah = tafsirAyah,
            surahName = surah?.nameArabic ?: "",
            onLoadTafsir = { type, sNum, aNum, fallback ->
                viewModel.getTafsir(type, sNum, aNum, fallback)
            },
            onDismiss = { viewModel.showTafsirDialog(null) }
        )
    }

    uiState.activeWordAnalysisAyah?.let { wordAyah ->
        WordAnalysisDialog(
            ayah = wordAyah,
            onDismiss = { viewModel.showWordAnalysisDialog(null) }
        )
    }

    uiState.activeNoteAyah?.let { noteAyah ->
        AddNoteDialog(
            ayah = noteAyah,
            surahName = surah?.nameArabic ?: "",
            onSave = { noteText, colorHex, tag ->
                viewModel.addNote(
                    surahNumber = noteAyah.surahNumber,
                    ayahNumber = noteAyah.ayahNumber,
                    surahName = surah?.nameArabic ?: "",
                    ayahSnippet = noteAyah.textArabic.take(40),
                    noteContent = noteText,
                    colorHex = colorHex,
                    tag = tag
                )
            },
            onDismiss = { viewModel.showNoteDialog(null) }
        )
    }

    uiState.activeShareAyah?.let { shareAyah ->
        ShareVerseDialog(
            ayah = shareAyah,
            surahName = surah?.nameArabic ?: "",
            onDismiss = { viewModel.showShareDialog(null) }
        )
    }
}

@Composable
fun SurahPagesContent(
    surah: Surah?,
    pagesList: List<Pair<Int, List<Ayah>>>,
    pagerState: PagerState,
    audioState: AudioPlayerUiState,
    arabicFontSize: Float,
    bookmarks: List<BookmarkEntity>,
    onPlayAyahAudio: (Int, Int) -> Unit,
    onToggleBookmark: (Ayah) -> Unit,
    onTafsir: (Ayah) -> Unit,
    onWordAnalysis: (Ayah) -> Unit,
    onAddNote: (Ayah) -> Unit,
    onShare: (Ayah) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedAyahOnPage by remember { mutableStateOf<Ayah?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("surah_pages_container")
    ) {
        // Fast Page Selector & Navigator Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (pagerState.currentPage > 0) {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    }
                },
                enabled = pagerState.currentPage > 0
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "الصفحة السابقة")
            }

            val currentPageNum = if (pagesList.isNotEmpty() && pagerState.currentPage in pagesList.indices) {
                pagesList[pagerState.currentPage].first
            } else (surah?.startPage ?: 1)

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = QuranGoldPrimary.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, QuranGoldPrimary.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "صفحة ${toArabicDigits(currentPageNum)} (ص ${pagerState.currentPage + 1} من ${pagesList.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = IslamicEmeraldPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            IconButton(
                onClick = {
                    if (pagerState.currentPage < pagesList.size - 1) {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                enabled = pagerState.currentPage < pagesList.size - 1
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "الصفحة التالية")
            }
        }

        // Quick page jump chips
        if (pagesList.size > 1) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(pagesList.size) { idx ->
                    val pNum = pagesList[idx].first
                    val isSelected = pagerState.currentPage == idx
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(idx) }
                        },
                        label = { Text("ص ${toArabicDigits(pNum)}", fontSize = 12.sp) }
                    )
                }
            }
        }

        // Horizontal Pager representing each Page in the Medina Mushaf
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            if (pageIndex in pagesList.indices) {
                val (pageNum, pageAyahs) = pagesList[pageIndex]

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.5.dp, QuranGoldPrimary.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                    ) {
                        // Top Header inside Mushaf Page
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الجزء ${toArabicDigits(surah?.juzNumber ?: 1)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = QuranGoldPrimary
                            )
                            Text(
                                text = "سُورَةُ ${surah?.nameArabic ?: ""}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = IslamicEmeraldPrimary
                            )
                            Text(
                                text = "ص ${toArabicDigits(pageNum)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = QuranGoldPrimary
                            )
                        }

                        // Decorative gold rule
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            QuranGoldPrimary.copy(alpha = 0.7f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Scrollable Page Body
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // If this is the start page of the surah
                            if (pageNum == surah?.startPage) {
                                SurahOrnamentTitleHeader(surah = surah)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (surah.number != 9) {
                                    BismillahHeader()
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            // Flow of Holy Verses
                            val annotatedString = buildAnnotatedString {
                                pageAyahs.forEach { a ->
                                    val isPlayingThisAyah = audioState.surahNumber == a.surahNumber &&
                                            audioState.ayahNumber == a.ayahNumber &&
                                            (audioState.status == AudioPlaybackStatus.PLAYING || audioState.status == AudioPlaybackStatus.BUFFERING)
                                    val isSelected = selectedAyahOnPage?.ayahNumber == a.ayahNumber

                                    pushStringAnnotation(tag = "AYAH", annotation = a.ayahNumber.toString())
                                    if (isPlayingThisAyah) {
                                        withStyle(
                                            style = SpanStyle(
                                                background = QuranGoldPrimary.copy(alpha = 0.35f),
                                                fontWeight = FontWeight.Bold,
                                                color = IslamicEmeraldPrimary
                                            )
                                        ) {
                                            append(a.textArabic)
                                        }
                                    } else if (isSelected) {
                                        withStyle(
                                            style = SpanStyle(
                                                background = IslamicEmeraldPrimary.copy(alpha = 0.18f),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        ) {
                                            append(a.textArabic)
                                        }
                                    } else {
                                        append(a.textArabic)
                                    }
                                    pop()

                                    append(" ")
                                    withStyle(
                                        style = SpanStyle(
                                            color = QuranGoldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = (arabicFontSize * 0.85f).sp
                                        )
                                    ) {
                                        append("﴿${toArabicDigits(a.ayahNumber)}﴾")
                                    }
                                    append("  ")
                                }
                            }

                            ClickableText(
                                text = annotatedString,
                                style = TextStyle(
                                    fontFamily = FontFamily.Default,
                                    fontSize = arabicFontSize.sp,
                                    lineHeight = (arabicFontSize * 1.9f).sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                onClick = { offset ->
                                    annotatedString.getStringAnnotations(tag = "AYAH", start = offset, end = offset)
                                        .firstOrNull()?.let { annotation ->
                                            val aNum = annotation.item.toIntOrNull()
                                            selectedAyahOnPage = pageAyahs.find { it.ayahNumber == aNum }
                                        }
                                }
                            )
                        }

                        // Page Footer with Ornate Page Number
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "۩   ${toArabicDigits(pageNum)}   ۩",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = QuranGoldPrimary
                            )
                        }
                    }
                }
            }
        }

        // Selected Ayah Action Bar on Page
        val activeAyah = selectedAyahOnPage ?: if (audioState.surahNumber == (surah?.number ?: 0) && audioState.status != AudioPlaybackStatus.IDLE) {
            pagesList.getOrNull(pagerState.currentPage)?.second?.find { it.ayahNumber == audioState.ayahNumber }
        } else null

        if (activeAyah != null) {
            val isBookmarked = bookmarks.any {
                it.surahNumber == activeAyah.surahNumber && it.ayahNumber == activeAyah.ayahNumber
            }
            val isPlaying = audioState.surahNumber == activeAyah.surahNumber &&
                    audioState.ayahNumber == activeAyah.ayahNumber &&
                    audioState.status == AudioPlaybackStatus.PLAYING

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "آية ﴿${toArabicDigits(activeAyah.ayahNumber)}﴾ • سورة ${surah?.nameArabic ?: ""}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = IslamicEmeraldPrimary
                        )
                        Text(
                            text = "ص ${toArabicDigits(activeAyah.page)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = QuranGoldPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onPlayAyahAudio(activeAyah.surahNumber, activeAyah.ayahNumber) }) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "استماع",
                                tint = IslamicEmeraldPrimary
                            )
                        }
                        IconButton(onClick = { onTafsir(activeAyah) }) {
                            Icon(Icons.Default.MenuBook, contentDescription = "تفسير")
                        }
                        IconButton(onClick = { onToggleBookmark(activeAyah) }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "مفضلة",
                                tint = if (isBookmarked) QuranGoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onWordAnalysis(activeAyah) }) {
                            Icon(Icons.Default.Info, contentDescription = "إعراب")
                        }
                        IconButton(onClick = { onAddNote(activeAyah) }) {
                            Icon(Icons.Default.NoteAdd, contentDescription = "تدبر")
                        }
                        IconButton(onClick = { onShare(activeAyah) }) {
                            Icon(Icons.Default.Share, contentDescription = "مشاركة")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurahOrnamentTitleHeader(surah: Surah) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = IslamicEmeraldPrimary.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, QuranGoldPrimary.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "سُورَةُ ${surah.nameArabic}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = IslamicEmeraldPrimary,
                    fontSize = 18.sp
                )
                Text(
                    text = "${if (surah.revelationType == RevelationType.MAKKI) "مكية" else "مدنية"} • عدد آياتها: ${toArabicDigits(surah.versesCount)} • ترتيبها: ${toArabicDigits(surah.number)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PageSeparatorHeader(
    pageNumber: Int,
    juzNumber: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(QuranGoldPrimary.copy(alpha = 0.4f))
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = QuranGoldPrimary.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, QuranGoldPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = IslamicEmeraldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "صفحة ${toArabicDigits(pageNumber)} • الجزء ${toArabicDigits(juzNumber)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = IslamicEmeraldPrimary
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(QuranGoldPrimary.copy(alpha = 0.4f))
        )
    }
}

@Composable
fun BismillahHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, QuranGoldPrimary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = QuranGoldPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AyahCardItem(
    ayah: Ayah,
    surahName: String,
    isAudioActive: Boolean,
    isPlaying: Boolean,
    isBuffering: Boolean,
    isBookmarked: Boolean,
    arabicFontSize: Float,
    onPlayAudio: () -> Unit,
    onToggleBookmark: () -> Unit,
    onTafsir: () -> Unit,
    onWordAnalysis: () -> Unit,
    onAddNote: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .border(
                width = if (isAudioActive) 2.5.dp else 0.5.dp,
                color = if (isAudioActive) QuranGoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("ayah_card_${ayah.ayahNumber}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isAudioActive) {
                IslamicEmeraldPrimary.copy(alpha = 0.10f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isAudioActive) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row with Ayah Medallion and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ayah Medallion and Active Audio Badge + Page chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isAudioActive) QuranGoldPrimary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${ayah.ayahNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isAudioActive) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (ayah.page > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = QuranGoldPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                text = "ص ${toArabicDigits(ayah.page)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = QuranGoldPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (isAudioActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isPlaying) IslamicEmeraldPrimary.copy(alpha = 0.15f) else QuranGoldPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isPlaying) "جاري الاستماع 🎧" else if (isBuffering) "جاري التحميل..." else "متوقف مؤقتاً ⏸️",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isPlaying) IslamicEmeraldPrimary else QuranGoldPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Action icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPlayAudio, modifier = Modifier.size(36.dp)) {
                        if (isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = QuranGoldPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "إيقاف مؤقت" else "استماع",
                                tint = if (isAudioActive) QuranGoldPrimary else IslamicEmeraldPrimary
                            )
                        }
                    }

                    IconButton(onClick = onToggleBookmark, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "حفظ في المفضلة",
                            tint = if (isBookmarked) QuranGoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onTafsir, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "تفسير",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onWordAnalysis, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "إعراب ومفردات",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onAddNote, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.NoteAdd,
                            contentDescription = "تدبر وخاطرة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Arabic text with diacritics
            Text(
                text = "${ayah.textArabic} ﴿${toArabicDigits(ayah.ayahNumber)}﴾",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = arabicFontSize.sp,
                lineHeight = (arabicFontSize * 1.8f).sp,
                textAlign = TextAlign.Right,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AudioBottomBar(
    audioState: AudioPlayerUiState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSpeedChange: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = audioState.qari.nameArabic,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = IslamicEmeraldPrimary
                )
                val currentPlayingSurah = remember(audioState.surahNumber) {
                    com.mosalah.quran.data.quran.QuranDataProvider.surahs.find { it.number == audioState.surahNumber }
                }
                Text(
                    text = "سورة ${currentPlayingSurah?.nameArabic ?: audioState.surahNumber} • الآية ${toArabicDigits(audioState.ayahNumber)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Speed Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clickable { onSpeedChange() }
                ) {
                    Text(
                        text = "${audioState.playbackSpeed}x",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(onClick = onPrev) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "السابق")
                }

                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(IslamicEmeraldPrimary)
                ) {
                    if (audioState.status == AudioPlaybackStatus.BUFFERING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (audioState.status == AudioPlaybackStatus.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "تشغيل/إيقاف",
                            tint = Color.White
                        )
                    }
                }

                IconButton(onClick = onNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = "التالي")
                }
            }
        }
    }
}
