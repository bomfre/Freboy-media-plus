package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.data.IptvRepository
import com.example.model.Channel
import com.example.model.EpgProgram
import com.example.model.ParentalControlConfig
import com.example.model.PlaylistSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class PinPromptTarget {
    data class UnlockChannel(val channel: Channel) : PinPromptTarget()
    data class UnlockCategory(val category: String) : PinPromptTarget()
    object OpenParentalSettings : PinPromptTarget()
}

class IptvViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = IptvRepository(application.applicationContext)

    val sources: StateFlow<List<PlaylistSource>> = repository.sources
    val channels: StateFlow<List<Channel>> = repository.channels
    val epgPrograms: StateFlow<Map<String, List<EpgProgram>>> = repository.epgPrograms
    val favorites: StateFlow<Set<String>> = repository.favorites
    val recentChannelIds: StateFlow<List<String>> = repository.recentChannelIds
    val parentalConfig: StateFlow<ParentalControlConfig> = repository.parentalConfig

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All Channels")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _currentChannel = MutableStateFlow<Channel?>(null)
    val currentChannel: StateFlow<Channel?> = _currentChannel.asStateFlow()

    private val _isPlayerVisible = MutableStateFlow(false)
    val isPlayerVisible: StateFlow<Boolean> = _isPlayerVisible.asStateFlow()

    private val _isPlayerFullScreen = MutableStateFlow(false)
    val isPlayerFullScreen: StateFlow<Boolean> = _isPlayerFullScreen.asStateFlow()

    private val _isInPipMode = MutableStateFlow(false)
    val isInPipMode: StateFlow<Boolean> = _isInPipMode.asStateFlow()

    private val _isPlayerPlaying = MutableStateFlow(false)
    val isPlayerPlaying: StateFlow<Boolean> = _isPlayerPlaying.asStateFlow()

    private val _isPlayerBuffering = MutableStateFlow(false)
    val isPlayerBuffering: StateFlow<Boolean> = _isPlayerBuffering.asStateFlow()

    private val _playerErrorMessage = MutableStateFlow<String?>(null)
    val playerErrorMessage: StateFlow<String?> = _playerErrorMessage.asStateFlow()

    private val _isProcessingSource = MutableStateFlow(false)
    val isProcessingSource: StateFlow<Boolean> = _isProcessingSource.asStateFlow()

    private val _sourceStatusMessage = MutableStateFlow<String?>(null)
    val sourceStatusMessage: StateFlow<String?> = _sourceStatusMessage.asStateFlow()

    private val _epgGuideChannel = MutableStateFlow<Channel?>(null)
    val epgGuideChannel: StateFlow<Channel?> = _epgGuideChannel.asStateFlow()

    private val _pinPromptTarget = MutableStateFlow<PinPromptTarget?>(null)
    val pinPromptTarget: StateFlow<PinPromptTarget?> = _pinPromptTarget.asStateFlow()

    // Persistent ExoPlayer instance across full screen, mini player, and PiP
    private var exoPlayerInstance: ExoPlayer? = null

    fun getPlayer(): ExoPlayer {
        if (exoPlayerInstance == null) {
            val context: Application = getApplication()
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("freboy-media-plus/2.0 (Android; ExoPlayer)")
                .setAllowCrossProtocolRedirects(true)

            val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory)

            exoPlayerInstance = ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build()
                .apply {
                    repeatMode = Player.REPEAT_MODE_OFF
                    playWhenReady = true
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            when (playbackState) {
                                Player.STATE_BUFFERING -> _isPlayerBuffering.value = true
                                Player.STATE_READY -> {
                                    _isPlayerBuffering.value = false
                                    _playerErrorMessage.value = null
                                }
                                Player.STATE_ENDED -> _isPlayerBuffering.value = false
                                Player.STATE_IDLE -> {}
                            }
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlayerPlaying.value = isPlaying
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            _isPlayerBuffering.value = false
                            _playerErrorMessage.value = "Playback error: ${error.errorCodeName}. Retrying..."
                            viewModelScope.launch {
                                delay(2500)
                                if (_currentChannel.value != null && isChannelAccessible(_currentChannel.value!!)) {
                                    exoPlayerInstance?.prepare()
                                    exoPlayerInstance?.play()
                                }
                            }
                        }
                    })
                }
        }
        return exoPlayerInstance!!
    }

    // Derived filtered channels
    val filteredChannels: StateFlow<List<Channel>> = combine(
        channels,
        selectedCategory,
        searchQuery,
        favorites,
        recentChannelIds
    ) { chList, category, query, favs, recents ->
        var list = chList

        // Category filter
        list = when (category) {
            "All Channels" -> list
            "⭐ Favorites" -> list.filter { favs.contains(it.id) }
            "🕒 Recent" -> {
                val recMap = recents.withIndex().associate { it.value to it.index }
                list.filter { recMap.containsKey(it.id) }.sortedBy { recMap[it.id] ?: 999 }
            }
            else -> list.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Search query filter
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.channelNumber.toString() == q
            }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All unique categories for filtering
    val availableCategories: StateFlow<List<String>> = channels.combine(favorites) { chList, favs ->
        val cats = chList.map { it.category }.distinct().filter { it.isNotBlank() }.sorted()
        val base = mutableListOf("All Channels")
        if (favs.isNotEmpty()) base.add("⭐ Favorites")
        base.add("🕒 Recent")
        base.addAll(cats)
        base
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All Channels", "🕒 Recent"))

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        if (repository.isCategoryLocked(category)) {
            _pinPromptTarget.value = PinPromptTarget.UnlockCategory(category)
        } else {
            _selectedCategory.value = category
        }
    }

    fun isChannelAccessible(channel: Channel): Boolean {
        return repository.isChannelAccessible(channel)
    }

    fun selectChannel(channel: Channel) {
        if (!repository.isChannelAccessible(channel)) {
            _pinPromptTarget.value = PinPromptTarget.UnlockChannel(channel)
        } else {
            playChannelImmediately(channel)
        }
    }

    private fun playChannelImmediately(channel: Channel) {
        _currentChannel.value = channel
        _isPlayerVisible.value = true
        _isPlayerFullScreen.value = true
        _playerErrorMessage.value = null
        _isPlayerBuffering.value = true
        repository.addRecentChannel(channel.id)

        val player = getPlayer()
        try {
            val mediaItem = MediaItem.fromUri(channel.streamUrl)
            player.stop()
            player.clearMediaItems()
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            _isPlayerPlaying.value = true
        } catch (e: Exception) {
            _playerErrorMessage.value = "Failed to load channel: ${e.message}"
            _isPlayerBuffering.value = false
        }
    }

    fun togglePlayPause() {
        exoPlayerInstance?.let { p ->
            if (p.isPlaying) {
                p.pause()
            } else {
                p.play()
            }
        }
    }

    fun seekRelative(offsetMillis: Long) {
        exoPlayerInstance?.let { p ->
            val pos = p.currentPosition
            p.seekTo((pos + offsetMillis).coerceAtLeast(0))
        }
    }

    fun onNextChannel() {
        val current = _currentChannel.value ?: return
        val list = channels.value
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && list.isNotEmpty()) {
            val nextIndex = (currentIndex + 1) % list.size
            selectChannel(list[nextIndex])
        }
    }

    fun onPrevChannel() {
        val current = _currentChannel.value ?: return
        val list = channels.value
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && list.isNotEmpty()) {
            val prevIndex = if (currentIndex - 1 < 0) list.size - 1 else currentIndex - 1
            selectChannel(list[prevIndex])
        }
    }

    fun toggleFavorite(channel: Channel) {
        repository.toggleFavorite(channel.id)
    }

    fun openEpgGuide(channel: Channel) {
        _epgGuideChannel.value = channel
    }

    fun closeEpgGuide() {
        _epgGuideChannel.value = null
    }

    fun minimizePlayer() {
        _isPlayerFullScreen.value = false
    }

    fun maximizePlayer() {
        _isPlayerFullScreen.value = true
    }

    fun closePlayer() {
        exoPlayerInstance?.stop()
        exoPlayerInstance?.clearMediaItems()
        _isPlayerVisible.value = false
        _isPlayerFullScreen.value = false
        _currentChannel.value = null
        _isPlayerPlaying.value = false
        _isPlayerBuffering.value = false
    }

    fun setInPipMode(inPip: Boolean) {
        _isInPipMode.value = inPip
        if (inPip) {
            _isPlayerFullScreen.value = true
        }
    }

    // --- Parental Controls & PIN verification ---

    fun verifyPin(pin: String): Boolean {
        return repository.verifyPin(pin)
    }

    fun handlePinSuccess(pin: String) {
        when (val target = _pinPromptTarget.value) {
            is PinPromptTarget.UnlockChannel -> {
                repository.unlockChannelForSession(target.channel.id)
                playChannelImmediately(target.channel)
                _pinPromptTarget.value = null
            }
            is PinPromptTarget.UnlockCategory -> {
                _selectedCategory.value = target.category
                _pinPromptTarget.value = null
            }
            is PinPromptTarget.OpenParentalSettings -> {
                _pinPromptTarget.value = null
            }
            null -> {}
        }
    }

    fun dismissPinPrompt() {
        _pinPromptTarget.value = null
    }

    fun setParentalEnabled(enabled: Boolean) {
        repository.setParentalEnabled(enabled)
        _currentChannel.value?.let { ch ->
            if (!repository.isChannelAccessible(ch)) {
                exoPlayerInstance?.pause()
            }
        }
    }

    fun updateParentalPin(newPin: String) {
        repository.updateParentalPin(newPin)
    }

    fun toggleCategoryLock(category: String) {
        repository.toggleCategoryLock(category)
        _currentChannel.value?.let { ch ->
            if (!repository.isChannelAccessible(ch)) {
                exoPlayerInstance?.pause()
            }
        }
    }

    // --- Source Imports ---

    fun addM3uPlaylist(name: String, urlOrContent: String, isDirectContent: Boolean) {
        viewModelScope.launch {
            _isProcessingSource.value = true
            _sourceStatusMessage.value = "Parsing M3U playlist..."
            val result = repository.addM3uPlaylist(name, urlOrContent, isDirectContent)
            _isProcessingSource.value = false
            if (result.isSuccess) {
                _sourceStatusMessage.value = "Successfully imported ${result.getOrNull()} channels!"
            } else {
                _sourceStatusMessage.value = "Import error: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun addXtreamAccount(name: String, serverUrl: String, user: String, pass: String) {
        viewModelScope.launch {
            _isProcessingSource.value = true
            _sourceStatusMessage.value = "Connecting to Xtream Codes server..."
            val result = repository.addXtreamAccount(name, serverUrl, user, pass)
            _isProcessingSource.value = false
            if (result.isSuccess) {
                _sourceStatusMessage.value = "Xtream connected! Loaded ${result.getOrNull()} channels."
            } else {
                _sourceStatusMessage.value = "Xtream error: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun addStalkerPortal(name: String, portalUrl: String, mac: String) {
        viewModelScope.launch {
            _isProcessingSource.value = true
            _sourceStatusMessage.value = "Performing Stalker MAC handshake..."
            val result = repository.addStalkerPortal(name, portalUrl, mac)
            _isProcessingSource.value = false
            if (result.isSuccess) {
                _sourceStatusMessage.value = "Stalker portal linked! Loaded ${result.getOrNull()} channels."
            } else {
                _sourceStatusMessage.value = "Stalker error: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun addEpgGuide(epgUrlOrXml: String, isDirectXml: Boolean) {
        viewModelScope.launch {
            _isProcessingSource.value = true
            _sourceStatusMessage.value = "Parsing XMLTV EPG data..."
            val result = repository.fetchAndApplyXmltvEpg(epgUrlOrXml, isDirectXml)
            _isProcessingSource.value = false
            if (result.isSuccess) {
                _sourceStatusMessage.value = "EPG updated with ${result.getOrNull()} programs!"
            } else {
                _sourceStatusMessage.value = "EPG load error: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearSourceStatusMessage() {
        _sourceStatusMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayerInstance?.release()
        exoPlayerInstance = null
    }
}
