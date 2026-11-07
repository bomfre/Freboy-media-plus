package com.example.model

enum class SourceType {
    M3U,
    XTREAM,
    STALKER,
    BUILT_IN
}

data class PlaylistSource(
    val id: String,
    val name: String,
    val type: SourceType,
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val macAddress: String = "",
    val epgUrl: String = "",
    val channelCount: Int = 0,
    val isActive: Boolean = true,
    val addedAtEpoch: Long = System.currentTimeMillis()
)

data class Channel(
    val id: String,
    val channelNumber: Int,
    val name: String,
    val logoUrl: String = "",
    val streamUrl: String,
    val category: String = "General",
    val epgId: String = "",
    val resolution: String = "HD", // "4K", "FHD", "HD", "SD"
    val isFavorite: Boolean = false,
    val isLocked: Boolean = false,
    val sourceId: String = "built_in",
    val headers: Map<String, String> = emptyMap()
)

data class EpgProgram(
    val id: String,
    val channelId: String, // tvg-id or channel identifier
    val title: String,
    val description: String = "",
    val startEpochSeconds: Long,
    val endEpochSeconds: Long,
    val category: String = "",
    val rating: String = ""
) {
    val durationMinutes: Long
        get() = ((endEpochSeconds - startEpochSeconds) / 60).coerceAtLeast(1)

    fun isCurrentlyPlaying(currentEpochSeconds: Long = System.currentTimeMillis() / 1000): Boolean {
        return currentEpochSeconds in startEpochSeconds until endEpochSeconds
    }

    fun progressFraction(currentEpochSeconds: Long = System.currentTimeMillis() / 1000): Float {
        if (currentEpochSeconds <= startEpochSeconds) return 0f
        if (currentEpochSeconds >= endEpochSeconds) return 1f
        val total = (endEpochSeconds - startEpochSeconds).toFloat()
        if (total <= 0f) return 1f
        return ((currentEpochSeconds - startEpochSeconds) / total).coerceIn(0f, 1f)
    }
}

data class ParentalControlConfig(
    val isEnabled: Boolean = false,
    val pinHash: String = "0000", // Default PIN: 0000
    val lockedCategories: Set<String> = emptySet(),
    val lockedChannelIds: Set<String> = emptySet(),
    val sessionUnlockedChannelIds: Set<String> = emptySet()
)

enum class AspectRatioMode(val displayName: String) {
    FIT("Fit (16:9)"),
    FILL("Fill / Zoom"),
    STRETCH("Stretch")
}

data class PlayerState(
    val currentChannel: Channel? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val errorMessage: String? = null,
    val aspectRatio: AspectRatioMode = AspectRatioMode.FIT,
    val isPipActive: Boolean = false,
    val isFullScreen: Boolean = false,
    val volume: Float = 1.0f,
    val availableAudioTracks: List<String> = emptyList(),
    val selectedAudioTrackIndex: Int = 0,
    val availableSubtitleTracks: List<String> = emptyList(),
    val selectedSubtitleTrackIndex: Int = -1 // -1 is Off
)
