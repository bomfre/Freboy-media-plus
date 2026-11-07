package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Dvr
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DefaultData
import com.example.ui.theme.CyanBright
import com.example.ui.theme.GreenActive
import com.example.ui.theme.RedLiveBadge
import com.example.ui.theme.SlateDark700
import com.example.ui.theme.SlateDark800
import com.example.ui.theme.SlateDark900
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.TextPrimary

@Composable
fun AddSourceDialog(
    isProcessing: Boolean,
    statusMessage: String?,
    onAddM3u: (name: String, urlOrContent: String, isDirectContent: Boolean) -> Unit,
    onAddXtream: (name: String, serverUrl: String, user: String, pass: String) -> Unit,
    onAddStalker: (name: String, portalUrl: String, mac: String) -> Unit,
    onAddEpg: (epgUrlOrXml: String, isDirectXml: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // M3U form
    var m3uName by remember { mutableStateOf("") }
    var m3uUrl by remember { mutableStateOf("") }
    var isDirectM3uContent by remember { mutableStateOf(false) }

    // Xtream form
    var xtreamName by remember { mutableStateOf("") }
    var xtreamServer by remember { mutableStateOf("") }
    var xtreamUser by remember { mutableStateOf("") }
    var xtreamPass by remember { mutableStateOf("") }

    // Stalker form
    var stalkerName by remember { mutableStateOf("") }
    var stalkerUrl by remember { mutableStateOf("") }
    var stalkerMac by remember { mutableStateOf("") }

    // EPG form
    var epgUrlOrContent by remember { mutableStateOf("") }
    var isDirectEpgXml by remember { mutableStateOf(false) }

    val tabs = listOf("M3U Playlist", "Xtream Codes", "Stalker Portal", "XMLTV EPG")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_source_dialog"),
        containerColor = SlateDark800,
        titleContentColor = TextPrimary,
        textContentColor = SlateMuted,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddLink,
                    contentDescription = null,
                    tint = CyanBright,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Add IPTV Source",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
            ) {
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SlateDark900,
                    contentColor = CyanBright,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CyanBright
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) CyanBright else SlateMuted,
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.testTag("source_tab_$index")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> {
                            // M3U Playlist Tab
                            Text(
                                text = "Load M3U / M3U8 Playlist",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Enter an online M3U link or paste raw playlist text with EXTINF tags.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = m3uName,
                                onValueChange = { m3uName = it },
                                label = { Text("Playlist Name") },
                                placeholder = { Text("e.g. My Sports M3U") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("m3u_name_input"),
                                colors = iptvTextFieldColors()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = m3uUrl,
                                onValueChange = { m3uUrl = it },
                                label = { Text(if (isDirectM3uContent) "Paste M3U Content" else "M3U URL (.m3u/.m3u8)") },
                                placeholder = { Text(if (isDirectM3uContent) "#EXTM3U\n#EXTINF:-1..." else "https://example.com/playlist.m3u") },
                                minLines = if (isDirectM3uContent) 4 else 1,
                                maxLines = if (isDirectM3uContent) 6 else 2,
                                modifier = Modifier.fillMaxWidth().testTag("m3u_url_input"),
                                colors = iptvTextFieldColors()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { isDirectM3uContent = !isDirectM3uContent },
                                    modifier = Modifier.testTag("toggle_m3u_mode_btn")
                                ) {
                                    Text(
                                        if (isDirectM3uContent) "Switch to URL" else "Paste Text Instead",
                                        color = CyanBright,
                                        fontSize = 12.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        m3uName = "Sample Sports & News"
                                        m3uUrl = DefaultData.SAMPLE_M3U
                                        isDirectM3uContent = true
                                    },
                                    modifier = Modifier.testTag("load_sample_m3u_btn")
                                ) {
                                    Text("Load Sample", color = TextPrimary, fontSize = 12.sp)
                                }
                            }
                        }
                        1 -> {
                            // Xtream Codes Tab
                            Text(
                                text = "Xtream Codes API",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Connect using your Xtream server host, port, username, and password.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = xtreamName,
                                onValueChange = { xtreamName = it },
                                label = { Text("Account Name") },
                                placeholder = { Text("e.g. Xtream VIP") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("xtream_name_input"),
                                colors = iptvTextFieldColors()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = xtreamServer,
                                onValueChange = { xtreamServer = it },
                                label = { Text("Server URL / Host") },
                                placeholder = { Text("http://provider.tv:8080") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("xtream_server_input"),
                                colors = iptvTextFieldColors()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = xtreamUser,
                                onValueChange = { xtreamUser = it },
                                label = { Text("Username") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("xtream_user_input"),
                                colors = iptvTextFieldColors()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = xtreamPass,
                                onValueChange = { xtreamPass = it },
                                label = { Text("Password") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("xtream_pass_input"),
                                colors = iptvTextFieldColors()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    xtreamName = "Demo Xtream Server"
                                    xtreamServer = "http://demo-xtream.tv:8080"
                                    xtreamUser = "vip_demo"
                                    xtreamPass = "stream2026"
                                },
                                modifier = Modifier.align(Alignment.End).testTag("load_demo_xtream_btn")
                            ) {
                                Text("Fill Demo Credentials", color = TextPrimary, fontSize = 12.sp)
                            }
                        }
                        2 -> {
                            // Stalker Portal Tab
                            Text(
                                text = "Stalker / Ministra Portal",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Connect via MAC address handshake to Stalker middleware portals.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = stalkerName,
                                onValueChange = { stalkerName = it },
                                label = { Text("Portal Name") },
                                placeholder = { Text("e.g. Stalker Mag") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("stalker_name_input"),
                                colors = iptvTextFieldColors()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = stalkerUrl,
                                onValueChange = { stalkerUrl = it },
                                label = { Text("Portal URL") },
                                placeholder = { Text("http://portal.example.com/c/") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("stalker_url_input"),
                                colors = iptvTextFieldColors()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = stalkerMac,
                                onValueChange = { stalkerMac = it },
                                label = { Text("Device MAC Address") },
                                placeholder = { Text("00:1A:79:XX:XX:XX") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("stalker_mac_input"),
                                colors = iptvTextFieldColors()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    stalkerName = "Demo Stalker MAG"
                                    stalkerUrl = "http://demo-portal.example.com/c/"
                                    stalkerMac = "00:1A:79:B4:C2:9E"
                                },
                                modifier = Modifier.align(Alignment.End).testTag("load_demo_stalker_btn")
                            ) {
                                Text("Fill Demo Stalker", color = TextPrimary, fontSize = 12.sp)
                            }
                        }
                        3 -> {
                            // XMLTV EPG Tab
                            Text(
                                text = "Electronic Program Guide (XMLTV)",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Load XMLTV schedule data to match channel tvg-ids and show live program guides.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = epgUrlOrContent,
                                onValueChange = { epgUrlOrContent = it },
                                label = { Text(if (isDirectEpgXml) "Paste XMLTV XML" else "XMLTV URL (.xml)") },
                                placeholder = { Text(if (isDirectEpgXml) "<tv><channel..." else "https://iptv-org.github.io/epg/guides/us.xml") },
                                minLines = if (isDirectEpgXml) 4 else 1,
                                maxLines = if (isDirectEpgXml) 6 else 2,
                                modifier = Modifier.fillMaxWidth().testTag("epg_input"),
                                colors = iptvTextFieldColors()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { isDirectEpgXml = !isDirectEpgXml },
                                    modifier = Modifier.testTag("toggle_epg_mode_btn")
                                ) {
                                    Text(
                                        if (isDirectEpgXml) "Switch to URL" else "Paste XML Instead",
                                        color = CyanBright,
                                        fontSize = 12.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        epgUrlOrContent = DefaultData.SAMPLE_XMLTV
                                        isDirectEpgXml = true
                                    },
                                    modifier = Modifier.testTag("load_sample_epg_btn")
                                ) {
                                    Text("Load Sample XMLTV", color = TextPrimary, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    statusMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = GreenActive,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (selectedTab) {
                        0 -> {
                            if (m3uUrl.isNotBlank()) {
                                onAddM3u(m3uName, m3uUrl, isDirectM3uContent)
                            }
                        }
                        1 -> {
                            if (xtreamServer.isNotBlank() && xtreamUser.isNotBlank()) {
                                onAddXtream(xtreamName, xtreamServer, xtreamUser, xtreamPass)
                            }
                        }
                        2 -> {
                            if (stalkerUrl.isNotBlank() && stalkerMac.isNotBlank()) {
                                onAddStalker(stalkerName, stalkerUrl, stalkerMac)
                            }
                        }
                        3 -> {
                            if (epgUrlOrContent.isNotBlank()) {
                                onAddEpg(epgUrlOrContent, isDirectEpgXml)
                            }
                        }
                    }
                },
                enabled = !isProcessing,
                modifier = Modifier.testTag("confirm_add_source_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanBright)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = SlateDark900,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (selectedTab == 3) "Load EPG" else "Add Source",
                        color = SlateDark900,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_add_source_button")
            ) {
                Text("Close", color = SlateMuted)
            }
        }
    )
}

@Composable
private fun iptvTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyanBright,
    unfocusedBorderColor = SlateDark700,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = CyanBright,
    unfocusedLabelColor = SlateMuted
)
