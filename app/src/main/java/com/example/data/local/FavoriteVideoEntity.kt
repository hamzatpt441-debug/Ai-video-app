package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_videos")
data class FavoriteVideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val url: String,
    val thumbnailUrl: String,
    val category: String,
    val durationSeconds: Int,
    val viewsCount: Int,
    val downloadsCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
