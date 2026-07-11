package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AiVideo
import com.example.data.repository.AuthRepository
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val videoRepository = VideoRepository(database.videoDao())
    val authRepository = AuthRepository()

    val currentUser = authRepository.currentUser

    // UI inputs
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Pagination / Infinite scrolling state
    private val _visibleLimit = MutableStateFlow(6)
    val visibleLimit: StateFlow<Int> = _visibleLimit.asStateFlow()

    private val _isInfiniteLoading = MutableStateFlow(false)
    val isInfiniteLoading: StateFlow<Boolean> = _isInfiniteLoading.asStateFlow()

    // Download simulation state
    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress: StateFlow<Int> = _downloadProgress.asStateFlow()

    private val _downloadingVideoId = MutableStateFlow<String?>(null)
    val downloadingVideoId: StateFlow<String?> = _downloadingVideoId.asStateFlow()

    // Videos list flow matching category & search
    val videos: StateFlow<List<AiVideo>> = combine(
        _selectedCategory,
        _searchQuery
    ) { cat, search ->
        Pair(cat, search)
    }.flatMapLatest { (cat, search) ->
        videoRepository.getVideosFlow(cat, search)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Paginated/Limited videos flow for home feed
    val paginatedVideos: StateFlow<List<AiVideo>> = combine(
        videos,
        _visibleLimit
    ) { list, limit ->
        list.take(limit)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Favorites
    val favorites: StateFlow<List<AiVideo>> = videoRepository.getFavoritesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Recent Downloads
    val recentDownloads: StateFlow<List<AiVideo>> = videoRepository.getRecentDownloadsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Actions
    fun setCategory(category: String) {
        _selectedCategory.value = category
        _visibleLimit.value = 6 // Reset limit on category switch
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _visibleLimit.value = 6 // Reset limit on search
    }

    fun loadMoreVideos() {
        if (_isInfiniteLoading.value) return
        val currentTotal = videos.value.size
        val currentLimit = _visibleLimit.value
        if (currentLimit >= currentTotal) return // No more videos to load

        viewModelScope.launch {
            _isInfiniteLoading.value = true
            delay(1200) // Beautiful simulated loading delay
            _visibleLimit.value = currentLimit + 6
            _isInfiniteLoading.value = false
        }
    }

    fun toggleFavorite(video: AiVideo, isFav: Boolean) {
        viewModelScope.launch {
            videoRepository.toggleFavorite(video, isFav)
        }
    }

    fun isFavorite(id: String): Flow<Boolean> {
        return videoRepository.isFavorite(id)
    }

    // Download Video Simulation
    fun downloadVideo(video: AiVideo) {
        if (_downloadingVideoId.value != null) return // Only download one at a time
        viewModelScope.launch {
            _downloadingVideoId.value = video.id
            _downloadProgress.value = 0
            while (_downloadProgress.value < 100) {
                delay(100)
                _downloadProgress.value += (10..25).random().coerceAtMost(100 - _downloadProgress.value)
            }
            // Finished!
            videoRepository.addRecentDownload(video)
            delay(500)
            _downloadingVideoId.value = null
            _downloadProgress.value = 0
        }
    }

    fun clearDownloads() {
        viewModelScope.launch {
            videoRepository.clearDownloads()
        }
    }

    // Upload (Admin Panel)
    private val _uploadStatus = MutableStateFlow<String?>(null)
    val uploadStatus: StateFlow<String?> = _uploadStatus.asStateFlow()

    fun uploadVideo(
        title: String,
        description: String,
        videoUrl: String,
        thumbnailUrl: String,
        category: String,
        duration: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uploadStatus.value = "Uploading to Video Vault..."
            delay(2000) // Beautiful upload animation
            val success = videoRepository.uploadVideo(
                title = title,
                description = description,
                videoUrl = videoUrl,
                thumbnailUrl = thumbnailUrl,
                category = category,
                duration = duration
            )
            if (success) {
                _uploadStatus.value = "Success! Video uploaded."
                onSuccess()
                delay(1500)
                _uploadStatus.value = null
            } else {
                _uploadStatus.value = "Error uploading video to cloud."
                delay(1500)
                _uploadStatus.value = null
            }
        }
    }

    fun uploadVideoWithFiles(
        title: String,
        description: String,
        videoUri: Uri?,
        videoUrlFallback: String,
        thumbnailUri: Uri?,
        thumbnailUrlFallback: String,
        category: String,
        duration: Int,
        tags: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uploadStatus.value = "Starting secure media upload..."
            val success = videoRepository.uploadVideoWithFiles(
                title = title,
                description = description,
                videoUri = videoUri,
                videoUrlFallback = videoUrlFallback,
                thumbnailUri = thumbnailUri,
                thumbnailUrlFallback = thumbnailUrlFallback,
                category = category,
                duration = duration,
                tags = tags,
                onProgress = { statusMessage ->
                    _uploadStatus.value = statusMessage
                }
            )
            if (success) {
                _uploadStatus.value = "Success! Video published to cloud."
                onSuccess()
                delay(2000)
                _uploadStatus.value = null
            } else {
                _uploadStatus.value = "Error during file upload process."
                delay(2000)
                _uploadStatus.value = null
            }
        }
    }

    fun setSimulatedAdmin(enabled: Boolean) {
        authRepository.setSimulatedAdmin(enabled)
    }
}
