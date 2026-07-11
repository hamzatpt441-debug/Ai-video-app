package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    // Favorites
    @Query("SELECT * FROM favorite_videos ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteVideoEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_videos WHERE id = :id)")
    fun isFavorite(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(video: FavoriteVideoEntity)

    @Query("DELETE FROM favorite_videos WHERE id = :id")
    suspend fun deleteFavorite(id: String)

    // Recent Downloads
    @Query("SELECT * FROM recent_downloads ORDER BY timestamp DESC")
    fun getAllRecentDownloads(): Flow<List<RecentDownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentDownload(video: RecentDownloadEntity)

    @Query("DELETE FROM recent_downloads WHERE id = :id")
    suspend fun deleteRecentDownload(id: String)

    @Query("DELETE FROM recent_downloads")
    suspend fun clearAllRecentDownloads()
}
