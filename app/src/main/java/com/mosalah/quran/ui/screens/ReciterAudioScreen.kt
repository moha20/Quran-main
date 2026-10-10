package com.mosalah.quran.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosalah.quran.data.model.Qari
import com.mosalah.quran.service.audio.AudioPlaybackStatus
import com.mosalah.quran.ui.theme.IslamicEmeraldPrimary
import com.mosalah.quran.ui.theme.QuranGoldPrimary
import com.mosalah.quran.ui.viewmodel.QuranViewModel

@Composable
fun ReciterAudioScreen(
    viewModel: QuranViewModel,
    onOpenSurah: (surahNumber: Int, ayahNumber: Int) -> Unit
) {
    val audioState by viewModel.audioState.collectAsState()
    val qaris = viewModel.repository.getQaris()
    val surahs = viewModel.getAllSurahs()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf("الكل") }

    val filterChips = listOf(
        "الكل",
        "أشهر القرّاء",
        "مرتل",
        "مجود",
        "المصحف المعلم",
        "رواية ورش",
        "مصر",
        "السعودية",
        "بلدان أخرى"
    )

    val famousIds = remember {
        setOf(
            "alafasy", "abdulbasit", "abdulbasit_mujawwad", "minshawy_murattal", "minshawy_mujawwad",
            "husary", "husary_mujawwad", "sudais", "shuraym", "muaiqly", "dossari", "ghamadi",
            "ajamy", "qatami", "shatri", "hudhaify", "ayyoub", "tablaway", "abbad"
        )
    }

    val filteredQaris = remember(qaris, searchQuery, selectedFilter) {
        qaris.filter { qari ->
            val matchesFilter = when (selectedFilter) {
                "الكل" -> true
                "أشهر القرّاء" -> qari.id in famousIds
                "مرتل" -> qari.style.contains("مرتل")
                "مجود" -> qari.style.contains("مجود")
                "المصحف المعلم" -> qari.style.contains("معلم") || qari.style.contains("المعلم")
                "رواية ورش" -> qari.style.contains("ورش") || qari.id.startsWith("warsh")
                "مصر" -> qari.country.contains("مصر")
                "السعودية" -> qari.country.contains("السعودية")
                "بلدان أخرى" -> !qari.country.contains("مصر") && !qari.country.contains("السعودية")
                else -> true
            }

            val query = searchQuery.trim().lowercase()
            val matchesSearch = query.isEmpty() ||
                qari.nameArabic.lowercase().contains(query) ||
                qari.nameEnglish.lowercase().contains(query) ||
                qari.style.lowercase().contains(query) ||
                qari.country.lowercase().contains(query)

            matchesFilter && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reciter_audio_screen"),
        contentPadding = PaddingValues(16.dp, bottom = 90.dp)
    ) {
        item {
            Text(
                text = "التلاوات وأشهر القرّاء",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = IslamicEmeraldPrimary
            )
            Text(
                text = "استمع للقرآن الكريم بأعذب أصوات مقرئي العالم الإسلامي (${qaris.size} قارئاً ورواية)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Active Player Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(IslamicEmeraldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = IslamicEmeraldPrimary,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = audioState.qari.nameArabic,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${audioState.qari.style} • ${audioState.qari.country}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Open Surah in Mushaf
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = QuranGoldPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.clickable {
                            onOpenSurah(audioState.surahNumber, audioState.ayahNumber)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = QuranGoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "سورة ${surahs.find { it.number == audioState.surahNumber }?.nameArabic ?: ""} • الآية ${audioState.ayahNumber} (فتح بالمصحف)",
                                style = MaterialTheme.typography.bodySmall,
                                color = QuranGoldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Player buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(onClick = { viewModel.audioPlayer.playPreviousAyah() }) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "السابق", modifier = Modifier.size(32.dp))
                        }

                        IconButton(
                            onClick = { viewModel.toggleAudioPlayback() },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(IslamicEmeraldPrimary)
                        ) {
                            if (audioState.status == AudioPlaybackStatus.BUFFERING) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(
                                    imageVector = if (audioState.status == AudioPlaybackStatus.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "تشغيل/إيقاف",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.audioPlayer.playNextAyah() }) {
                            Icon(Icons.Default.SkipNext, contentDescription = "التالي", modifier = Modifier.size(32.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Speed and Repeat settings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Speed Chip
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.clickable {
                                val nextSpeed = when (audioState.playbackSpeed) {
                                    1.0f -> 1.25f
                                    1.25f -> 1.5f
                                    1.5f -> 0.75f
                                    else -> 1.0f
                                }
                                viewModel.setAudioSpeed(nextSpeed)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("سرعة: ${audioState.playbackSpeed}x", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // Repeat Count Chip
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.clickable {
                                val nextRep = when (audioState.repeatCount) {
                                    1 -> 3
                                    3 -> 5
                                    else -> 1
                                }
                                viewModel.setRepeatCount(nextRep)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تكرار: ${audioState.repeatCount} مرات", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(14.dp),
                placeholder = {
                    Text(
                        text = "ابحث بالاسم، الرواية، أو الدولة...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = IslamicEmeraldPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "مسح",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IslamicEmeraldPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }

        // Filter chips row
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterChips) { filter ->
                    val isChipSelected = selectedFilter == filter
                    FilterChip(
                        selected = isChipSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IslamicEmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Section header with counter
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "اختر القارئ المفضل",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = IslamicEmeraldPrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${filteredQaris.size} قارئاً",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = IslamicEmeraldPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Empty state when search/filter returns nothing
        if (filteredQaris.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "لا توجد نتائج مطابقة للبحث",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "جرب البحث باسم آخر أو إزالة التصفية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = IslamicEmeraldPrimary,
                            modifier = Modifier.clickable {
                                searchQuery = ""
                                selectedFilter = "الكل"
                            }
                        ) {
                            Text(
                                text = "عرض كل المقرئين",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Reciters list items
        items(filteredQaris, key = { it.id }) { qari ->
            val isSelected = audioState.qari.id == qari.id
            val isPlayingThis = isSelected && audioState.status == AudioPlaybackStatus.PLAYING
            val isBufferingThis = isSelected && audioState.status == AudioPlaybackStatus.BUFFERING

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.setQari(qari) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) IslamicEmeraldPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                ),
                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = SolidColor(IslamicEmeraldPrimary)) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) IslamicEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPlayingThis) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = qari.nameArabic,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = qari.style,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = IslamicEmeraldPrimary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = qari.country,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = IslamicEmeraldPrimary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Play / State Action Button
                    IconButton(
                        onClick = {
                            if (isSelected) {
                                viewModel.toggleAudioPlayback()
                            } else {
                                viewModel.setQari(qari)
                                viewModel.playAyahAudio(audioState.surahNumber, audioState.ayahNumber)
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) IslamicEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        when {
                            isBufferingThis -> {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            isPlayingThis -> {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = "إيقاف مؤقت",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "تشغيل",
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
