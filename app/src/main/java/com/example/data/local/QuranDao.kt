package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber)")
    fun isBookmarked(surahNumber: Int, ayahNumber: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber")
    suspend fun deleteBookmarkByAyah(surahNumber: Int, ayahNumber: Int)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    // Reading History
    @Query("SELECT * FROM reading_history ORDER BY lastReadTimestamp DESC LIMIT 1")
    fun getLastRead(): Flow<ReadingHistoryEntity?>

    @Query("SELECT * FROM reading_history ORDER BY lastReadTimestamp DESC")
    fun getAllReadingHistory(): Flow<List<ReadingHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReadingHistory(history: ReadingHistoryEntity)

    // User Notes
    @Query("SELECT * FROM user_notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<UserNoteEntity>>

    @Query("SELECT * FROM user_notes WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber")
    fun getNotesForAyah(surahNumber: Int, ayahNumber: Int): Flow<List<UserNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: UserNoteEntity)

    @Delete
    suspend fun deleteNote(note: UserNoteEntity)

    // Memorization Progress
    @Query("SELECT * FROM memorization_progress")
    fun getAllMemorization(): Flow<List<MemorizationEntity>>

    @Query("SELECT * FROM memorization_progress WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber LIMIT 1")
    suspend fun getMemorizationForAyah(surahNumber: Int, ayahNumber: Int): MemorizationEntity?

    @Query("SELECT * FROM memorization_progress WHERE nextReview <= :currentTimestamp AND status != 'NOT_STARTED'")
    fun getDueReviews(currentTimestamp: Long): Flow<List<MemorizationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMemorization(mem: MemorizationEntity)

    // Prayer Logs
    @Query("SELECT * FROM prayer_logs WHERE dateString = :dateString LIMIT 1")
    fun getPrayerLogForDate(dateString: String): Flow<PrayerLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerLog(prayerLog: PrayerLogEntity)

    // Tasbih
    @Query("SELECT * FROM tasbih_logs")
    fun getAllTasbih(): Flow<List<TasbihEntity>>

    @Query("SELECT * FROM tasbih_logs WHERE id = :id LIMIT 1")
    fun getTasbihById(id: String): Flow<TasbihEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTasbih(tasbih: TasbihEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialTasbih(tasbihList: List<TasbihEntity>)

    @Delete
    suspend fun deleteTasbih(tasbih: TasbihEntity)

    // Custom Azkar
    @Query("SELECT * FROM custom_azkar ORDER BY createdAt DESC")
    fun getAllCustomAzkar(): Flow<List<CustomZikrEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomZikr(zikr: CustomZikrEntity): Long

    @Update
    suspend fun updateCustomZikr(zikr: CustomZikrEntity)

    @Delete
    suspend fun deleteCustomZikr(zikr: CustomZikrEntity)

    @Query("DELETE FROM custom_azkar WHERE id = :id")
    suspend fun deleteCustomZikrById(id: Long)
}
