package com.example.data.model

import com.example.data.local.FavoriteVideoEntity
import com.example.data.local.RecentDownloadEntity

data class AiVideo(
    val id: String,
    val title: String,
    val description: String,
    val url: String,
    val thumbnailUrl: String,
    val category: String,
    val durationSeconds: Int,
    val viewsCount: Int,
    val downloadsCount: Int,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toFavoriteEntity() = FavoriteVideoEntity(
        id = id,
        title = title,
        description = description,
        url = url,
        thumbnailUrl = thumbnailUrl,
        category = category,
        durationSeconds = durationSeconds,
        viewsCount = viewsCount,
        downloadsCount = downloadsCount
    )

    fun toRecentDownloadEntity() = RecentDownloadEntity(
        id = id,
        title = title,
        description = description,
        url = url,
        thumbnailUrl = thumbnailUrl,
        category = category,
        durationSeconds = durationSeconds,
        viewsCount = viewsCount,
        downloadsCount = downloadsCount
    )
}
