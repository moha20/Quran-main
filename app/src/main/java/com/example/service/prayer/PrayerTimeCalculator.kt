package com.example.service.prayer

import java.util.Calendar
import java.util.Date
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

data class PrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val nextPrayerName: String,
    val nextPrayerRemainingMinutes: Long,
    val locationName: String,
    val isFromInternet: Boolean = false,
    val lastUpdatedTime: String = ""
)

data class CityPreset(
    val nameArabic: String,
    val nameEnglish: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: Double
)

object PrayerTimeCalculator {

    val cities = listOf(
        CityPreset("مكة المكرمة", "Makkah", 21.4225, 39.8262, 3.0),
        CityPreset("المدينة المنورة", "Madinah", 24.5247, 39.5692, 3.0),
        CityPreset("الرياض", "Riyadh", 24.7136, 46.6753, 3.0),
        CityPreset("القاهرة", "Cairo", 30.0444, 31.2357, 2.0),
        CityPreset("القدس الشريف", "Jerusalem", 31.7683, 35.2137, 2.0),
        CityPreset("إسطنبول", "Istanbul", 41.0082, 28.9784, 3.0),
        CityPreset("لندن", "London", 51.5074, -0.1278, 0.0),
        CityPreset("باريس", "Paris", 48.8566, 2.3522, 1.0),
        CityPreset("نيويورك", "New York", 40.7128, -74.0060, -5.0),
        CityPreset("كوالالمبور", "Kuala Lumpur", 3.1390, 101.6869, 8.0),
        CityPreset("جاكرتا", "Jakarta", -6.2088, 106.8456, 7.0)
    )

    fun calculateQiblaAngle(latitude: Double, longitude: Double): Double {
        val kaabaLat = Math.toRadians(21.4225)
        val kaabaLon = Math.toRadians(39.8262)
        val userLat = Math.toRadians(latitude)
        val userLon = Math.toRadians(longitude)

        val deltaLon = kaabaLon - userLon
        val y = sin(deltaLon) * cos(kaabaLat)
        val x = cos(userLat) * sin(kaabaLat) - sin(userLat) * cos(kaabaLat) * cos(deltaLon)

        var qibla = Math.toDegrees(atan2(y, x))
        qibla = (qibla + 360.0) % 360.0
        return qibla
    }

