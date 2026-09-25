package com.mosalah.quran.data.quran

import android.content.Context
import android.util.Log
import com.mosalah.quran.data.model.Ayah
import com.mosalah.quran.data.model.DuaItem
import com.mosalah.quran.data.model.Qari
import com.mosalah.quran.data.model.RevelationType
import com.mosalah.quran.data.model.Surah
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap

object QuranDataProvider {

    private var appContext: Context? = null
    private val memoryCache = ConcurrentHashMap<Int, List<Ayah>>()
    private var cachedAzkar: List<DuaItem>? = null

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    val qaris = listOf(
        Qari(
            id = "alafasy",
            nameArabic = "مشاري بن راشد العفاسي",
            nameEnglish = "Mishary Rashid Alafasy",
            style = "مرتل - هادئ ومتقن",
            audioSubfolder = "Alafasy_128kbps",
            country = "الكويت"
        ),
        Qari(
            id = "abdulbasit",
            nameArabic = "عبد الباسط عبد الصمد",
            nameEnglish = "Abdul Basit Abdul Samad",
            style = "مرتل - الصوت الخالد",
            audioSubfolder = "AbdulSamad_64kbps_QuranExplorer.Com",
            country = "مصر"
        ),
        Qari(
            id = "sudais",
            nameArabic = "عبد الرحمن السديس",
            nameEnglish = "Abdur-Rahman As-Sudais",
            style = "تلاوة الحرم المكي الشريف",
            audioSubfolder = "Abdurrahmaan_As-Sudais_192kbps",
            country = "السعودية"
        ),
        Qari(
            id = "husary",
            nameArabic = "محمود خليل الحصري",
            nameEnglish = "Mahmoud Khalil Al-Husary",
            style = "المصحف المعلم - للمتعلمين",
            audioSubfolder = "Husary_128kbps",
            country = "مصر"
        ),
        Qari(
            id = "ghamadi",
            nameArabic = "سعد الغامدي",
            nameEnglish = "Saad Al-Ghamdi",
            style = "مرتل - خاشع",
            audioSubfolder = "Ghamadi_40kbps",
            country = "السعودية"
        )
    )

