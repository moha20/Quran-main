package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CustomZikrEntity
import com.example.data.local.TasbihEntity
import com.example.data.model.DuaItem
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldPrimary
import com.example.ui.theme.QuranGoldLight
import com.example.ui.theme.QuranGoldPrimary
import com.example.ui.viewmodel.QuranViewModel

@Composable
fun DhikrDuasScreen(
    viewModel: QuranViewModel
) {
    val context = LocalContext.current
    val vibrator = remember {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    val tasbihList by viewModel.tasbihList.collectAsState()
    val customAzkarList by viewModel.customAzkar.collectAsState()
    val duaCounts by viewModel.duaCounts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val allDuas = viewModel.repository.getDuas()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedTasbihId by remember { mutableStateOf("subhanallah") }
    var selectedCategoryArabic by remember { mutableStateOf<String?>(null) }

    // Dialog States
    var showAddZikrDialog by remember { mutableStateOf(false) }
    var showAddTasbihDialog by remember { mutableStateOf(false) }
    var zikrToDelete by remember { mutableStateOf<CustomZikrEntity?>(null) }
    var tasbihToDelete by remember { mutableStateOf<TasbihEntity?>(null) }

    val activeTasbih = tasbihList.find { it.id == selectedTasbihId } ?: tasbihList.firstOrNull() ?: TasbihEntity(
        id = "subhanallah",
        arabicPhrase = "سُبْحَانَ اللَّهِ",
        translation = "تنزيه الله وتقديسه عن كل نقص",
        currentCount = 0,
        targetCount = 33,
        totalHistoricalCount = 0
    )

    fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    fun copyToClipboard(text: String, label: String = "الذكر") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "تم نسخ $label إلى الحافظة", Toast.LENGTH_SHORT).show()
    }

    fun shareText(title: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$title\n\n$text\n\n— من تطبيق القرآن الكريم")
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة الذكر"))
    }

    val categories = remember(allDuas) {
        allDuas.map { it.categoryArabic }.distinct()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dhikr_duas_screen"),
        contentPadding = PaddingValues(16.dp, bottom = 96.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الأذكار وحصن المسلم والمسبحة",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = IslamicEmeraldPrimary
                    )
                    Text(
                        text = "«أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ» - مسبحة ذكية وأدعية مأثورة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { showAddZikrDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(IslamicEmeraldPrimary.copy(alpha = 0.12f))
                        .testTag("add_custom_zikr_top_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة ذكر",
                        tint = IslamicEmeraldPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Tabs: Tasbih, Hisn al-Muslim, Custom Azkar, Khatma
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = IslamicEmeraldPrimary,
                edgePadding = 0.dp,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("المسبحة الإلكترونية") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("حصن المسلم (${allDuas.size})") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("أذكاري (${customAzkarList.size})") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("مخطط الختمة") }
                )
            }
        }

        // ================= TAB 0: ELECTRONIC TASBIH =================
        if (selectedTab == 0) {
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasbihList, key = { it.id }) { item ->
                        val isSelected = item.id == activeTasbih.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTasbihId = item.id },
                            label = { Text(item.arabicPhrase) },
                            trailingIcon = if (!item.id.startsWith("subhanallah") &&
                                !item.id.startsWith("alhamdulillah") &&
                                !item.id.startsWith("allahu_akbar") &&
                                !item.id.startsWith("la_ilaha") &&
                                !item.id.startsWith("astaghfirullah") &&
                                !item.id.startsWith("salawat")
                            ) {
                                {
                                    IconButton(
                                        onClick = { tasbihToDelete = item },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف التسبيح",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            } else null
                        )
                    }
                    item {
                        OutlinedButton(
                            onClick = { showAddTasbihDialog = true },
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تسبيح جديد", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Big Interactive Circular Counter
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(28.dp),
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
                            text = activeTasbih.arabicPhrase,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = IslamicEmeraldPrimary,
                            textAlign = TextAlign.Center
                        )
                        val arabicMeaning = when (activeTasbih.arabicPhrase.trim()) {
                            "سُبْحَانَ اللَّهِ" -> "تنزيه الله وتقديسه عن كل نقص"
                            "الْحَمْدُ لِلَّهِ" -> "الثناء والحمد والشكر لله رب العالمين"
                            "اللَّهُ أَكْبَرُ" -> "تعظيم الله وإجلاله فوق كل شيء"
                            "لَا إِلَٰهَ إِلَّا اللَّهُ" -> "شهادة التوحيد والإخلاص لله تعالى"
                            "أَسْتَغْفِرُ اللَّهَ" -> "طلب المغفرة والصفح من الذنوب"
                            "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ" -> "الصلاة والسلام على النبي المختار ﷺ"
                            else -> if (!activeTasbih.translation.any { it in 'a'..'z' || it in 'A'..'Z' }) activeTasbih.translation else ""
                        }
                        if (arabicMeaning.isNotEmpty()) {
                            Text(
                                text = arabicMeaning,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Tappable Bead Circle
                        Box(
                            modifier = Modifier
                                .size(210.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            IslamicEmeraldPrimary,
                                            IslamicEmeraldDark
                                        )
                                    )
                                )
                                .border(5.dp, QuranGoldLight, CircleShape)
                                .clickable {
                                    triggerVibration()
                                    viewModel.incrementTasbih(activeTasbih.id)
                                }
                                .testTag("tasbih_counter_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${activeTasbih.currentCount}",
                                    fontSize = 58.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "الهدف: ${activeTasbih.targetCount}",
                                    fontSize = 15.sp,
                                    color = QuranGoldLight
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "اضغط للتسبيح",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "إجمالي التسبيحات السابقة:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${activeTasbih.totalHistoricalCount} مرة",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = QuranGoldPrimary
                                )
                            }

                            IconButton(
                                onClick = { viewModel.resetTasbih(activeTasbih.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "إعادة ضبط",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // ================= TAB 1: HISN AL-MUSLIM (BUILT-IN AUTHENTIC AZKAR) =================
        else if (selectedTab == 1) {
            // Category filter chips
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryArabic == null,
                            onClick = { selectedCategoryArabic = null },
                            label = { Text("جميع الأذكار (${allDuas.size})") }
                        )
                    }
                    items(categories) { catArabic ->
                        val countInCat = allDuas.count { it.categoryArabic == catArabic }
                        FilterChip(
                            selected = selectedCategoryArabic == catArabic,
                            onClick = { selectedCategoryArabic = catArabic },
                            label = { Text("$catArabic ($countInCat)") }
                        )
                    }
                }
            }

            val filteredDuas = if (selectedCategoryArabic == null) {
                allDuas
            } else {
                allDuas.filter { it.categoryArabic == selectedCategoryArabic }
            }

            // Category progress indicator if specific category selected
            if (selectedCategoryArabic != null) {
                val completedCount = filteredDuas.count {
                    val count = duaCounts[it.id] ?: 0
                    count >= it.targetCount
                }
                val totalInCat = filteredDuas.size
                val catProgress = if (totalInCat > 0) completedCount.toFloat() / totalInCat.toFloat() else 0f

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = IslamicEmeraldPrimary.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "إنجاز $selectedCategoryArabic",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicEmeraldPrimary
                                )
                                Text(
                                    text = "$completedCount من $totalInCat مكتملة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (completedCount == totalInCat && totalInCat > 0) IslamicEmeraldDark else QuranGoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { catProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = IslamicEmeraldPrimary,
                                trackColor = IslamicEmeraldPrimary.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { selectedCategoryArabic?.let { viewModel.resetCategoryDuaCounts(it) } },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("إعادة ضبط أذكار القسم", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Add Custom Zikr Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { showAddZikrDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = IslamicEmeraldPrimary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "إضافة",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "إضافة ذكر أو دعاء جديد",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicEmeraldPrimary
                                )
                                Text(
                                    text = "أضف أورادك الخاصة مع عداد تفاعلي مخصص",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            items(filteredDuas, key = { it.id }) { dua ->
                val currentCount = duaCounts[dua.id] ?: 0
                val isCompleted = currentCount >= dua.targetCount

                AzkarCardItem(
                    title = dua.titleArabic,
                    categoryArabic = dua.categoryArabic,
                    arabicText = dua.arabicText,
                    translation = dua.translation,
                    reference = dua.reference,
                    benefits = dua.benefits,
                    currentCount = currentCount,
                    targetCount = dua.targetCount,
                    isCompleted = isCompleted,
                    onIncrement = {
                        triggerVibration()
                        viewModel.incrementDuaCount(dua.id, dua.targetCount)
                    },
                    onReset = { viewModel.resetDuaCount(dua.id) },
                    onCopy = { copyToClipboard(dua.arabicText, dua.titleArabic) },
                    onShare = { shareText(dua.titleArabic, dua.arabicText) }
                )
            }
        }

        // ================= TAB 2: USER'S CUSTOM AZKAR =================
        else if (selectedTab == 2) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "أذكاري وأدعيتي الخاصة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IslamicEmeraldPrimary
                        )
                        Text(
                            text = "أدعيتك وأورادك الشخصية المحفوظة محلياً",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showAddZikrDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicEmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_custom_zikr_action_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة ذكر", fontSize = 12.sp)
                    }
                }
            }

            if (customAzkarList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("✨", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "لم تقم بإضافة أذكار مخصصة بعد",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "يمكنك إضافة أي أدعية قرآنية، أو أوراد نبوية، أو أذكار شخصية تفضل تكرارها يومياً مع عداد ذكي.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = { showAddZikrDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = IslamicEmeraldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("إضافة أول ذكر مخصص")
                            }
                        }
                    }
                }
            } else {
                items(customAzkarList, key = { it.id }) { customZikr ->
                    val isCompleted = customZikr.currentCount >= customZikr.targetCount

                    AzkarCardItem(
                        title = customZikr.titleArabic,
                        categoryArabic = customZikr.categoryArabic,
                        arabicText = customZikr.arabicText,
                        translation = customZikr.translation,
                        reference = if (customZikr.titleEnglish.isNotEmpty()) customZikr.titleEnglish else "ذكر مخصص",
                        benefits = customZikr.benefits,
                        currentCount = customZikr.currentCount,
                        targetCount = customZikr.targetCount,
                        isCompleted = isCompleted,
                        onIncrement = {
                            triggerVibration()
                            viewModel.incrementCustomZikr(customZikr.id)
                        },
                        onReset = { viewModel.resetCustomZikr(customZikr.id) },
                        onCopy = { copyToClipboard(customZikr.arabicText, customZikr.titleArabic) },
                        onShare = { shareText(customZikr.titleArabic, customZikr.arabicText) },
                        onDelete = { zikrToDelete = customZikr }
                    )
                }
            }
        }

        // ================= TAB 3: KHATMA READING PLANNER =================
        else {
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
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "مخطط ختم القرآن الكريم",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = IslamicEmeraldPrimary
                        )
                        Text(
                            text = "حدد مدة الختمة وتابع تقدمك في صفحات المصحف الشريف (604 صفحات)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("مدة الختمة المستهدفة:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.khatmaPlanDays == 30,
                                onClick = { viewModel.setKhatmaPlanDays(30) },
                                label = { Text("30 يوماً (جزء يومياً)") }
                            )
                            FilterChip(
                                selected = uiState.khatmaPlanDays == 60,
                                onClick = { viewModel.setKhatmaPlanDays(60) },
                                label = { Text("60 يوماً (نصف جزء يومياً)") }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val completionPercent = (uiState.khatmaCurrentPage.toFloat() / 604f) * 100f
                        val pagesPerDay = (604 / uiState.khatmaPlanDays)
                        val remainingDays = ((604 - uiState.khatmaCurrentPage) / pagesPerDay).coerceAtLeast(1)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الصفحة الحالية: ${uiState.khatmaCurrentPage} من 604",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "%.1f%%".format(completionPercent),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = IslamicEmeraldPrimary
                            )
                        }

                        Slider(
                            value = uiState.khatmaCurrentPage.toFloat(),
                            onValueChange = { viewModel.updateKhatmaPage(it.toInt()) },
                            valueRange = 1f..604f,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "الورد اليومي المطلوب: $pagesPerDay صفحة",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "المتبقي لإتمام الختمة: $remainingDays يوماً",
                                style = MaterialTheme.typography.bodySmall,
                                color = QuranGoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // ================= ADD CUSTOM ZIKR DIALOG =================
    if (showAddZikrDialog) {
        var titleArabic by remember { mutableStateOf("") }
        var titleEnglish by remember { mutableStateOf("") }
        var arabicText by remember { mutableStateOf("") }
        var translation by remember { mutableStateOf("") }
        var benefits by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("أذكار الصباح") }
        var targetCount by remember { mutableIntStateOf(3) }
        var alsoAddToTasbih by remember { mutableStateOf(false) }

        val categoryOptions = listOf(
            "أذكار الصباح",
            "أذكار المساء",
            "أذكار بعد الصلاة",
            "أدعية النوم والاستيقاظ",
            "أدعية تفريج الهم والكرب",
            "أدعية الشفاء والرقية",
            "أذكار مخصصة"
        )

        AlertDialog(
            onDismissRequest = { showAddZikrDialog = false },
            title = {
                Text(
                    text = "إضافة ذكر أو دعاء جديد",
                    fontWeight = FontWeight.Bold,
                    color = IslamicEmeraldPrimary
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = titleArabic,
                            onValueChange = { titleArabic = it },
                            label = { Text("عنوان الذكر أو الدعاء *") },
                            placeholder = { Text("مثال: دعاء تيسير الأمور") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_zikr_title_input"),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = arabicText,
                            onValueChange = { arabicText = it },
                            label = { Text("نص الذكر بالعربية *") },
                            placeholder = { Text("اكتب نص الذكر أو الدعاء المبارك هنا...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_zikr_text_input"),
                            minLines = 3,
                            maxLines = 6
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = translation,
                            onValueChange = { translation = it },
                            label = { Text("الترجمة أو المعنى (اختياري)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = benefits,
                            onValueChange = { benefits = it },
                            label = { Text("فضل الذكر أو المصدر (اختياري)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
                    }

                    item {
                        Text("التصنيف:", style = MaterialTheme.typography.labelMedium)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(categoryOptions) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    item {
                        Text("عدد مرات التكرار المستهدفة:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            listOf(1, 3, 7, 33, 100).forEach { countPreset ->
                                FilterChip(
                                    selected = targetCount == countPreset,
                                    onClick = { targetCount = countPreset },
                                    label = { Text("$countPreset") }
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { alsoAddToTasbih = !alsoAddToTasbih },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = alsoAddToTasbih,
                                onCheckedChange = { alsoAddToTasbih = it }
                            )
                            Text(
                                text = "إضافة الذكر إلى المسبحة الإلكترونية أيضاً",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleArabic.isNotBlank() && arabicText.isNotBlank()) {
                            viewModel.addCustomZikr(
                                titleArabic = titleArabic.trim(),
                                titleEnglish = titleEnglish.trim(),
                                categoryArabic = selectedCategory,
                                arabicText = arabicText.trim(),
                                translation = translation.trim(),
                                targetCount = targetCount,
                                benefits = benefits.trim()
                            )
                            if (alsoAddToTasbih) {
                                viewModel.addTasbih(
                                    phrase = arabicText.trim(),
                                    translation = if (translation.isNotBlank()) translation.trim() else titleArabic.trim(),
                                    targetCount = if (targetCount > 1) targetCount else 33
                                )
                            }
                            Toast.makeText(context, "تمت إضافة الذكر بنجاح ✨", Toast.LENGTH_SHORT).show()
                            showAddZikrDialog = false
                        } else {
                            Toast.makeText(context, "يرجى كتابة عنوان الذكر ونصه بالعربية", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmeraldPrimary),
                    modifier = Modifier.testTag("confirm_add_zikr_button")
                ) {
                    Text("حفظ الذكر")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddZikrDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // ================= ADD TASBIH DIALOG =================
    if (showAddTasbihDialog) {
        var tasbihPhrase by remember { mutableStateOf("") }
        var tasbihTrans by remember { mutableStateOf("") }
        var tasbihTarget by remember { mutableIntStateOf(33) }

        AlertDialog(
            onDismissRequest = { showAddTasbihDialog = false },
            title = {
                Text("إضافة تسبيح للمسبحة", fontWeight = FontWeight.Bold, color = IslamicEmeraldPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tasbihPhrase,
                        onValueChange = { tasbihPhrase = it },
                        label = { Text("صيغة التسبيح بالعربية *") },
                        placeholder = { Text("مثال: لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tasbihTrans,
                        onValueChange = { tasbihTrans = it },
                        label = { Text("الترجمة أو المعنى (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("الهدف:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(33, 100, 1000).forEach { count ->
                            FilterChip(
                                selected = tasbihTarget == count,
                                onClick = { tasbihTarget = count },
                                label = { Text("$count مرة") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tasbihPhrase.isNotBlank()) {
                            viewModel.addTasbih(
                                phrase = tasbihPhrase.trim(),
                                translation = tasbihTrans.trim(),
                                targetCount = tasbihTarget
                            )
                            Toast.makeText(context, "تمت إضافة التسبيح للمسبحة", Toast.LENGTH_SHORT).show()
                            showAddTasbihDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmeraldPrimary)
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTasbihDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // ================= DELETE CONFIRMATION DIALOGS =================
    zikrToDelete?.let { customZikr ->
        AlertDialog(
            onDismissRequest = { zikrToDelete = null },
            title = { Text("حذف الذكر المخصص") },
            text = { Text("هل أنت متأكد من حذف «${customZikr.titleArabic}»؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomZikr(customZikr)
                        zikrToDelete = null
                        Toast.makeText(context, "تم حذف الذكر", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { zikrToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    tasbihToDelete?.let { tasbih ->
        AlertDialog(
            onDismissRequest = { tasbihToDelete = null },
            title = { Text("حذف التسبيح") },
            text = { Text("هل تريد إزالة «${tasbih.arabicPhrase}» من المسبحة؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTasbih(tasbih)
                        tasbihToDelete = null
                        Toast.makeText(context, "تم حذف التسبيح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { tasbihToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun AzkarCardItem(
    title: String,
    categoryArabic: String,
    arabicText: String,
    translation: String,
    reference: String,
    benefits: String,
    currentCount: Int,
    targetCount: Int,
    isCompleted: Boolean,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val cardBorderColor by animateColorAsState(
        targetValue = if (isCompleted) IslamicEmeraldPrimary.copy(alpha = 0.6f) else Color.Transparent,
        label = "cardBorderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(1.5.dp, cardBorderColor, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) IslamicEmeraldPrimary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Title + Category & Target Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IslamicEmeraldPrimary
                    )
                    Text(
                        text = categoryArabic,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCompleted) IslamicEmeraldPrimary.copy(alpha = 0.15f) else QuranGoldPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "التكرار: $targetCount مرات",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted) IslamicEmeraldPrimary else QuranGoldPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Arabic Text
            Text(
                text = arabicText,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 18.sp,
                lineHeight = 30.sp,
                textAlign = TextAlign.Right,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            if (translation.isNotEmpty() && !translation.any { it in 'a'..'z' || it in 'A'..'Z' }) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = translation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (benefits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "فضل الذكر: $benefits",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (reference.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "المصدر: $reference",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions & Interactive Counter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary action buttons: Copy, Share, Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (currentCount > 0) {
                        IconButton(
                            onClick = onReset,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "إعادة ضبط",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Interactive Counter Pill Button
                Button(
                    onClick = onIncrement,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) IslamicEmeraldPrimary else QuranGoldPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تم الإكمال ($currentCount/$targetCount)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text(
                            text = "اقرأ: $currentCount / $targetCount",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
