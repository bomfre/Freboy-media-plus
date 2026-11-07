package com.example.ui.player

import android.view.LayoutInflater
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.R
import com.example.model.AspectRatioMode
import com.example.model.Channel
import com.example.model.EpgProgram
import com.example.ui.theme.AmberLive
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanSubtle
import com.example.ui.theme.RedLiveBadge
import com.example.ui.theme.SlateDark800
import com.example.ui.theme.SlateDark900
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerView(
    player: ExoPlayer,
    channel: Channel,
    allChannels: List<Channel>,
    currentProgram: EpgProgram?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    errorMessage: String?,
    isChannelLocked: Boolean,
    onTogglePlayPause: () -> Unit,
    onSeekRelative: (Long) -> Unit,
    onUnlockRequested: () -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onSelectNextChannel: () -> Unit,
    onSelectPrevChannel: () -> Unit,
    onSelectChannel: (Channel) -> Unit,
    onEnterPip: () -> Unit,
    onClosePlayer: () -> Unit,
    isInPipMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showControls by remember { mutableStateOf(!isInPipMode) }
    var showQuickDrawer by remember { mutableStateOf(false) }
    var showTrackDialog by remember { mutableStateOf(false) }
    var aspectRatioMode by remember { mutableStateOf(AspectRatioMode.FIT) }

    var selectedAudioTrack by remember { mutableIntStateOf(0) }
    var selectedSubtitleTrack by remember { mutableIntStateOf(-1) }

    // Auto-hide controls timer
    LaunchedEffect(showControls, isInPipMode) {
        if (showControls && !isInPipMode) {
            delay(4000)
            showControls = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("video_player_container")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!isInPipMode) {
                    showControls = !showControls
                }
            }
    ) {
        // Video Surface using layout with texture_view (avoids SurfaceSyncGroup issues)
        if (!isChannelLocked) {
            AndroidView(
                factory = { ctx ->
                    val playerView = LayoutInflater.from(ctx).inflate(R.layout.layout_player_view, null, false) as PlayerView
                    playerView.player = player
                    playerView.useController = false
                    playerView.resizeMode = when (aspectRatioMode) {
                        AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        AspectRatioMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    }
                    playerView
                },
                update = { playerView ->
                    playerView.player = player
                    playerView.resizeMode = when (aspectRatioMode) {
                        AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        AspectRatioMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            DisposableEffect(Unit) {
                onDispose {
                    // Detach player when leaving full screen
                }
            }
        }

        // Parental Control Lock Overlay
        if (isChannelLocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SlateDark900.copy(alpha = 0.96f))
                    .padding(24.dp)
                    .testTag("player_locked_overlay"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AmberLive.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Content Locked",
                            tint = AmberLive,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Content Restricted by Parental Control",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Channel #${channel.channelNumber} ${channel.name} (${channel.category}) is protected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onUnlockRequested,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanBright),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("unlock_channel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = SlateDark900,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enter PIN to Unlock",
                            color = SlateDark900,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Buffering Indicator
        if (isBuffering && !isChannelLocked) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = CyanBright,
                    strokeWidth = 3.dp
                )
            }
        }

        // Error message banner
        errorMessage?.let { msg ->
            if (!isChannelLocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = msg,
                        color = AmberLive,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Controls HUD Overlay (Hidden in PiP mode)
        if (!isInPipMode) {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.75f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = onClosePlayer,
                                modifier = Modifier.testTag("player_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = TextPrimary
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "#${channel.channelNumber} ${channel.name}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CyanSubtle)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = channel.resolution,
                                            color = CyanBright,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(RedLiveBadge)
                                    )
                                    Text(
                                        text = "LIVE • ${channel.category}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SlateMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Top right quick actions: Aspect ratio, Audio tracks, PiP, Channel drawer
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Aspect ratio toggle
                            IconButton(
                                onClick = {
                                    aspectRatioMode = when (aspectRatioMode) {
                                        AspectRatioMode.FIT -> AspectRatioMode.FILL
                                        AspectRatioMode.FILL -> AspectRatioMode.STRETCH
                                        AspectRatioMode.STRETCH -> AspectRatioMode.FIT
                                    }
                                },
                                modifier = Modifier.testTag("aspect_ratio_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AspectRatio,
                                    contentDescription = aspectRatioMode.displayName,
                                    tint = CyanBright
                                )
                            }

                            // Audio & Subtitles
                            IconButton(
                                onClick = { showTrackDialog = true },
                                modifier = Modifier.testTag("audio_tracks_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Audiotrack,
                                    contentDescription = "Tracks",
                                    tint = TextPrimary
                                )
                            }

                            // Picture in Picture
                            IconButton(
                                onClick = onEnterPip,
                                modifier = Modifier.testTag("pip_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureInPictureAlt,
                                    contentDescription = "Picture in Picture",
                                    tint = CyanBright
                                )
                            }

                            // Quick Channel Drawer
                            IconButton(
                                onClick = { showQuickDrawer = !showQuickDrawer },
                                modifier = Modifier.testTag("quick_channel_drawer_toggle")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = "Channels",
                                    tint = TextPrimary
                                )
                            }
                        }
                    }

                    // Center Playback Controls
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Prev Channel
                        IconButton(
                            onClick = onSelectPrevChannel,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .testTag("prev_channel_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Channel",
                                tint = TextPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Rewind 10s
                        IconButton(
                            onClick = { onSeekRelative(-10000) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .testTag("rewind_10_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Rewind 10s",
                                tint = TextPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Play/Pause
                        IconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(CyanBright)
                                .testTag("player_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = SlateDark900,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Forward 10s
                        IconButton(
                            onClick = { onSeekRelative(10000) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .testTag("forward_10_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Forward 10s",
                                tint = TextPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Next Channel
                        IconButton(
                            onClick = onSelectNextChannel,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .testTag("next_channel_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Channel",
                                tint = TextPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Bottom Bar: Now playing program guide info
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        currentProgram?.let { prog ->
                            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                            val startTime = timeFormat.format(Date(prog.startEpochSeconds * 1000))
                            val endTime = timeFormat.format(Date(prog.endEpochSeconds * 1000))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prog.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "$startTime - $endTime • ${prog.category}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SlateMuted
                                    )
                                }

                                IconButton(
                                    onClick = { onToggleFavorite(channel) },
                                    modifier = Modifier.testTag("player_favorite_button")
                                ) {
                                    Icon(
                                        imageVector = if (channel.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "Favorite",
                                        tint = if (channel.isFavorite) AmberLive else TextPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            val progress = prog.progressFraction(System.currentTimeMillis() / 1000)
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = CyanBright,
                                trackColor = SlateDark800
                            )
                        }
                    }
                }
            }
        }

        // Quick Channel Switcher Drawer
        if (showQuickDrawer && !isInPipMode) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
            ) {
                QuickChannelDrawer(
                    channels = allChannels,
                    currentChannel = channel,
                    onSelectChannel = { newCh ->
                        onSelectChannel(newCh)
                        showQuickDrawer = false
                    },
                    onClose = { showQuickDrawer = false }
                )
            }
        }

        // Track selector dialog
        if (showTrackDialog) {
            TrackSelectionDialog(
                audioTracks = listOf("Stereo Track 1 (Original)", "Surround 5.1 (Dolby)", "Commentary Track"),
                selectedAudioIndex = selectedAudioTrack,
                onSelectAudio = { selectedAudioTrack = it },
                subtitleTracks = listOf("English [CC]", "Spanish", "French", "German"),
                selectedSubtitleIndex = selectedSubtitleTrack,
                onSelectSubtitle = { selectedSubtitleTrack = it },
                onDismiss = { showTrackDialog = false }
            )
        }
    }
}