    // Complete 114 Surahs Directory
    val surahs: List<Surah> = listOf(
        Surah(1, "الفَاتِحَة", "Al-Fatihah", "The Opening", 7, RevelationType.MAKKI, 1, 1),
        Surah(2, "البَقَرَة", "Al-Baqarah", "The Cow", 286, RevelationType.MADANI, 2, 1),
        Surah(3, "آل عِمْرَان", "Ali 'Imran", "Family of Imran", 200, RevelationType.MADANI, 50, 3),
        Surah(4, "النِّسَاء", "An-Nisa", "The Women", 176, RevelationType.MADANI, 77, 4),
        Surah(5, "المَائِدَة", "Al-Ma'idah", "The Table Spread", 120, RevelationType.MADANI, 106, 6),
        Surah(6, "الأَنْعَام", "Al-An'am", "The Cattle", 165, RevelationType.MAKKI, 128, 7),
        Surah(7, "الأَعْرَاف", "Al-A'raf", "The Heights", 206, RevelationType.MAKKI, 151, 8),
        Surah(8, "الأَنْفَال", "Al-Anfal", "The Spoils of War", 75, RevelationType.MADANI, 177, 9),
        Surah(9, "التَّوْبَة", "At-Tawbah", "The Repentance", 129, RevelationType.MADANI, 187, 10),
        Surah(10, "يُونُس", "Yunus", "Jonah", 109, RevelationType.MAKKI, 208, 11),
        Surah(11, "هُود", "Hud", "Hud", 123, RevelationType.MAKKI, 221, 11),
        Surah(12, "يُوسُف", "Yusuf", "Joseph", 111, RevelationType.MAKKI, 235, 12),
        Surah(13, "الرَّعْد", "Ar-Ra'd", "The Thunder", 43, RevelationType.MADANI, 249, 13),
        Surah(14, "إِبْرَاهِيم", "Ibrahim", "Abraham", 52, RevelationType.MAKKI, 255, 13),
        Surah(15, "الحِجْر", "Al-Hijr", "The Rocky Tract", 99, RevelationType.MAKKI, 262, 14),
        Surah(16, "النَّحْل", "An-Nahl", "The Bee", 128, RevelationType.MAKKI, 267, 14),
        Surah(17, "الإِسْرَاء", "Al-Isra", "The Night Journey", 111, RevelationType.MAKKI, 282, 15),
        Surah(18, "الكَهْف", "Al-Kahf", "The Cave", 110, RevelationType.MAKKI, 293, 15),
        Surah(19, "مَرْيَم", "Maryam", "Mary", 98, RevelationType.MAKKI, 305, 16),
        Surah(20, "طه", "Ta-Ha", "Ta-Ha", 135, RevelationType.MAKKI, 312, 16),
        Surah(21, "الأَنْبِيَاء", "Al-Anbiya", "The Prophets", 112, RevelationType.MAKKI, 322, 17),
        Surah(22, "الحَجّ", "Al-Hajj", "The Pilgrimage", 78, RevelationType.MADANI, 332, 17),
        Surah(23, "المُؤْمِنُون", "Al-Mu'minun", "The Believers", 118, RevelationType.MAKKI, 342, 18),
        Surah(24, "النُّور", "An-Nur", "The Light", 64, RevelationType.MADANI, 350, 18),
        Surah(25, "الفُرْقَان", "Al-Furqan", "The Criterion", 77, RevelationType.MAKKI, 359, 18),
        Surah(26, "الشُّعَرَاء", "Ash-Shu'ara", "The Poets", 227, RevelationType.MAKKI, 367, 19),
        Surah(27, "النَّمْل", "An-Naml", "The Ant", 93, RevelationType.MAKKI, 377, 19),
        Surah(28, "القَصَص", "Al-Qasas", "The Stories", 88, RevelationType.MAKKI, 385, 20),
        Surah(29, "العَنْكَبُوت", "Al-'Ankabut", "The Spider", 69, RevelationType.MAKKI, 396, 20),
        Surah(30, "الرُّوم", "Ar-Rum", "The Romans", 60, RevelationType.MAKKI, 404, 21),
        Surah(31, "لُقْمَان", "Luqman", "Luqman", 34, RevelationType.MAKKI, 411, 21),
        Surah(32, "السَّجْدَة", "As-Sajdah", "The Prostration", 30, RevelationType.MAKKI, 415, 21),
        Surah(33, "الأَحْزَاب", "Al-Ahzab", "The Combined Forces", 73, RevelationType.MADANI, 418, 21),
        Surah(34, "سَبَأ", "Saba", "Sheba", 54, RevelationType.MAKKI, 428, 22),
        Surah(35, "فَاطِر", "Fatir", "The Originator", 45, RevelationType.MAKKI, 434, 22),
        Surah(36, "يس", "Ya-Sin", "Ya-Sin", 83, RevelationType.MAKKI, 440, 22),
        Surah(37, "الصَّافَّات", "As-Saffat", "Those Who Set The Ranks", 182, RevelationType.MAKKI, 446, 23),
        Surah(38, "ص", "Sad", "The Letter Sad", 88, RevelationType.MAKKI, 453, 23),
        Surah(39, "الزُّمَر", "Az-Zumar", "The Troops", 75, RevelationType.MAKKI, 458, 23),
        Surah(40, "غَافِر", "Ghafir", "The Forgiver", 85, RevelationType.MAKKI, 467, 24),
        Surah(41, "فُصِّلَت", "Fussilat", "Explained In Detail", 54, RevelationType.MAKKI, 477, 24),
        Surah(42, "الشُّورَى", "Ash-Shura", "The Consultation", 53, RevelationType.MAKKI, 483, 25),
        Surah(43, "الزُّخْرُف", "Az-Zukhruf", "The Ornaments of Gold", 89, RevelationType.MAKKI, 489, 25),
        Surah(44, "الدُّخَان", "Ad-Dukhan", "The Smoke", 59, RevelationType.MAKKI, 496, 25),
        Surah(45, "الجَاثِيَة", "Al-Jathiyah", "The Crouching", 37, RevelationType.MAKKI, 499, 25),
        Surah(46, "الأَحْقَاف", "Al-Ahqaf", "The Wind-Curved Sandhills", 35, RevelationType.MAKKI, 502, 26),
        Surah(47, "مُحَمَّد", "Muhammad", "Muhammad", 38, RevelationType.MADANI, 507, 26),
        Surah(48, "الفَتْح", "Al-Fath", "The Victory", 29, RevelationType.MADANI, 511, 26),
        Surah(49, "الحُجُرَات", "Al-Hujurat", "The Rooms", 18, RevelationType.MADANI, 515, 26),
        Surah(50, "ق", "Qaf", "The Letter Qaf", 45, RevelationType.MAKKI, 518, 26),
        Surah(51, "الذَّارِيَات", "Adh-Dhariyat", "The Winnowing Winds", 60, RevelationType.MAKKI, 520, 26),
        Surah(52, "الطُّور", "At-Tur", "The Mount", 49, RevelationType.MAKKI, 523, 27),
        Surah(53, "النَّجْم", "An-Najm", "The Star", 62, RevelationType.MAKKI, 526, 27),
        Surah(54, "القَمَر", "Al-Qamar", "The Moon", 55, RevelationType.MAKKI, 528, 27),
        Surah(55, "الرَّحْمَٰن", "Ar-Rahman", "The Beneficent", 78, RevelationType.MADANI, 531, 27),
        Surah(56, "الوَاقِعَة", "Al-Waqi'ah", "The Inevitable", 96, RevelationType.MAKKI, 534, 27),
        Surah(57, "الحَدِيد", "Al-Hadid", "The Iron", 29, RevelationType.MADANI, 537, 27),
        Surah(58, "المُجَادَلَة", "Al-Mujadila", "The Pleading Woman", 22, RevelationType.MADANI, 542, 28),
        Surah(59, "الحَشْر", "Al-Hashr", "The Exile", 24, RevelationType.MADANI, 545, 28),
        Surah(60, "المُمْتَحَنَة", "Al-Mumtahanah", "She That Is To Be Examined", 13, RevelationType.MADANI, 549, 28),
        Surah(61, "الصَّفّ", "As-Saff", "The Ranks", 14, RevelationType.MADANI, 551, 28),
        Surah(62, "الجُمُعَة", "Al-Jumu'ah", "Friday", 11, RevelationType.MADANI, 553, 28),
        Surah(63, "المُنَافِقُون", "Al-Munafiqun", "The Hypocrites", 11, RevelationType.MADANI, 554, 28),
        Surah(64, "التَّغَابُن", "At-Taghabun", "Mutual Loss and Gain", 18, RevelationType.MADANI, 556, 28),
        Surah(65, "الطَّلَاق", "At-Talaq", "The Divorce", 12, RevelationType.MADANI, 558, 28),
        Surah(66, "التَّحْرِيم", "At-Tahrim", "The Prohibition", 12, RevelationType.MADANI, 560, 28),
        Surah(67, "المُلْك", "Al-Mulk", "The Dominion", 30, RevelationType.MAKKI, 562, 29),
        Surah(68, "القَلَم", "Al-Qalam", "The Pen", 52, RevelationType.MAKKI, 564, 29),
        Surah(69, "الحَاقَّة", "Al-Haqqah", "The Inevitable Truth", 52, RevelationType.MAKKI, 566, 29),
        Surah(70, "المَعَارِج", "Al-Ma'arij", "The Ascending Stairways", 44, RevelationType.MAKKI, 568, 29),
        Surah(71, "نُوح", "Nuh", "Noah", 28, RevelationType.MAKKI, 570, 29),
        Surah(72, "الجِنّ", "Al-Jinn", "The Jinn", 28, RevelationType.MAKKI, 572, 29),
        Surah(73, "المُزَّمِّل", "Al-Muzzammil", "The Enshrouded One", 20, RevelationType.MAKKI, 574, 29),
        Surah(74, "المُدَّثِّر", "Al-Muddaththir", "The Cloaked One", 56, RevelationType.MAKKI, 575, 29),
        Surah(75, "القِيَامَة", "Al-Qiyamah", "The Resurrection", 40, RevelationType.MAKKI, 577, 29),
        Surah(76, "الإِنْسَان", "Al-Insan", "Man", 31, RevelationType.MADANI, 578, 29),
        Surah(77, "المُرْسَلَات", "Al-Mursalat", "The Emissaries", 50, RevelationType.MAKKI, 580, 29),
        Surah(78, "النَّبَأ", "An-Naba", "The Tidings", 40, RevelationType.MAKKI, 582, 30),
        Surah(79, "النَّازِعَات", "An-Nazi'at", "Those Who Drag Forth", 46, RevelationType.MAKKI, 583, 30),
        Surah(80, "عَبَسَ", "'Abasa", "He Frowned", 42, RevelationType.MAKKI, 585, 30),
        Surah(81, "التَّكْوِير", "At-Takwir", "The Overthrowing", 29, RevelationType.MAKKI, 586, 30),
        Surah(82, "الانْفِطَار", "Al-Infitar", "The Cleaving", 19, RevelationType.MAKKI, 587, 30),
        Surah(83, "المُطَفِّفِين", "Al-Mutaffifin", "Those Who Deal in Fraud", 36, RevelationType.MAKKI, 587, 30),
        Surah(84, "الانْشِقَاق", "Al-Inshiqaq", "The Splitting Open", 25, RevelationType.MAKKI, 589, 30),
        Surah(85, "البُرُوج", "Al-Buruj", "The Constellations", 22, RevelationType.MAKKI, 590, 30),
        Surah(86, "الطَّارِق", "At-Tariq", "The Night-Comer", 17, RevelationType.MAKKI, 591, 30),
        Surah(87, "الأَعْلَى", "Al-A'la", "The Most High", 19, RevelationType.MAKKI, 591, 30),
        Surah(88, "الغَاشِيَة", "Al-Ghashiyah", "The Overwhelming Event", 26, RevelationType.MAKKI, 592, 30),
        Surah(89, "الفَجْر", "Al-Fajr", "The Dawn", 30, RevelationType.MAKKI, 593, 30),
        Surah(90, "البَلَد", "Al-Balad", "The City", 20, RevelationType.MAKKI, 594, 30),
        Surah(91, "الشَّمْس", "Ash-Shams", "The Sun", 15, RevelationType.MAKKI, 595, 30),
        Surah(92, "اللَّيْل", "Al-Layl", "The Night", 21, RevelationType.MAKKI, 595, 30),
        Surah(93, "الضُّحَى", "Ad-Duha", "The Morning Brightness", 11, RevelationType.MAKKI, 596, 30),
        Surah(94, "الشَّرْح", "Ash-Sharh", "The Relief", 8, RevelationType.MAKKI, 596, 30),
        Surah(95, "التِّين", "At-Tin", "The Fig", 8, RevelationType.MAKKI, 597, 30),
        Surah(96, "العَلَق", "Al-'Alaq", "The Clot", 19, RevelationType.MAKKI, 597, 30),
        Surah(97, "القَدْر", "Al-Qadr", "The Night of Decree", 5, RevelationType.MAKKI, 598, 30),
        Surah(98, "البَيِّنَة", "Al-Bayyinah", "The Clear Evidence", 8, RevelationType.MADANI, 598, 30),
        Surah(99, "الزَّلْزَلَة", "Az-Zalzalah", "The Earthquake", 8, RevelationType.MADANI, 599, 30),
        Surah(100, "العَادِيَات", "Al-'Adiyat", "The Courser", 11, RevelationType.MAKKI, 599, 30),
        Surah(101, "القَارِعَة", "Al-Qari'ah", "The Calamity", 11, RevelationType.MAKKI, 600, 30),
        Surah(102, "التَّكَاثُر", "At-Takathur", "The Rivalry in World Increase", 8, RevelationType.MAKKI, 600, 30),
        Surah(103, "العَصْر", "Al-'Asr", "The Declining Day", 3, RevelationType.MAKKI, 601, 30),
        Surah(104, "الهُمَزَة", "Al-Humazah", "The Scorner", 9, RevelationType.MAKKI, 601, 30),
        Surah(105, "الفِيل", "Al-Fil", "The Elephant", 5, RevelationType.MAKKI, 601, 30),
        Surah(106, "قُرَيْش", "Quraysh", "Quraysh", 4, RevelationType.MAKKI, 602, 30),
        Surah(107, "المَاعُون", "Al-Ma'un", "The Small Kindness", 7, RevelationType.MAKKI, 602, 30),
        Surah(108, "الكَوْثَر", "Al-Kawthar", "The Abundance", 3, RevelationType.MAKKI, 602, 30),
        Surah(109, "الكٰفِرُون", "Al-Kafirun", "The Disbelievers", 6, RevelationType.MAKKI, 603, 30),
        Surah(110, "النَّصْر", "An-Nasr", "The Divine Support", 3, RevelationType.MADANI, 603, 30),
        Surah(111, "المَسَد", "Al-Masad", "The Palm Fiber", 5, RevelationType.MAKKI, 603, 30),
        Surah(112, "الإِخْلَاص", "Al-Ikhlas", "The Sincerity", 4, RevelationType.MAKKI, 604, 30),
        Surah(113, "الفَلَق", "Al-Falaq", "The Daybreak", 5, RevelationType.MAKKI, 604, 30),
        Surah(114, "النَّاس", "An-Nas", "Mankind", 6, RevelationType.MAKKI, 604, 30)
    )

