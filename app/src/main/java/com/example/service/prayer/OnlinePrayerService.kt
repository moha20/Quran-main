package com.example.service.prayer

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class OnlinePrayerResult(
    val prayerTimes: PrayerTimes,
    val hijriDate: String,
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val qiblaAngle: Double
)

class OnlinePrayerService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val prefs = context.getSharedPreferences("online_prayer_cache", Context.MODE_PRIVATE)

    suspend fun autoDetectAndFetch(): OnlinePrayerResult? = withContext(Dispatchers.IO) {
        // 1. Try device location if permitted and available
        val deviceLoc = getDeviceLocation()
        if (deviceLoc != null) {
            val result = fetchPrayerTimesForLocation(
                lat = deviceLoc.latitude,
                lon = deviceLoc.longitude,
                locationName = "موقعي الحالي (GPS)"
            )
            if (result != null) return@withContext result
        }

        // 2. Try IP Geolocation over internet
        val ipLoc = getIpLocation()
        if (ipLoc != null) {
            val result = fetchPrayerTimesForLocation(
                lat = ipLoc.lat,
                lon = ipLoc.lon,
                locationName = ipLoc.cityName
            )
            if (result != null) return@withContext result
        }

        // 3. Fallback: fetch for Makkah Al-Mukarramah from internet
        android.util.Log.d("OnlinePrayer", "Auto-detect falling back to Makkah online fetch")
        val makkahPreset = PrayerTimeCalculator.cities.first()
        val makkahOnline = fetchPrayerTimesForCity(makkahPreset)
        if (makkahOnline != null) return@withContext makkahOnline

        // 4. Fallback to cached online data
        getCachedPrayerResult()
    }

    suspend fun fetchPrayerTimesForCity(city: CityPreset): OnlinePrayerResult? = withContext(Dispatchers.IO) {
        fetchPrayerTimesForLocation(
            lat = city.latitude,
            lon = city.longitude,
            locationName = city.nameArabic
        )
    }

    suspend fun fetchPrayerTimesForLocation(
        lat: Double,
        lon: Double,
        locationName: String
    ): OnlinePrayerResult? = withContext(Dispatchers.IO) {
        try {
            // Method 4 is Umm al-Qura (standard for Gulf/Saudi Arabia/General)
            val url = "https://api.aladhan.com/v1/timings?latitude=$lat&longitude=$lon&method=4"
            android.util.Log.d("OnlinePrayer", "Requesting: $url")
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) QuranApp")
                .build()

            val response = client.newCall(request).execute()
            android.util.Log.d("OnlinePrayer", "Response code: ${response.code}")
            if (!response.isSuccessful) return@withContext null
            val bodyString = response.body?.string() ?: return@withContext null
            val json = JSONObject(bodyString)
            if (json.optInt("code") != 200) {
                android.util.Log.w("OnlinePrayer", "API returned non-200 status in body: $bodyString")
                return@withContext null
            }

            val data = json.getJSONObject("data")
            val result = parseAladhanData(data, locationName, lat, lon)

            if (result != null) {
                saveToCache(bodyString, locationName, lat, lon)
                android.util.Log.d("OnlinePrayer", "Successfully parsed and cached prayer times for $locationName")
            } else {
                android.util.Log.e("OnlinePrayer", "parseAladhanData returned null")
            }
            result
        } catch (e: Exception) {
            android.util.Log.e("OnlinePrayer", "Exception during fetchPrayerTimesForLocation: ${e.message}", e)
            null
        }
    }

    private fun parseAladhanData(
        data: JSONObject,
        locationName: String,
        lat: Double,
        lon: Double
    ): OnlinePrayerResult? {
        return try {
            val timings = data.getJSONObject("timings")
            val dateObj = data.getJSONObject("date")
            val hijriObj = dateObj.getJSONObject("hijri")

            fun cleanTime(raw: String): String {
                return raw.trim().split(" ")[0].take(5)
            }

            val fajr = cleanTime(timings.getString("Fajr"))
            val sunrise = cleanTime(timings.getString("Sunrise"))
            val dhuhr = cleanTime(timings.getString("Dhuhr"))
            val asr = cleanTime(timings.getString("Asr"))
            val maghrib = cleanTime(timings.getString("Maghrib"))
            val isha = cleanTime(timings.getString("Isha"))

            val (nextPrayerName, remainingMins) = PrayerTimeCalculator.calculateNextPrayer(
                fajr = fajr,
                sunrise = sunrise,
                dhuhr = dhuhr,
                asr = asr,
                maghrib = maghrib,
                isha = isha
            )

            // Format Hijri date with Arabic month and day
            val hDay = hijriObj.optString("day", "")
            val hMonthAr = hijriObj.optJSONObject("month")?.optString("ar", "") ?: ""
            val hYear = hijriObj.optString("year", "")
            val weekdayAr = hijriObj.optJSONObject("weekday")?.optString("ar", "") ?: ""

            val hijriFormatted = if (weekdayAr.isNotBlank()) {
                "$weekdayAr، $hDay $hMonthAr $hYear هـ"
            } else {
                "$hDay $hMonthAr $hYear هـ"
            }

            val nowTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            val prayerTimes = PrayerTimes(
                fajr = fajr,
                sunrise = sunrise,
                dhuhr = dhuhr,
                asr = asr,
                maghrib = maghrib,
                isha = isha,
                nextPrayerName = nextPrayerName,
                nextPrayerRemainingMinutes = remainingMins,
                locationName = locationName,
                isFromInternet = true,
                lastUpdatedTime = nowTimeStr
            )

            val qibla = PrayerTimeCalculator.calculateQiblaAngle(lat, lon)

            OnlinePrayerResult(
                prayerTimes = prayerTimes,
                hijriDate = hijriFormatted,
                cityName = locationName,
                latitude = lat,
                longitude = lon,
                qiblaAngle = qibla
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private data class IpLocation(val lat: Double, val lon: Double, val cityName: String)

    private fun getIpLocation(): IpLocation? {
        val endpoints = listOf(
            "http://ip-api.com/json",
            "https://ipapi.co/json/",
            "https://freeipapi.com/api/json"
        )
        for (url in endpoints) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 QuranApp")
                    .build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) continue
                val body = response.body?.string() ?: continue
                val json = JSONObject(body)

                val lat = when {
                    json.has("lat") -> json.getDouble("lat")
                    json.has("latitude") -> json.getDouble("latitude")
                    else -> continue
                }
                val lon = when {
                    json.has("lon") -> json.getDouble("lon")
                    json.has("longitude") -> json.getDouble("longitude")
                    else -> continue
                }
                val city = json.optString("city", json.optString("cityName", ""))
                val country = json.optString("country", json.optString("countryName", ""))
                val rawLocation = if (city.isNotBlank()) "$city، $country" else country
                val locationName = arabicizeLocationName(rawLocation).ifBlank { "موقعي الحالي" }
                android.util.Log.d("OnlinePrayer", "IP Location detected: $locationName ($lat, $lon)")
                return IpLocation(lat, lon, locationName)
            } catch (e: Exception) {
                android.util.Log.w("OnlinePrayer", "Failed IP geo url $url: ${e.message}")
            }
        }
        return null
    }

    private fun arabicizeLocationName(raw: String): String {
        var result = raw
        val dict = listOf(
            "Egypt" to "مصر",
            "Saudi Arabia" to "المملكة العربية السعودية",
            "United Arab Emirates" to "الإمارات",
            "Jordan" to "الأردن",
            "Kuwait" to "الكويت",
            "Qatar" to "قطر",
            "Bahrain" to "البحرين",
            "Oman" to "عُمان",
            "Iraq" to "العراق",
            "Syria" to "سوريا",
            "Lebanon" to "لبنان",
            "Palestine" to "فلسطين",
            "Yemen" to "اليمن",
            "Sudan" to "السودان",
            "Libya" to "ليبيا",
            "Tunisia" to "تونس",
            "Algeria" to "الجزائر",
            "Morocco" to "المغرب",
            "Cairo" to "القاهرة",
            "Giza" to "الجيزة",
            "Alexandria" to "الإسكندرية",
            "Bani Suwayf" to "بني سويف",
            "Banī Suwayf" to "بني سويف",
            "Beni Suef" to "بني سويف",
            "Mansoura" to "المنصورة",
            "Tanta" to "طنطا",
            "Asyut" to "أسيوط",
            "Sohag" to "سوهاج",
            "Qena" to "قنا",
            "Aswan" to "أسوان",
            "Luxor" to "الأقصر",
            "Port Said" to "بورسعيد",
            "Suez" to "السويس",
            "Ismailia" to "الإسماعيلية",
            "Zagazig" to "الزقازيق",
            "Faiyum" to "الفيوم",
            "Riyadh" to "الرياض",
            "Jeddah" to "جدة",
            "Makkah" to "مكة المكرمة",
            "Madinah" to "المدينة المنورة",
            "Dammam" to "الدمام",
            "Dubai" to "دبي",
            "Abu Dhabi" to "أبو ظبي",
            "Sharjah" to "الشارقة",
            "Doha" to "الدوحة",
            "Manama" to "المنامة",
            "Muscat" to "مسقط"
        )
        for ((en, ar) in dict) {
            result = result.replace(en, ar, ignoreCase = true)
        }
        return result
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation(): Location? {
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
            val gpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val passiveLoc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            listOfNotNull(gpsLoc, netLoc, passiveLoc).maxByOrNull { it.time }
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun saveToCache(rawJson: String, locationName: String, lat: Double, lon: Double) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        prefs.edit()
            .putString("cache_today", todayStr)
            .putString("cache_json", rawJson)
            .putString("cache_location", locationName)
            .putString("cache_lat", lat.toString())
            .putString("cache_lon", lon.toString())
            .apply()
    }

    fun getCachedPrayerResult(): OnlinePrayerResult? {
        return try {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val cachedDate = prefs.getString("cache_today", null) ?: return null
            if (cachedDate != todayStr) return null

            val rawJson = prefs.getString("cache_json", null) ?: return null
            val location = prefs.getString("cache_location", "موقعي") ?: "موقعي"
            val lat = prefs.getString("cache_lat", "0.0")?.toDoubleOrNull() ?: 0.0
            val lon = prefs.getString("cache_lon", "0.0")?.toDoubleOrNull() ?: 0.0

            val json = JSONObject(rawJson)
            val data = json.getJSONObject("data")
            parseAladhanData(data, location, lat, lon)
        } catch (e: Exception) {
            null
        }
    }
}
