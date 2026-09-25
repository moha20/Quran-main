package com.mosalah.quran.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosalah.quran.service.prayer.PrayerTimeCalculator
import com.mosalah.quran.ui.theme.IslamicEmeraldDark
import com.mosalah.quran.ui.theme.IslamicEmeraldPrimary
import com.mosalah.quran.ui.theme.QuranGoldLight
import com.mosalah.quran.ui.theme.QuranGoldPrimary
import com.mosalah.quran.ui.viewmodel.QuranViewModel

@Composable
fun PrayerQiblaScreen(
    viewModel: QuranViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val todayPrayerLog by viewModel.todayPrayerLog.collectAsState()
    val isUpdatingPrayerTimes by viewModel.isUpdatingPrayerTimes.collectAsState()
    var showCityDropdown by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("prayer_qibla_screen"),
        contentPadding = PaddingValues(16.dp, bottom = 90.dp)
    ) {
        // Header & City selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مواقيت الصلاة والقبلة",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = IslamicEmeraldPrimary
                    )
                    Text(
                        text = uiState.hijriDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // City dropdown trigger
                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { showCityDropdown = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = IslamicEmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = uiState.selectedCity.nameArabic,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showCityDropdown,
                        onDismissRequest = { showCityDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.MyLocation,
                                        contentDescription = null,
                                        tint = IslamicEmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "📍 موقعي الحالي (تلقائي عبر الإنترنت)",
                                        fontWeight = FontWeight.Bold,
                                        color = IslamicEmeraldPrimary
                                    )
                                }
                            },
                            onClick = {
                                viewModel.selectAutoLocation()
                                showCityDropdown = false
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        PrayerTimeCalculator.cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city.nameArabic) },
                                onClick = {
                                    viewModel.selectCity(city)
                                    showCityDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Online update status & refresh button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (uiState.prayerTimes.isFromInternet) {
                        IslamicEmeraldPrimary.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isUpdatingPrayerTimes) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = IslamicEmeraldPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "جارِ التحديث عبر الإنترنت...",
                                style = MaterialTheme.typography.labelSmall,
                                color = IslamicEmeraldPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        } else if (uiState.prayerTimes.isFromInternet) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = IslamicEmeraldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val updateText = if (uiState.prayerTimes.lastUpdatedTime.isNotBlank()) {
                                "محدث عبر الإنترنت (${uiState.prayerTimes.lastUpdatedTime})"
                            } else {
                                "محدث تلقائياً عبر الإنترنت"
                            }
                            Text(
                                text = updateText,
                                style = MaterialTheme.typography.labelSmall,
                                color = IslamicEmeraldPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "حساب محلي تقديري (غير متصل)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { viewModel.refreshPrayerTimesManual() },
                    enabled = !isUpdatingPrayerTimes,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "تحديث المواقيت",
                        tint = IslamicEmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Hero Next Prayer Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(IslamicEmeraldDark, IslamicEmeraldPrimary)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "الصلاة القادمة",
                                style = MaterialTheme.typography.labelMedium,
                                color = QuranGoldLight.copy(alpha = 0.9f)
                            )
                            Text(
                                text = uiState.prayerTimes.nextPrayerName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Mosque, contentDescription = null, tint = QuranGoldLight, modifier = Modifier.size(28.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "الوقت المتبقي: ${uiState.prayerTimes.nextPrayerRemainingMinutes} دقيقة تقريباً",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.95f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tabs: Prayer Times vs Qibla Compass
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = IslamicEmeraldPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("مواقيت اليوم") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("بوصلة القبلة 🧭") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("محاسبة الصلوات") }
                )
            }
        }

        if (selectedTab == 0) {
            val ptList = listOf(
                Pair("الفجر", uiState.prayerTimes.fajr),
                Pair("الشروق", uiState.prayerTimes.sunrise),
                Pair("الظهر", uiState.prayerTimes.dhuhr),
                Pair("العصر", uiState.prayerTimes.asr),
                Pair("المغرب", uiState.prayerTimes.maghrib),
                Pair("العشاء", uiState.prayerTimes.isha)
            )

            items(ptList.size) { index ->
                val prayer = ptList[index]
                val isNext = uiState.prayerTimes.nextPrayerName.contains(prayer.first.split(" ")[0])
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNext) IslamicEmeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isNext) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(QuranGoldPrimary)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (isNext) QuranGoldPrimary else IslamicEmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = prayer.first,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                                color = if (isNext) IslamicEmeraldPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = prayer.second,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isNext) QuranGoldPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        } else if (selectedTab == 1) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "اتجاه القبلة الشريفة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IslamicEmeraldPrimary
                        )
                        Text(
                            text = "زاوية مكة المكرمة: ${uiState.qiblaAngle.toInt()}° درجة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(2.dp, QuranGoldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ش",
                                fontWeight = FontWeight.Bold,
                                color = Color.Red,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 8.dp)
                            )

                            Canvas(
                                modifier = Modifier
                                    .size(200.dp)
                                    .rotate(uiState.qiblaAngle.toFloat())
                            ) {
                                val center = Offset(size.width / 2, size.height / 2)
                                drawLine(
                                    color = Color(0xFF0F5A3E),
                                    start = center,
                                    end = Offset(size.width / 2, 10f),
                                    strokeWidth = 6f,
                                    cap = StrokeCap.Round
                                )
                                drawCircle(
                                    color = Color(0xFFD4AF37),
                                    radius = 12f,
                                    center = Offset(size.width / 2, 20f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(IslamicEmeraldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mosque,
                                    contentDescription = "الكعبة",
                                    tint = QuranGoldLight,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "وجّه هاتفك بحيث يستقر المؤشر الذهبي نحو الأعلى باتجاه البيت الحرام بمكة المكرمة.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "سجل متابعة الصلوات (حاسب نفسك)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IslamicEmeraldPrimary
                        )
                        Text(
                            text = "«حَاسِبُوا أَنْفُسَكُمْ قَبْلَ أَنْ تُحَاسَبُوا» - تتبع أداء الصلوات في وقتها",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val prayerItems = listOf(
                            Triple("fajr", "صلاة الفجر", todayPrayerLog?.fajr == true),
                            Triple("dhuhr", "صلاة الظهر", todayPrayerLog?.dhuhr == true),
                            Triple("asr", "صلاة العصر", todayPrayerLog?.asr == true),
                            Triple("maghrib", "صلاة المغرب", todayPrayerLog?.maghrib == true),
                            Triple("isha", "صلاة العشاء", todayPrayerLog?.isha == true),
                            Triple("qiyam", "قيام الليل والوتر", todayPrayerLog?.qiyam == true)
                        )

                        prayerItems.forEach { (id, name, checked) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (checked) IslamicEmeraldPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .clickable { viewModel.togglePrayerCompleted(id) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (checked) IslamicEmeraldPrimary else Color.Transparent)
                                            .border(
                                                2.dp,
                                                if (checked) IslamicEmeraldPrimary else MaterialTheme.colorScheme.outline,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (checked) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                if (checked) {
                                    Text(
                                        text = "أُدِّيَتْ بحمد الله",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = IslamicEmeraldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