    val hisnDuas: List<DuaItem>
        get() = getDuas()

    fun getDuas(context: Context? = null): List<DuaItem> {
        cachedAzkar?.let { return it }
        val ctx = context?.applicationContext ?: appContext
        if (ctx != null) {
            try {
                val jsonString = ctx.assets.open("azkar.json").bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                val list = ArrayList<DuaItem>(jsonArray.length())
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        DuaItem(
                            id = obj.optInt("id", i + 1),
                            category = obj.optString("category", ""),
                            categoryArabic = obj.optString("categoryArabic", ""),
                            titleArabic = obj.optString("titleArabic", ""),
                            titleEnglish = "",
                            arabicText = obj.optString("arabicText", ""),
                            translation = "",
                            reference = obj.optString("reference", ""),
                            targetCount = obj.optInt("targetCount", 1),
                            benefits = obj.optString("benefits", "")
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    cachedAzkar = list
                    return list
                }
            } catch (e: Exception) {
                Log.e("QuranDataProvider", "Error loading azkar.json: ${e.message}")
            }
        }
        return emptyList()
    }

    fun getAyahsForSurah(surahNumber: Int, context: Context? = null): List<Ayah> {
        memoryCache[surahNumber]?.let { return it }

        // Load authentic verses for all 114 Surahs from packaged local JSON assets
        val ctx = context?.applicationContext ?: appContext
        if (ctx != null) {
            try {
                val jsonString = ctx.assets.open("surahs/$surahNumber.json").bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                val list = ArrayList<Ayah>(jsonArray.length())
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    list.add(
                        Ayah(
                            surahNumber = item.optInt("surahNumber", surahNumber),
                            ayahNumber = item.optInt("ayahNumber", i + 1),
                            textArabic = item.optString("textArabic", ""),
                            textEnglish = "",
                            textFrench = "",
                            textUrdu = "",
                            tafsirMuyassar = item.optString("tafsirMuyassar", ""),
                            page = item.optInt("page", 0)
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    memoryCache[surahNumber] = list
                    return list
                }
            } catch (e: Exception) {
                Log.e("QuranDataProvider", "Error loading surah $surahNumber from assets: ${e.message}")
            }
        }

        // Graceful fallback if assets not yet initialized
        val surah = surahs.find { it.number == surahNumber } ?: return emptyList()
        val count = surah.versesCount
        val fallbackList = ArrayList<Ayah>(count)
        for (i in 1..count) {
            fallbackList.add(
                Ayah(
                    surahNumber = surahNumber,
                    ayahNumber = i,
                    textArabic = "آية $i من سورة ${surah.nameArabic}",
                    textEnglish = "",
                    textFrench = "",
                    textUrdu = "",
                    tafsirMuyassar = "تفسير الآية $i من سورة ${surah.nameArabic}.",
                    page = surah.startPage
                )
            )
        }
        return fallbackList
    }

    fun getPagesForSurah(surahNumber: Int, context: Context? = null): Map<Int, List<Ayah>> {
        val ayahs = getAyahsForSurah(surahNumber, context)
        return ayahs.groupBy { if (it.page > 0) it.page else (surahs.find { s -> s.number == surahNumber }?.startPage ?: 1) }
    }

    fun getAudioUrl(qari: Qari, surahNumber: Int, ayahNumber: Int): String {
        val sNum = surahNumber.toString().padStart(3, '0')
        val aNum = ayahNumber.toString().padStart(3, '0')
        return "https://everyayah.com/data/${qari.audioSubfolder}/$sNum$aNum.mp3"
    }
}
