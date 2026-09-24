package com.example.data.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

enum class TafsirType(val id: Int, val title: String, val bookName: String, val author: String) {
    MUYASSAR(1, "التفسير الميسر", "التفسير الميسر", "نخبة من العلماء"),
    IBN_KATHIR(4, "تفسير ابن كثير", "تفسير القرآن العظيم", "عماد الدين إسماعيل بن كثير"),
    SAADI(3, "تفسير السعدي", "تيسير الكريم الرحمن في تفسير كلام المنان", "الشيخ عبد الرحمن بن ناصر السعدي")
}

class TafsirRepository(private val context: Context) {

    private val memoryCache = ConcurrentHashMap<String, String>()
    private val diskCacheDir by lazy {
        File(context.cacheDir, "tafseer_cache").apply { mkdirs() }
    }

    suspend fun getTafsir(
        tafsirType: TafsirType,
        surahNumber: Int,
        ayahNumber: Int,
        fallbackMuyassar: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val cacheKey = "${tafsirType.id}_${surahNumber}_$ayahNumber"

        // 1. Check in-memory cache
        memoryCache[cacheKey]?.let { return@withContext Result.success(it) }

        // 2. If Muyassar and provided locally from JSON
        if (tafsirType == TafsirType.MUYASSAR && fallbackMuyassar.isNotBlank()) {
            memoryCache[cacheKey] = fallbackMuyassar
            return@withContext Result.success(fallbackMuyassar)
        }

        // 3. Check pre-packaged local assets (e.g. for Surah 1)
        try {
            val assetFileName = when (tafsirType) {
                TafsirType.SAADI -> "tafseer/saadi_$surahNumber.json"
                TafsirType.IBN_KATHIR -> "tafseer/ibnkathir_$surahNumber.json"
                else -> null
            }
            if (assetFileName != null) {
                val assetContent = context.assets.open(assetFileName).bufferedReader().use { it.readText() }
                val json = JSONObject(assetContent)
                val text = json.optString(ayahNumber.toString(), "")
                if (text.isNotBlank()) {
                    memoryCache[cacheKey] = text
                    return@withContext Result.success(text)
                }
            }
        } catch (_: Exception) {}

        // 4. Check disk cache
        val diskFile = File(diskCacheDir, "$cacheKey.txt")
        if (diskFile.exists()) {
            try {
                val cachedText = diskFile.readText(Charsets.UTF_8)
                if (cachedText.isNotBlank()) {
                    memoryCache[cacheKey] = cachedText
                    return@withContext Result.success(cachedText)
                }
            } catch (_: Exception) {}
        }

        // 5. Fetch online from api.quran-tafseer.com
        try {
            val urlString = "http://api.quran-tafseer.com/tafseer/${tafsirType.id}/$surahNumber/$ayahNumber"
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 12000
                setRequestProperty("User-Agent", "QuranApp-Android")
                setRequestProperty("Accept", "application/json")
            }

            val code = conn.responseCode
            if (code in 200..299) {
                val response = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val json = JSONObject(response)
                val text = json.optString("text", "").trim()
                if (text.isNotEmpty()) {
                    memoryCache[cacheKey] = text
                    try {
                        diskFile.writeText(text, Charsets.UTF_8)
                    } catch (_: Exception) {}
                    return@withContext Result.success(text)
                }
            }
            Result.failure(Exception("لم يتم العثور على التفسير (كود: $code)"))
        } catch (e: Exception) {
            Log.e("TafsirRepository", "Failed to fetch tafsir $cacheKey: ${e.message}")
            Result.failure(e)
        }
    }
}
