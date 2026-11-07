package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Channel
import com.example.model.EpgProgram
import com.example.model.ParentalControlConfig
import com.example.model.PlaylistSource
import com.example.model.SourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class IptvRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("iptv_preferences", Context.MODE_PRIVATE)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    // In-memory state flows
    private val _sources = MutableStateFlow<List<PlaylistSource>>(emptyList())
    val sources: StateFlow<List<PlaylistSource>> = _sources.asStateFlow()

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _epgPrograms = MutableStateFlow<Map<String, List<EpgProgram>>>(emptyMap())
    val epgPrograms: StateFlow<Map<String, List<EpgProgram>>> = _epgPrograms.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _recentChannelIds = MutableStateFlow<List<String>>(emptyList())
    val recentChannelIds: StateFlow<List<String>> = _recentChannelIds.asStateFlow()

    private val _parentalConfig = MutableStateFlow(ParentalControlConfig())
    val parentalConfig: StateFlow<ParentalControlConfig> = _parentalConfig.asStateFlow()

    init {
        loadSavedData()
    }

    private fun loadSavedData() {
        // Load favorites
        val savedFavs = prefs.getStringSet("favorite_ids", emptySet()) ?: emptySet()
        _favorites.value = savedFavs

        // Load parental config
        val parentalEnabled = prefs.getBoolean("parental_enabled", false)
        val parentalPin = prefs.getString("parental_pin", "0000") ?: "0000"
        val lockedChannels = prefs.getStringSet("locked_channel_ids", emptySet()) ?: emptySet()
        val lockedCategories = prefs.getStringSet("locked_categories", setOf("Adult", "Mature")) ?: setOf("Adult", "Mature")
        _parentalConfig.value = ParentalControlConfig(
            isEnabled = parentalEnabled,
            pinHash = parentalPin,
            lockedCategories = lockedCategories,
            lockedChannelIds = lockedChannels
        )

        // Load recents
        val recentsJson = prefs.getString("recent_channels", "[]") ?: "[]"
        try {
            val arr = JSONArray(recentsJson)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) list.add(arr.getString(i))
            _recentChannelIds.value = list
        } catch (_: Exception) {}

        // Load initial channels
        val initialList = DefaultData.getInitialChannels().map { ch ->
            ch.copy(
                isFavorite = savedFavs.contains(ch.id),
                isLocked = lockedChannels.contains(ch.id) || lockedCategories.contains(ch.category)
            )
        }
        _channels.value = initialList
        _sources.value = listOf(DefaultData.defaultSource)

        // Generate dynamic EPG for initial channels
        val initialEpg = DefaultData.generateDynamicEpg(initialList)
        updateEpgPrograms(initialEpg)
    }

    fun updateEpgPrograms(programs: List<EpgProgram>) {
        val grouped = programs.groupBy { it.channelId }
        val current = _epgPrograms.value.toMutableMap()
        grouped.forEach { (chId, progList) ->
            current[chId] = progList.sortedBy { it.startEpochSeconds }
        }
        _epgPrograms.value = current
    }

    fun toggleFavorite(channelId: String) {
        val currentFavs = _favorites.value.toMutableSet()
        if (currentFavs.contains(channelId)) {
            currentFavs.remove(channelId)
        } else {
            currentFavs.add(channelId)
        }
        _favorites.value = currentFavs
        prefs.edit().putStringSet("favorite_ids", currentFavs).apply()

        // Update channel model
        _channels.value = _channels.value.map {
            if (it.id == channelId) it.copy(isFavorite = currentFavs.contains(channelId)) else it
        }
    }

    fun addRecentChannel(channelId: String) {
        val list = _recentChannelIds.value.toMutableList()
        list.remove(channelId)
        list.add(0, channelId)
        if (list.size > 20) list.removeAt(list.size - 1)
        _recentChannelIds.value = list

        val arr = JSONArray(list)
        prefs.edit().putString("recent_channels", arr.toString()).apply()
    }

    // --- Parental Controls ---

    fun setParentalEnabled(enabled: Boolean) {
        val current = _parentalConfig.value.copy(isEnabled = enabled)
        _parentalConfig.value = current
        prefs.edit().putBoolean("parental_enabled", enabled).apply()
        refreshChannelLockStatus()
    }

    fun updateParentalPin(newPin: String) {
        val current = _parentalConfig.value.copy(pinHash = newPin)
        _parentalConfig.value = current
        prefs.edit().putString("parental_pin", newPin).apply()
    }

    fun verifyPin(enteredPin: String): Boolean {
        return enteredPin == _parentalConfig.value.pinHash
    }

    fun toggleChannelLock(channelId: String) {
        val currentLocked = _parentalConfig.value.lockedChannelIds.toMutableSet()
        if (currentLocked.contains(channelId)) {
            currentLocked.remove(channelId)
        } else {
            currentLocked.add(channelId)
        }
        _parentalConfig.value = _parentalConfig.value.copy(lockedChannelIds = currentLocked)
        prefs.edit().putStringSet("locked_channel_ids", currentLocked).apply()
        refreshChannelLockStatus()
    }

    fun toggleCategoryLock(category: String) {
        val currentLocked = _parentalConfig.value.lockedCategories.toMutableSet()
        if (currentLocked.contains(category)) {
            currentLocked.remove(category)
        } else {
            currentLocked.add(category)
        }
        _parentalConfig.value = _parentalConfig.value.copy(lockedCategories = currentLocked)
        prefs.edit().putStringSet("locked_categories", currentLocked).apply()
        refreshChannelLockStatus()
    }

    fun unlockChannelForSession(channelId: String) {
        val sessionUnlocked = _parentalConfig.value.sessionUnlockedChannelIds + channelId
        _parentalConfig.value = _parentalConfig.value.copy(sessionUnlockedChannelIds = sessionUnlocked)
    }

    fun isChannelAccessible(channel: Channel): Boolean {
        if (!_parentalConfig.value.isEnabled) return true
        if (_parentalConfig.value.sessionUnlockedChannelIds.contains(channel.id)) return true
        if (_parentalConfig.value.lockedChannelIds.contains(channel.id)) return false
        if (_parentalConfig.value.lockedCategories.contains(channel.category)) return false
        return true
    }

    fun isCategoryLocked(category: String): Boolean {
        return _parentalConfig.value.isEnabled && _parentalConfig.value.lockedCategories.contains(category)
    }

    private fun refreshChannelLockStatus() {
        val config = _parentalConfig.value
        _channels.value = _channels.value.map { ch ->
            val locked = config.isEnabled && (config.lockedChannelIds.contains(ch.id) || config.lockedCategories.contains(ch.category))
            ch.copy(isLocked = locked)
        }
    }

    // --- Playlist & Account Imports ---

    suspend fun addM3uPlaylist(
        name: String,
        urlOrContent: String,
        isDirectContent: Boolean = false
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val content = if (isDirectContent) {
                urlOrContent
            } else {
                val req = Request.Builder().url(urlOrContent).build()
                val res = httpClient.newCall(req).execute()
                res.body?.string() ?: ""
            }

            if (content.isBlank()) return@withContext Result.failure(Exception("Empty playlist content"))

            val sourceId = "m3u_" + System.currentTimeMillis()
            val parseResult = M3uParser.parse(content, sourceId)

            if (parseResult.channels.isEmpty()) {
                return@withContext Result.failure(Exception("No valid streams found in M3U file"))
            }

            val newSource = PlaylistSource(
                id = sourceId,
                name = name.ifBlank { "M3U Playlist" },
                type = SourceType.M3U,
                url = if (!isDirectContent) urlOrContent else "",
                epgUrl = parseResult.epgUrl ?: "",
                channelCount = parseResult.channels.size
            )

            // Merge channels
            val updatedSources = _sources.value + newSource
            val updatedChannels = _channels.value + parseResult.channels
            _sources.value = updatedSources
            _channels.value = updatedChannels

            // Generate EPG for new channels or fetch XMLTV if URL present
            if (!parseResult.epgUrl.isNullOrBlank()) {
                fetchAndApplyXmltvEpg(parseResult.epgUrl)
            } else {
                val dynamicEpg = DefaultData.generateDynamicEpg(parseResult.channels)
                updateEpgPrograms(dynamicEpg)
            }

            refreshChannelLockStatus()
            Result.success(parseResult.channels.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addXtreamAccount(
        name: String,
        serverUrl: String,
        username: String,
        password: String
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val sourceId = "xtream_" + System.currentTimeMillis()
            val auth = XtreamCodesClient.loginAndLoad(serverUrl, username, password, sourceId)

            if (!auth.isValid) {
                return@withContext Result.failure(Exception(auth.message))
            }

            val newSource = PlaylistSource(
                id = sourceId,
                name = name.ifBlank { "Xtream ($username)" },
                type = SourceType.XTREAM,
                url = serverUrl,
                username = username,
                password = password,
                epgUrl = auth.epgUrl ?: "",
                channelCount = auth.channels.size
            )

            _sources.value = _sources.value + newSource
            _channels.value = _channels.value + auth.channels

            val dynamicEpg = DefaultData.generateDynamicEpg(auth.channels)
            updateEpgPrograms(dynamicEpg)

            refreshChannelLockStatus()
            Result.success(auth.channels.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addStalkerPortal(
        name: String,
        portalUrl: String,
        macAddress: String
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val sourceId = "stalker_" + System.currentTimeMillis()
            val auth = StalkerPortalClient.connectAndLoad(portalUrl, macAddress, "", sourceId)

            if (!auth.isValid) {
                return@withContext Result.failure(Exception(auth.message))
            }

            val newSource = PlaylistSource(
                id = sourceId,
                name = name.ifBlank { "Stalker Portal" },
                type = SourceType.STALKER,
                url = portalUrl,
                macAddress = macAddress,
                channelCount = auth.channels.size
            )

            _sources.value = _sources.value + newSource
            _channels.value = _channels.value + auth.channels

            val dynamicEpg = DefaultData.generateDynamicEpg(auth.channels)
            updateEpgPrograms(dynamicEpg)

            refreshChannelLockStatus()
            Result.success(auth.channels.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAndApplyXmltvEpg(epgUrlOrXml: String, isDirectXml: Boolean = false): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val content = if (isDirectXml) {
                epgUrlOrXml
            } else {
                val req = Request.Builder().url(epgUrlOrXml).build()
                val res = httpClient.newCall(req).execute()
                res.body?.string() ?: ""
            }

            if (content.isBlank()) return@withContext Result.failure(Exception("Empty XMLTV response"))

            val parsedPrograms = XmltvParser.parse(content)
            if (parsedPrograms.isNotEmpty()) {
                updateEpgPrograms(parsedPrograms)
                Result.success(parsedPrograms.size)
            } else {
                Result.failure(Exception("No programme tags found in XMLTV"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
