package com.example.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanBright
import com.example.ui.theme.SlateDark800
import com.example.ui.theme.SlateDark900
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.TextPrimary

@Composable
fun TrackSelectionDialog(
    audioTracks: List<String>,
    selectedAudioIndex: Int,
    onSelectAudio: (Int) -> Unit,
    subtitleTracks: List<String>,
    selectedSubtitleIndex: Int,
    onSelectSubtitle: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("track_selection_dialog"),
        containerColor = SlateDark800,
        titleContentColor = TextPrimary,
        textContentColor = SlateMuted,
        title = {
            Text(
                text = "Audio & Subtitle Tracks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SlateDark900,
                    contentColor = CyanBright
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Audiotrack, contentDescription = null)
                                Text("Audio (${audioTracks.size})")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Subtitles, contentDescription = null)
                                Text("Subtitles")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTab == 0) {
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        val tracks = if (audioTracks.isNotEmpty()) audioTracks else listOf("Stereo Track 1 (Default)", "Surround 5.1")
                        itemsIndexed(tracks) { index, trackName ->
                            val isSelected = index == selectedAudioIndex
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectAudio(index) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectAudio(index) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = CyanBright,
                                        unselectedColor = SlateMuted
                                    )
                                )
                                Text(
                                    text = trackName,
                                    color = if (isSelected) CyanBright else TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        // Off option
                        item {
                            val isOff = selectedSubtitleIndex == -1
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectSubtitle(-1) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isOff,
                                    onClick = { onSelectSubtitle(-1) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = CyanBright,
                                        unselectedColor = SlateMuted
                                    )
                                )
                                Text(
                                    text = "Off",
                                    color = if (isOff) CyanBright else TextPrimary,
                                    fontWeight = if (isOff) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        val subs = if (subtitleTracks.isNotEmpty()) subtitleTracks else listOf("English Closed Captions", "Spanish", "French")
                        itemsIndexed(subs) { index, subName ->
                            val isSelected = index == selectedSubtitleIndex
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectSubtitle(index) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectSubtitle(index) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = CyanBright,
                                        unselectedColor = SlateMuted
                                    )
                                )
                                Text(
                                    text = subName,
                                    color = if (isSelected) CyanBright else TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = CyanBright, fontWeight = FontWeight.Bold)
            }
        }
    )
}
