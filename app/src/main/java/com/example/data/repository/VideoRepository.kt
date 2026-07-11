package com.example.data.repository

import android.net.Uri
import android.util.Log
import com.example.data.local.VideoDao
import com.example.data.model.AiVideo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class VideoRepository(
    private val videoDao: VideoDao
) {
    private val firestore = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        Log.w("VideoRepository", "Firebase not initialized: ${e.message}")
        null
    }

    private val seedVideos = listOf(
        AiVideo(
            id = "seed_1",
            title = "Forest Stream Sunlight",
            description = "A peaceful stream flowing through a lush forest during golden hour. Serene nature loop.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-forest-stream-in-the-sunlight-529-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=500&auto=format&fit=crop",
            category = "Nature",
            durationSeconds = 15,
            viewsCount = 1240,
            downloadsCount = 420
        ),
        AiVideo(
            id = "seed_2",
            title = "Ocean Waves Aerial",
            description = "Beautiful breaking waves on the sandy shore from a top-down drone view. Sparkling emerald sea water.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-waves-breaking-on-the-shore-from-above-154-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1505118380757-91f5f5632de0?w=500&auto=format&fit=crop",
            category = "Sea",
            durationSeconds = 25,
            viewsCount = 3820,
            downloadsCount = 1190
        ),
        AiVideo(
            id = "seed_3",
            title = "Mysterious Pale Moon",
            description = "High-definition time-lapse of a glowing lunar surface surrounded by flowing dark space clouds.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-mysterious-pale-looking-moon-with-clouds-39912-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=500&auto=format&fit=crop",
            category = "Sky",
            durationSeconds = 30,
            viewsCount = 920,
            downloadsCount = 310
        ),
        AiVideo(
            id = "seed_4",
            title = "Raindrops on Puddle",
            description = "Cinematic slow-motion close-up of water droplets forming patterns on a forest floor puddle.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-rain-drops-falling-on-a-puddle-1574-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?w=500&auto=format&fit=crop",
            category = "Rain",
            durationSeconds = 18,
            viewsCount = 1560,
            downloadsCount = 590
        ),
        AiVideo(
            id = "seed_5",
            title = "Deep Reef Coral Life",
            description = "Stunning underwater footage of colorful coral reefs and tropical schools of fish.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-under-the-sea-with-fishes-and-reefs-41584-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1546026423-cc4642628d2b?w=500&auto=format&fit=crop",
            category = "Sea",
            durationSeconds = 20,
            viewsCount = 2100,
            downloadsCount = 680
        ),
        AiVideo(
            id = "seed_6",
            title = "Slow Motion Rain Window",
            description = "Cozy and ambient macro lens shot of rain sliding down a window pane with soft bokeh lights.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-slow-motion-of-rain-drops-on-a-window-34289-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1428908728789-d2de25dbd4e2?w=500&auto=format&fit=crop",
            category = "Rain",
            durationSeconds = 24,
            viewsCount = 4410,
            downloadsCount = 1820
        ),
        AiVideo(
            id = "seed_7",
            title = "Deep Space Galaxy",
            description = "Vibrant nebula clouds, dust trails, and distant shimmering stars. Perfect abstract background.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-stars-in-the-space-background-1611-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500&auto=format&fit=crop",
            category = "Abstract",
            durationSeconds = 15,
            viewsCount = 2900,
            downloadsCount = 950
        ),
        AiVideo(
            id = "seed_8",
            title = "Tokyo Neon Night",
            description = "Vibrant city night view, neon signs, and fast-moving light streaks. Futuristic atmosphere.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-time-lapse-of-a-city-at-night-140-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=500&auto=format&fit=crop",
            category = "City",
            durationSeconds = 22,
            viewsCount = 5200,
            downloadsCount = 2100
        ),
        AiVideo(
            id = "seed_9",
            title = "Developer Coding",
            description = "Close-up of a programmer typing code on an ergonomic mechanical keyboard. Green cyber vibes.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-hands-of-a-developer-typing-on-a-keyboard-40156-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=500&auto=format&fit=crop",
            category = "Technology",
            durationSeconds = 12,
            viewsCount = 1880,
            downloadsCount = 490
        ),
        AiVideo(
            id = "seed_10",
            title = "Glowing Cyber Coin",
            description = "Abstract 3D render of a futuristic silver cryptocurrency coin spinning against a dark cyber background.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-spinning-silver-cyber-coin-42456-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1621761191319-c6fb62004040?w=500&auto=format&fit=crop",
            category = "Technology",
            durationSeconds = 18,
            viewsCount = 940,
            downloadsCount = 280
        ),
        AiVideo(
            id = "seed_11",
            title = "Waterfall in Dense Forest",
            description = "Beautiful secluded waterfall cascading down mossy green boulders deep within a mountain range.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-waterfall-in-forest-2213-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=500&auto=format&fit=crop",
            category = "Nature",
            durationSeconds = 20,
            viewsCount = 3100,
            downloadsCount = 980
        ),
        AiVideo(
            id = "seed_12",
            title = "Idea Ignition Bulb",
            description = "A glowing light bulb held by a hand in pitch black, sparking motivational and creative energy.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-holding-a-glowing-light-bulb-in-the-dark-42442-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1493612276216-ee3925520721?w=500&auto=format&fit=crop",
            category = "Motivation",
            durationSeconds = 25,
            viewsCount = 1750,
            downloadsCount = 440
        ),
        AiVideo(
            id = "seed_13",
            title = "Snowy Pine Forests",
            description = "Breathtaking drone pan of a deep snow-covered pine tree forest in freezing alpine conditions.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-aerial-view-of-thick-snow-covered-forest-41618-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1482862549707-f63cb32c5fd9?w=500&auto=format&fit=crop",
            category = "Nature",
            durationSeconds = 16,
            viewsCount = 2500,
            downloadsCount = 720
        ),
        AiVideo(
            id = "seed_14",
            title = "Brewing Coffee Morning",
            description = "Satisfying close-up of dark freshly brewed coffee flowing into a clean white ceramic mug.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-pouring-hot-coffee-into-a-cup-42211-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=500&auto=format&fit=crop",
            category = "Food",
            durationSeconds = 15,
            viewsCount = 3980,
            downloadsCount = 1450
        ),
        AiVideo(
            id = "seed_15",
            title = "Morning Run Athlete",
            description = "A disciplined athlete training on a professional red running track in high-contrast morning light.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-runner-training-on-a-running-track-41566-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1476480862126-209bfaa8edc8?w=500&auto=format&fit=crop",
            category = "Sports",
            durationSeconds = 18,
            viewsCount = 1200,
            downloadsCount = 350
        ),
        AiVideo(
            id = "seed_16",
            title = "Zen Meditation Mountain",
            description = "An inspiring clip of a woman meditating peacefully on a high mountain cliff with clouds passing.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-meditating-woman-in-nature-with-eyes-closed-42352-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=500&auto=format&fit=crop",
            category = "Motivation",
            durationSeconds = 30,
            viewsCount = 5400,
            downloadsCount = 1930
        ),
        AiVideo(
            id = "seed_17",
            title = "Cozy Sleeping Kitten",
            description = "A cute little fluffy kitten sleeping peacefully on a soft warm fabric sofa. Perfect animal video.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-cute-cat-lying-on-a-sofa-42516-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=500&auto=format&fit=crop",
            category = "Animals",
            durationSeconds = 15,
            viewsCount = 8900,
            downloadsCount = 3200
        ),
        AiVideo(
            id = "seed_18",
            title = "Raindrops on Green Leaves",
            description = "Pristine macro shot of rain dropping on glistening dark green forest leaves during a monsoon.",
            url = "https://assets.mixkit.co/videos/preview/mixkit-close-up-of-green-leaves-with-rain-drops-41571-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1534274988757-a28bf1a57c17?w=500&auto=format&fit=crop",
            category = "Rain",
            durationSeconds = 14,
            viewsCount = 2800,
            downloadsCount = 870
        )
    )

    // Dynamic state that merges uploaded videos and seed videos
    private val uploadedVideosState = MutableStateFlow<List<AiVideo>>(emptyList())

    init {
        // Start listening to Firestore if available
        listenToFirestore()
    }

    private fun listenToFirestore() {
        if (firestore == null) return
        try {
            firestore.collection("videos")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("VideoRepository", "Firestore listening failed", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                AiVideo(
                                    id = doc.id,
                                    title = doc.getString("title") ?: "",
                                    description = doc.getString("description") ?: "",
                                    url = doc.getString("url") ?: "",
                                    thumbnailUrl = doc.getString("thumbnailUrl") ?: "",
                                    category = doc.getString("category") ?: "",
                                    durationSeconds = (doc.getLong("durationSeconds") ?: 30L).toInt(),
                                    viewsCount = (doc.getLong("viewsCount") ?: 0L).toInt(),
                                    downloadsCount = (doc.getLong("downloadsCount") ?: 0L).toInt(),
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                )
                            } catch (ex: Exception) {
                                null
                            }
                        }
                        uploadedVideosState.value = list
                    }
                }
        } catch (ex: Exception) {
            Log.e("VideoRepository", "Failed setting up Firestore listener", ex)
        }
    }

    fun getVideosFlow(category: String?, searchQuery: String?): Flow<List<AiVideo>> {
        return uploadedVideosState.map { uploadedList ->
            // Combine seed videos with uploaded videos
            val combinedList = uploadedList + seedVideos
            
            // Filter by category
            var filtered = if (category.isNullOrEmpty() || category == "All") {
                combinedList
            } else {
                combinedList.filter { it.category.equals(category, ignoreCase = true) }
            }

            // Filter by search query
            if (!searchQuery.isNullOrEmpty()) {
                filtered = filtered.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
                }
            }
            
            // Sort to show newer uploads first
            filtered.sortedByDescending { it.createdAt }
        }
    }

    // Favorites
    fun getFavoritesFlow(): Flow<List<AiVideo>> {
        return videoDao.getAllFavorites().map { entities ->
            entities.map { entity ->
                AiVideo(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    url = entity.url,
                    thumbnailUrl = entity.thumbnailUrl,
                    category = entity.category,
                    durationSeconds = entity.durationSeconds,
                    viewsCount = entity.viewsCount,
                    downloadsCount = entity.downloadsCount,
                    createdAt = entity.timestamp
                )
            }
        }
    }

    fun isFavorite(id: String): Flow<Boolean> = videoDao.isFavorite(id)

    suspend fun toggleFavorite(video: AiVideo, isFav: Boolean) {
        if (isFav) {
            videoDao.deleteFavorite(video.id)
        } else {
            videoDao.insertFavorite(video.toFavoriteEntity())
        }
    }

    // Recent Downloads
    fun getRecentDownloadsFlow(): Flow<List<AiVideo>> {
        return videoDao.getAllRecentDownloads().map { entities ->
            entities.map { entity ->
                AiVideo(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    url = entity.url,
                    thumbnailUrl = entity.thumbnailUrl,
                    category = entity.category,
                    durationSeconds = entity.durationSeconds,
                    viewsCount = entity.viewsCount,
                    downloadsCount = entity.downloadsCount,
                    createdAt = entity.timestamp
                )
            }
        }
    }

    suspend fun addRecentDownload(video: AiVideo) {
        videoDao.insertRecentDownload(video.toRecentDownloadEntity())
        
        // Try incrementing download count in Firestore if possible
        if (firestore != null) {
            try {
                firestore.collection("videos").document(video.id)
                    .update("downloadsCount", video.downloadsCount + 1)
            } catch (ex: Exception) {
                Log.w("VideoRepository", "Failed updating download count in Firestore", ex)
            }
        }
    }

    suspend fun clearDownloads() {
        videoDao.clearAllRecentDownloads()
    }

    // Upload Video (Admin Panel)
    suspend fun uploadVideo(
        title: String,
        description: String,
        videoUrl: String,
        thumbnailUrl: String,
        category: String,
        duration: Int
    ): Boolean {
        val id = "video_" + System.currentTimeMillis()
        val newVideo = AiVideo(
            id = id,
            title = title,
            description = description,
            url = videoUrl,
            thumbnailUrl = thumbnailUrl.ifEmpty { "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&auto=format&fit=crop" },
            category = category,
            durationSeconds = duration,
            viewsCount = 1,
            downloadsCount = 0,
            createdAt = System.currentTimeMillis()
        )

        // Add to local state first for immediate UI update in offline fallback
        uploadedVideosState.value = listOf(newVideo) + uploadedVideosState.value

        if (firestore != null) {
            try {
                val data = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "url" to videoUrl,
                    "thumbnailUrl" to newVideo.thumbnailUrl,
                    "category" to category,
                    "durationSeconds" to duration.toLong(),
                    "viewsCount" to 1L,
                    "downloadsCount" to 0L,
                    "createdAt" to newVideo.createdAt
                )
                firestore.collection("videos").document(id).set(data).await()
                return true
            } catch (e: Exception) {
                Log.e("VideoRepository", "Firestore upload failed", e)
                return false
            }
        }
        return true // Fallback succeeds immediately
    }

    // Modern Upload with Firebase Storage files support and Firestore metadata + custom tags list
    suspend fun uploadVideoWithFiles(
        title: String,
        description: String,
        videoUri: Uri?,
        videoUrlFallback: String,
        thumbnailUri: Uri?,
        thumbnailUrlFallback: String,
        category: String,
        duration: Int,
        tags: String,
        onProgress: (String) -> Unit
    ): Boolean {
        val id = "video_" + System.currentTimeMillis()
        var finalVideoUrl = videoUrlFallback
        var finalThumbnailUrl = thumbnailUrlFallback

        // Safe Firebase Storage access
        val storageInstance = try {
            FirebaseStorage.getInstance()
        } catch (e: Exception) {
            Log.w("VideoRepository", "Firebase Storage not initialized during upload: ${e.message}")
            null
        }

        if (storageInstance != null) {
            // Upload video file if provided
            if (videoUri != null) {
                try {
                    onProgress("Uploading video file to storage...")
                    val videoRef = storageInstance.reference.child("videos/$id.mp4")
                    videoRef.putFile(videoUri).await()
                    finalVideoUrl = videoRef.downloadUrl.await().toString()
                    Log.d("VideoRepository", "Video file uploaded successfully: $finalVideoUrl")
                } catch (e: Exception) {
                    Log.e("VideoRepository", "Failed uploading video file to storage", e)
                    onProgress("Failed uploading video file. Using direct URL.")
                }
            }

            // Upload thumbnail file if provided
            if (thumbnailUri != null) {
                try {
                    onProgress("Uploading thumbnail file to storage...")
                    val thumbRef = storageInstance.reference.child("thumbnails/$id.jpg")
                    thumbRef.putFile(thumbnailUri).await()
                    finalThumbnailUrl = thumbRef.downloadUrl.await().toString()
                    Log.d("VideoRepository", "Thumbnail file uploaded successfully: $finalThumbnailUrl")
                } catch (e: Exception) {
                    Log.e("VideoRepository", "Failed uploading thumbnail file to storage", e)
                    onProgress("Failed uploading thumbnail file. Using direct URL.")
                }
            }
        } else {
            onProgress("Firebase Storage offline. Using direct URLs.")
        }

        if (finalVideoUrl.isEmpty()) {
            finalVideoUrl = "https://assets.mixkit.co/videos/preview/mixkit-forest-stream-in-the-sunlight-529-large.mp4" // Fallback sample video
        }
        if (finalThumbnailUrl.isEmpty()) {
            finalThumbnailUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&auto=format&fit=crop" // Fallback sample thumbnail
        }

        val newVideo = AiVideo(
            id = id,
            title = title,
            description = description,
            url = finalVideoUrl,
            thumbnailUrl = finalThumbnailUrl,
            category = category,
            durationSeconds = duration,
            viewsCount = 1,
            downloadsCount = 0,
            createdAt = System.currentTimeMillis()
        )

        // Add to local state first for immediate UI update in offline/dev fallback
        uploadedVideosState.value = listOf(newVideo) + uploadedVideosState.value

        // Parse tags to list
        val tagsList = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        if (firestore != null) {
            try {
                onProgress("Saving video metadata to Firestore...")
                val data = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "url" to finalVideoUrl,
                    "thumbnailUrl" to finalThumbnailUrl,
                    "category" to category,
                    "durationSeconds" to duration.toLong(),
                    "viewsCount" to 1L,
                    "downloadsCount" to 0L,
                    "createdAt" to newVideo.createdAt,
                    "tags" to tagsList
                )
                firestore.collection("videos").document(id).set(data).await()
                return true
            } catch (e: Exception) {
                Log.e("VideoRepository", "Firestore metadata save failed", e)
                return false
            }
        }
        return true // Fallback succeeds immediately
    }
}
