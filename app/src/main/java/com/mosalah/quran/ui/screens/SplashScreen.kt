package com.mosalah.quran.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosalah.quran.ui.theme.IslamicEmeraldDark
import com.mosalah.quran.ui.theme.IslamicEmeraldLight
import com.mosalah.quran.ui.theme.IslamicEmeraldMedium
import com.mosalah.quran.ui.theme.IslamicEmeraldPrimary
import com.mosalah.quran.ui.theme.NightBgDark
import com.mosalah.quran.ui.theme.QuranGoldDark
import com.mosalah.quran.ui.theme.QuranGoldLight
import com.mosalah.quran.ui.theme.QuranGoldPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    durationMillis: Long = 3000L,
    onTimeout: () -> Unit
) {
    // Animations for emblem scale and fade
    val emblemScale = remember { Animatable(0.75f) }
    val contentAlpha = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }

    // Breathing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "halo_glow")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_scale"
    )

    LaunchedEffect(Unit) {
        // Entrance animation
        launch {
            emblemScale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )
        }
        launch {
            contentAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 600)
            )
        }

        // Progress bar smooth fill over the exact duration
        launch {
            progress.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(
                    durationMillis = durationMillis.toInt(),
                    easing = LinearEasing
                )
            )
        }

        // Wait for exactly 3 seconds (or specified duration)
        delay(durationMillis)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen")
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        IslamicEmeraldDark,
                        Color(0xFF072418),
                        NightBgDark
                    )
                )
            )
            .clickable { onTimeout() } // Allow skip on tap
    ) {
        // Decorative corner borders / framing
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(24.dp)
                )
        )

        // Skip button at top
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 24.dp)
                .clickable { onTimeout() },
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, QuranGoldPrimary.copy(alpha = 0.3f))
        ) {
            Text(
                text = "تخطي",
                style = MaterialTheme.typography.labelSmall,
                color = QuranGoldLight.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        // Center Branding & Calligraphy
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 28.dp)
                .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Emblem with glowing halo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.scale(emblemScale.value)
            ) {
                // Soft golden radial halo
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(haloPulse)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    QuranGoldPrimary.copy(alpha = 0.35f),
                                    IslamicEmeraldMedium.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Outer Gold Ring
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(2.dp, QuranGoldPrimary.copy(alpha = 0.8f)),
                    modifier = Modifier.size(112.dp)
                ) {}

                // Middle Emerald Badge
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    IslamicEmeraldPrimary,
                                    IslamicEmeraldMedium
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = "المصحف الشريف",
                        tint = QuranGoldLight,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Bismillah Calligraphy
            Text(
                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 21.sp,
                color = QuranGoldPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // App Name
            Text(
                text = "قُـرْآنٌ وَذِكْـر",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "المصحف الشريف • أذكار المسلم • مواقيت الصلاة • تلاوات",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 13.sp,
                color = QuranGoldLight.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Ayah Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, QuranGoldPrimary.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFDE68A),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "سورة الرعد • الآية ٢٨",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = IslamicEmeraldLight.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Bottom Progress & Dedication
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 36.dp)
                .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant smooth gold progress indicator
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = QuranGoldPrimary,
                trackColor = Color.White.copy(alpha = 0.12f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mosque,
                    contentDescription = null,
                    tint = IslamicEmeraldLight.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "صدقة جارية • تقبل الله منا ومنكم",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
