package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Channel
import com.example.model.EpgProgram
import com.example.ui.IptvViewModel
import com.example.ui.PinPromptTarget
import com.example.ui.components.AddSourceDialog
import com.example.ui.components.ChannelCard
import com.example.ui.components.EpgGuideSheet
import com.example.ui.components.ParentalControlSettingsDialog
import com.example.ui.components.PinDialog
import com.example.ui.components.PinDialogMode
import com.example.ui.player.MiniPlayerBar
import com.example.ui.player.VideoPlayerView
import com.example.ui.theme.AmberLive
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanSubtle
import com.example.ui.theme.RedLiveBadge
import com.example.ui.theme.SlateDark700
import com.example.ui.theme.SlateDark800
import com.example.ui.theme.SlateDark900
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: IptvViewModel,
    onTriggerPip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val channels by viewModel.filteredChannels.collectAsState()
    val allChannels by viewModel.channels.collectAsState()
    val sources by viewModel.sources.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val epgPrograms by viewModel.epgPrograms.collectAsState()
    val parentalConfig by viewModel.parentalConfig.collectAsState()

    val currentChannel by viewModel.currentChannel.collectAsState()
    val isPlayerVisible by viewModel.isPlayerVisible.collectAsState()
    val isPlayerFullScreen by viewModel.isPlayerFullScreen.collectAsState()
    val isInPipMode by viewModel.isInPipMode.collectAsState()

    val isPlayerPlaying by viewModel.isPlayerPlaying.collectAsState()
    val isPlayerBuffering by viewModel.isPlayerBuffering.collectAsState()
    val playerErrorMessage by viewModel.playerErrorMessage.collectAsState()

    val isProcessingSource by viewModel.isProcessingSource.collectAsState()
    val sourceStatusMessage by viewModel.sourceStatusMessage.collectAsState()
    val epgGuideChannel by viewModel.epgGuideChannel.collectAsState()
    val pinPromptTarget by viewModel.pinPromptTarget.collectAsState()

    var showAddSourceDialog by remember { mutableStateOf(false) }
    var showParentalSettingsDialog by remember { mutableStateOf(false) }

    // Intercept back button when player is full screen
    BackHandler(enabled = isPlayerFullScreen && !isInPipMode) {
        viewModel.minimizePlayer()
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Main Channel Guide Screen
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("main_iptv_screen"),
            containerColor = SlateDark900,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyanBright),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LiveTv,
                                    contentDescription = null,
                                    tint = SlateDark900,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Freboy Media",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = TextPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CyanSubtle)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "PLUS",
                                            color = CyanBright,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "${sources.size} Playlist Sources • ${allChannels.size} Channels",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    },
                    actions = {
                        // Parental Controls Button
                        IconButton(
                            onClick = { showParentalSettingsDialog = true },
                            modifier = Modifier.testTag("parental_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Parental Controls",
                                tint = if (parentalConfig.isEnabled) AmberLive else SlateMuted
                            )
                        }

                        // Add Source Button
                        Button(
                            onClick = { showAddSourceDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanBright),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("add_source_top_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = SlateDark900,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add Source",
                                color = SlateDark900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SlateDark900
                    )
                )
            },
            bottomBar = {
                // Mini Player Bar
                if (isPlayerVisible && currentChannel != null && !isPlayerFullScreen) {
                    val ch = currentChannel!!
                    val isLocked = !parentalConfig.sessionUnlockedChannelIds.contains(ch.id) &&
                            parentalConfig.isEnabled &&
                            (parentalConfig.lockedChannelIds.contains(ch.id) || parentalConfig.lockedCategories.contains(ch.category))

                    val currentEpg = epgPrograms[ch.epgId]?.firstOrNull { it.isCurrentlyPlaying() }
                        ?: epgPrograms[ch.id]?.firstOrNull { it.isCurrentlyPlaying() }

                    MiniPlayerBar(
                        channel = ch,
                        currentProgram = currentEpg,
                        isPlaying = isPlayerPlaying,
                        isLocked = isLocked,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onMaximize = { viewModel.maximizePlayer() },
                        onClose = { viewModel.closePlayer() }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Search Input
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                "Search channels, genres, numbers...",
                                color = SlateMuted,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = SlateMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = SlateMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("channel_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SlateDark800,
                            unfocusedContainerColor = SlateDark800,
                            focusedBorderColor = CyanBright,
                            unfocusedBorderColor = SlateDark700,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // Categories Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableCategories.forEach { category ->
                        val isSelected = selectedCategory == category
                        val isCategoryLocked = parentalConfig.isEnabled && parentalConfig.lockedCategories.contains(category)

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCategory(category) },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isCategoryLocked) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked Category",
                                            tint = AmberLive,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Text(
                                        text = category,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SlateDark800,
                                labelColor = TextPrimary,
                                selectedContainerColor = CyanBright,
                                selectedLabelColor = SlateDark900
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) CyanBright else SlateDark700,
                                enabled = true,
                                selected = isSelected
                            ),
                            modifier = Modifier.testTag("category_chip_$category")
                        )
                    }
                }

                // Channels List
                if (channels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = SlateMuted,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No channels match \"$searchQuery\"" else "No channels in this category",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your search or add a new M3U / Xtream / Stalker playlist.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("channels_list"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(channels, key = { it.id }) { channel ->
                            val isPlaying = currentChannel?.id == channel.id
                            val currentEpg = epgPrograms[channel.epgId]?.firstOrNull { it.isCurrentlyPlaying() }
                                ?: epgPrograms[channel.id]?.firstOrNull { it.isCurrentlyPlaying() }

                            ChannelCard(
                                channel = channel,
                                currentProgram = currentEpg,
                                isPlayingThisChannel = isPlaying,
                                onSelectChannel = { viewModel.selectChannel(it) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onOpenEpgGuide = { viewModel.openEpgGuide(it) },
                                onLockedClicked = { viewModel.selectChannel(it) }
                            )
                        }
                    }
                }
            }
        }

        // Fullscreen Video Player Mode (or PiP Mode)
        if (isPlayerFullScreen && currentChannel != null) {
            val ch = currentChannel!!
            val isLocked = !parentalConfig.sessionUnlockedChannelIds.contains(ch.id) &&
                    parentalConfig.isEnabled &&
                    (parentalConfig.lockedChannelIds.contains(ch.id) || parentalConfig.lockedCategories.contains(ch.category))

            val currentEpg = epgPrograms[ch.epgId]?.firstOrNull { it.isCurrentlyPlaying() }
                ?: epgPrograms[ch.id]?.firstOrNull { it.isCurrentlyPlaying() }

            VideoPlayerView(
                player = viewModel.getPlayer(),
                channel = ch,
                allChannels = allChannels,
                currentProgram = currentEpg,
                isPlaying = isPlayerPlaying,
                isBuffering = isPlayerBuffering,
                errorMessage = playerErrorMessage,
                isChannelLocked = isLocked,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSeekRelative = { viewModel.seekRelative(it) },
                onUnlockRequested = { viewModel.selectChannel(ch) },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onSelectNextChannel = { viewModel.onNextChannel() },
                onSelectPrevChannel = { viewModel.onPrevChannel() },
                onSelectChannel = { viewModel.selectChannel(it) },
                onEnterPip = onTriggerPip,
                onClosePlayer = { viewModel.minimizePlayer() },
                isInPipMode = isInPipMode,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // EPG Guide Timeline Dialog Sheet
    epgGuideChannel?.let { guideCh ->
        val progs = epgPrograms[guideCh.epgId] ?: epgPrograms[guideCh.id] ?: emptyList()
        EpgGuideSheet(
            channel = guideCh,
            programs = progs,
            onPlayChannel = { viewModel.selectChannel(it) },
            onDismiss = { viewModel.closeEpgGuide() }
        )
    }

    // Add Source Dialog (M3U, Xtream, Stalker, XMLTV)
    if (showAddSourceDialog) {
        AddSourceDialog(
            isProcessing = isProcessingSource,
            statusMessage = sourceStatusMessage,
            onAddM3u = { name, url, isDirect ->
                viewModel.addM3uPlaylist(name, url, isDirect)
            },
            onAddXtream = { name, sUrl, user, pass ->
                viewModel.addXtreamAccount(name, sUrl, user, pass)
            },
            onAddStalker = { name, pUrl, mac ->
                viewModel.addStalkerPortal(name, pUrl, mac)
            },
            onAddEpg = { epg, isDirect ->
                viewModel.addEpgGuide(epg, isDirect)
            },
            onDismiss = {
                showAddSourceDialog = false
                viewModel.clearSourceStatusMessage()
            }
        )
    }

    // Parental Controls Settings Dialog
    if (showParentalSettingsDialog) {
        ParentalControlSettingsDialog(
            config = parentalConfig,
            availableCategories = availableCategories.filter { it != "All Channels" && it != "⭐ Favorites" && it != "🕒 Recent" },
            onToggleEnabled = { viewModel.setParentalEnabled(it) },
            onChangePin = { viewModel.updateParentalPin(it) },
            onToggleCategoryLock = { viewModel.toggleCategoryLock(it) },
            onDismiss = { showParentalSettingsDialog = false }
        )
    }

    // Parental PIN Entry Prompt Dialog
    pinPromptTarget?.let { target ->
        val promptTitle = when (target) {
            is PinPromptTarget.UnlockChannel -> "Unlock Channel #${target.channel.channelNumber}"
            is PinPromptTarget.UnlockCategory -> "Unlock \"${target.category}\""
            is PinPromptTarget.OpenParentalSettings -> "Enter Parental PIN"
        }

        val promptSubtitle = when (target) {
            is PinPromptTarget.UnlockChannel -> "${target.channel.name} is restricted by Parental Controls."
            is PinPromptTarget.UnlockCategory -> "Category \"${target.category}\" requires PIN verification."
            is PinPromptTarget.OpenParentalSettings -> "Enter your 4-digit PIN to access parental settings."
        }

        PinDialog(
            mode = PinDialogMode.UNLOCK_CONTENT,
            title = promptTitle,
            subtitle = promptSubtitle,
            onVerifyPin = { enteredPin ->
                viewModel.verifyPin(enteredPin)
            },
            onSuccess = { validPin ->
                viewModel.handlePinSuccess(validPin)
            },
            onDismiss = { viewModel.dismissPinPrompt() }
        )
    }
}
