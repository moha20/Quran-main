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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosalah.quran.ui.theme.IslamicEmeraldDark
import com.mosalah.quran.ui.theme.IslamicEmeraldPrimary
import com.mosalah.quran.ui.theme.QuranGoldLight
import com.mosalah.quran.ui.theme.QuranGoldPrimary
import com.mosalah.quran.ui.viewmodel.QuranViewModel

@Composable
fun MemorizationScreen(
    viewModel: QuranViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val memorizationList by viewModel.memorizationList.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val memorizedCount = memorizationList.count { it.status == "MEMORIZED" }
    val reviewCount = memorizationList.count { it.status == "NEEDS_REVIEW" }

    // Sample flashcard items from Juz Amma
    val flashcards = remember {
        listOf(
            FlashcardData(1, 1, "الفاتحة", "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "أي: أبدأ قراءتي مستعيناً باسم الله الرحمن الرحيم"),
            FlashcardData(1, 2, "الفاتحة", "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "أي: الثناء والشكر لله وحده مالك كل المخلوقات"),
            FlashcardData(1, 7, "الفاتحة", "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "أي: طريق النبيين والصديقين والشهداء والصالحين"),
            FlashcardData(112, 1, "الإخلاص", "قُلْ هُوَ اللَّهُ أَحَدٌ", "أي: قل يا محمد هو الله الواحد الأحد الذي لا شريك له"),
            FlashcardData(112, 2, "الإخلاص", "اللَّهُ الصَّمَدُ", "أي: المقصود في الحوائج كلها الغني عن جميع خلقه"),
            FlashcardData(113, 1, "الفلق", "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "أي: قل ألتجئ وأعتصم برب الصبح وفالق الإصباح"),
            FlashcardData(114, 1, "الناس", "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "أي: قل ألتجئ وأعتصم برب الخلق ومالكهم وإلههم")
        )
    }

    val currentCard = flashcards[uiState.memorizationFlashcardIndex % flashcards.size]

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("memorization_screen"),
        contentPadding = PaddingValues(16.dp, bottom = 90.dp)
    ) {
        item {
            Text(
                text = "حفظ وتثبيت القرآن (الحفظ التفاعلي)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = IslamicEmeraldPrimary
            )
            Text(
                text = "نظام التكرار المتباعد الذكي لتثبيت الآيات والسور مع بطاقات المراجعة",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Stats Dashboard
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
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("الهدف اليومي: 5 آيات", style = MaterialTheme.typography.labelMedium, color = QuranGoldPrimary)
                            Text("إجمالي الآيات المحفوظة: $memorizedCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(IslamicEmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { (memorizedCount.toFloat() / 20f).coerceIn(0.05f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = IslamicEmeraldPrimary,
                    )
                }
            }
        }

        // Tabs: Flashcards vs Tajweed Guide
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
                    text = { Text("بطاقات الحفظ الذكية") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("أحكام التجويد") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("الأوسمة والإنجازات") }
                )
            }
        }

        if (selectedTab == 0) {
            // Flashcard UI
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(1.dp, QuranGoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = IslamicEmeraldPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "سورة ${currentCard.surahName} - الآية ${currentCard.ayahNumber}",
                                style = MaterialTheme.typography.labelMedium,
                                color = IslamicEmeraldPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (!uiState.flashcardRevealed) {
                            Text(
                                text = "استحضر الآية الكريمة في قلبك واتلها غيباً...",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { viewModel.toggleFlashcardReveal() },
                                colors = ButtonDefaults.buttonColors(containerColor = IslamicEmeraldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("كشف الآية والتحقق")
                            }
                        } else {
                            Text(
                                text = currentCard.textArabic,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicEmeraldPrimary,
                                lineHeight = 36.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = currentCard.translation,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.recordFlashcardReview(currentCard.surahNumber, currentCard.ayahNumber, false)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تحتاج مراجعة", color = MaterialTheme.colorScheme.error)
                                }

                                Button(
                                    onClick = {
                                        viewModel.recordFlashcardReview(currentCard.surahNumber, currentCard.ayahNumber, true)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmeraldPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("أتقنتها بفضل الله")
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // Tajweed guide
            val rules = listOf(
                Pair("القلقلة (Qalqalah)", "اضطراب الصوت عند النطق بالحرف الساكن حتى يسمع له نبرة قوية. حروفها: (ق، ط، ب، ج، د) مجمعة في 'قطب جد'."),
                Pair("الغنة (Ghunnah)", "صوت رخيم يخرج من الخيشوم ملازم لحرفي النون والميم المشددتين بمقدار حركتين."),
                Pair("الإدغام (Idgham)", "إدخال حرف ساكن في حرف متحرك بحيث يصيران حرفاً واحداً مشدداً. حروفه: (ي، ر، م، ل، و، ن) في كلمة 'يرملون'."),
                Pair("الإخفاء الحقيقي (Ikhfa)", "النطق بالنون الساكنة أو التنوين بصفة بين الإظهار والإدغام مع بقاء الغنة. حروفه 15 حرفاً في أوائل كلمات بيت: صف ذا ثنا كم جاد شخص قد سما..."),
                Pair("أحكام المدود (Madd)", "المد الطبيعي (حركتان)، المد المتصل والمنفصل (4 أو 5 حركات)، والمد اللازم (6 حركات).")
            )

            items(rules) { rule ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(QuranGoldPrimary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = rule.first,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = IslamicEmeraldPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = rule.second,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        } else {
            // Badges & Achievements
            val badges = listOf(
                Triple("حافظ الفاتحة", "إتمام حفظ وتثبيت سورة الفاتحة السبع المثاني", true),
                Triple("رفيق جزء عم", "إتقان السور القصار من سورة النبأ حتى الناس", memorizedCount >= 5),
                Triple("متقن التجويد", "التعرف على مخارج الحروف وأحكام التلاوة الأساسية", true),
                Triple("السائر في النور", "مواظبة على القراءة اليومية لـ 7 أيام متتالية", true),
                Triple("حافظ سورة الكهف", "حفظ وتدبر الآيات العشر الأولى والأخيرة", false)
            )

            items(badges) { badge ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (badge.third) QuranGoldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (badge.third) QuranGoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (badge.third) Icons.Default.EmojiEvents else Icons.Default.Star,
                                contentDescription = null,
                                tint = if (badge.third) Color.Black else Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = badge.first,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (badge.third) IslamicEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = badge.second,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (badge.third) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = IslamicEmeraldPrimary
                            ) {
                                Text(
                                    text = "مكتمل ✓",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class FlashcardData(
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val textArabic: String,
    val translation: String
)