    fun calculatePrayerTimes(
        city: CityPreset,
        calendar: Calendar = Calendar.getInstance()
    ): PrayerTimes {
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        // Solar Declination approximation
        val b = 2 * Math.PI * (dayOfYear - 81) / 365.0
        val declination = Math.toRadians(23.45 * sin(b))

        // Equation of Time (in minutes)
        val eot = 9.87 * sin(2 * b) - 7.53 * cos(b) - 1.5 * sin(b)

        val latRad = Math.toRadians(city.latitude)
        val solarNoonUtc = 12.0 - (city.longitude / 15.0) - (eot / 60.0)
        val solarNoonLocal = solarNoonUtc + city.timezone

        // Dhuhr
        val dhuhrHours = solarNoonLocal

        // Sunrise & Sunset Angle (-0.833 degrees for refraction)
        val sunriseSunsetAngle = Math.toRadians(-0.833)
        val cosHourAngleSun = (sin(sunriseSunsetAngle) - sin(latRad) * sin(declination)) / (cos(latRad) * cos(declination))
        val hourAngleSun = Math.toDegrees(acos(cosHourAngleSun.coerceIn(-1.0, 1.0))) / 15.0

        val sunriseHours = dhuhrHours - hourAngleSun
        val maghribHours = dhuhrHours + hourAngleSun

        // Fajr (18.5 degrees below horizon)
        val fajrAngle = Math.toRadians(-18.5)
        val cosHourAngleFajr = (sin(fajrAngle) - sin(latRad) * sin(declination)) / (cos(latRad) * cos(declination))
        val hourAngleFajr = Math.toDegrees(acos(cosHourAngleFajr.coerceIn(-1.0, 1.0))) / 15.0
        val fajrHours = dhuhrHours - hourAngleFajr

        // Isha (17.5 degrees below horizon or 90 min after Maghrib in Makkah)
        val ishaAngle = Math.toRadians(-17.5)
        val cosHourAngleIsha = (sin(ishaAngle) - sin(latRad) * sin(declination)) / (cos(latRad) * cos(declination))
        val hourAngleIsha = Math.toDegrees(acos(cosHourAngleIsha.coerceIn(-1.0, 1.0))) / 15.0
        val ishaHours = dhuhrHours + hourAngleIsha

        // Asr (Shafi'i: shadow = 1 + shadow at noon)
        val noonAlt = (Math.PI / 2.0) - Math.abs(latRad - declination)
        val asrAlt = atan2(1.0, 1.0 + tan((Math.PI / 2.0) - noonAlt))
        val cosHourAngleAsr = (sin(asrAlt) - sin(latRad) * sin(declination)) / (cos(latRad) * cos(declination))
        val hourAngleAsr = Math.toDegrees(acos(cosHourAngleAsr.coerceIn(-1.0, 1.0))) / 15.0
        val asrHours = dhuhrHours + hourAngleAsr

        fun formatHours(hours: Double): String {
            val normalized = (hours + 24.0) % 24.0
            val h = floor(normalized).toInt()
            val m = floor((normalized - h) * 60.0).toInt()
            return "%02d:%02d".format(h, m)
        }

        val fTime = formatHours(fajrHours)
        val sTime = formatHours(sunriseHours)
        val dTime = formatHours(dhuhrHours)
        val aTime = formatHours(asrHours)
        val mTime = formatHours(maghribHours)
        val iTime = formatHours(ishaHours)

        val (nextName, remainingMins) = calculateNextPrayer(fTime, sTime, dTime, aTime, mTime, iTime, calendar)

        return PrayerTimes(
            fajr = fTime,
            sunrise = sTime,
            dhuhr = dTime,
            asr = aTime,
            maghrib = mTime,
            isha = iTime,
            nextPrayerName = nextName,
            nextPrayerRemainingMinutes = remainingMins,
            locationName = city.nameArabic,
            isFromInternet = false
        )
    }

    fun calculateNextPrayer(
        fajr: String,
        sunrise: String,
        dhuhr: String,
        asr: String,
        maghrib: String,
        isha: String,
        calendar: Calendar = Calendar.getInstance()
    ): Pair<String, Long> {
        fun toMins(formatted: String): Int {
            val parts = formatted.trim().split(" ")[0].split(":")
            if (parts.size < 2) return 0
            val h = parts[0].toIntOrNull() ?: 0
            val m = parts[1].toIntOrNull() ?: 0
            return h * 60 + m
        }

        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val prayerList = listOf(
            Pair("الفجر", toMins(fajr)),
            Pair("الشروق", toMins(sunrise)),
            Pair("الظهر", toMins(dhuhr)),
            Pair("العصر", toMins(asr)),
            Pair("المغرب", toMins(maghrib)),
            Pair("العشاء", toMins(isha))
        )

        val next = prayerList.firstOrNull { it.second > currentMinutes }
        return if (next != null) {
            Pair(next.first, (next.second - currentMinutes).toLong())
        } else {
            val fajrTomorrow = toMins(fajr) + 24 * 60
            Pair("الفجر", (fajrTomorrow - currentMinutes).toLong())
        }
    }

    fun getHijriDateString(): String {
        // Approximate Hijri date
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val months = listOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان المبارك", "شوال", "ذو القعدة", "ذو الحجة"
        )
        // 2026 approximation
        val hDay = ((day + 12) % 30) + 1
        val hMonthIndex = (month + 7) % 12
        val hYear = 1448
        return "$hDay ${months[hMonthIndex]} $hYear هـ"
    }
}
