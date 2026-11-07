package com.example.data

import com.example.model.Channel
import java.io.BufferedReader
import java.io.StringReader
import java.util.UUID

data class M3uParseResult(
    val channels: List<Channel>,
    val epgUrl: String?
)

object M3uParser {

    private val TVG_ID_REGEX = """tvg-id=["']?([^"' ,]+)["']?""".toRegex(RegexOption.IGNORE_CASE)
    private val TVG_NAME_REGEX = """tvg-name=["']?([^"']+)["']?""".toRegex(RegexOption.IGNORE_CASE)
    private val TVG_LOGO_REGEX = """tvg-logo=["']?([^"']+)["']?""".toRegex(RegexOption.IGNORE_CASE)
    private val GROUP_TITLE_REGEX = """group-title=["']?([^"']+)["']?""".toRegex(RegexOption.IGNORE_CASE)
    private val EPG_URL_REGEX = """(?:x-tvg-url|url-tvg)=["']?([^"'\s]+)["']?""".toRegex(RegexOption.IGNORE_CASE)

    fun parse(content: String, sourceId: String = "m3u_import"): M3uParseResult {
        val channels = mutableListOf<Channel>()
        var detectedEpgUrl: String? = null

        val reader = BufferedReader(StringReader(content))
        var line: String? = reader.readLine()

        var currentTvgId = ""
        var currentTvgName = ""
        var currentTvgLogo = ""
        var currentGroupTitle = "General"
        var currentChannelName = ""
        var channelIndex = 1

        while (line != null) {
            val trimmed = line.trim()

            if (trimmed.startsWith("#EXTM3U", ignoreCase = true)) {
                val match = EPG_URL_REGEX.find(trimmed)
                if (match != null) {
                    detectedEpgUrl = match.groupValues[1]
                }
            } else if (trimmed.startsWith("#EXTINF:", ignoreCase = true)) {
                // Parse attributes
                val tvgIdMatch = TVG_ID_REGEX.find(trimmed)
                currentTvgId = tvgIdMatch?.groupValues?.get(1)?.trim() ?: ""

                val tvgNameMatch = TVG_NAME_REGEX.find(trimmed)
                currentTvgName = tvgNameMatch?.groupValues?.get(1)?.trim() ?: ""

                val logoMatch = TVG_LOGO_REGEX.find(trimmed)
                currentTvgLogo = logoMatch?.groupValues?.get(1)?.trim() ?: ""

                val groupMatch = GROUP_TITLE_REGEX.find(trimmed)
                currentGroupTitle = groupMatch?.groupValues?.get(1)?.trim() ?: "General"

                // Extract title after comma
                val commaIndex = trimmed.lastIndexOf(',')
                currentChannelName = if (commaIndex != -1 && commaIndex < trimmed.length - 1) {
                    trimmed.substring(commaIndex + 1).trim()
                } else if (currentTvgName.isNotEmpty()) {
                    currentTvgName
                } else {
                    "Channel $channelIndex"
                }
            } else if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                // This is the stream URL
                val streamUrl = trimmed
                val finalName = if (currentChannelName.isNotEmpty()) currentChannelName else "Channel $channelIndex"
                val resolution = inferResolution(finalName, streamUrl)

                channels.add(
                    Channel(
                        id = UUID.randomUUID().toString(),
                        channelNumber = channelIndex,
                        name = finalName,
                        logoUrl = currentTvgLogo,
                        streamUrl = streamUrl,
                        category = if (currentGroupTitle.isNotBlank()) currentGroupTitle else "General",
                        epgId = if (currentTvgId.isNotEmpty()) currentTvgId else finalName.lowercase().replace("\\s+".toRegex(), "."),
                        resolution = resolution,
                        sourceId = sourceId
                    )
                )

                channelIndex++
                // Reset for next channel
                currentTvgId = ""
                currentTvgName = ""
                currentTvgLogo = ""
                currentGroupTitle = "General"
                currentChannelName = ""
            }

            line = reader.readLine()
        }

        return M3uParseResult(channels, detectedEpgUrl)
    }

    private fun inferResolution(name: String, url: String): String {
        val combined = "$name $url".uppercase()
        return when {
            combined.contains("4K") || combined.contains("UHD") || combined.contains("2160") -> "4K"
            combined.contains("FHD") || combined.contains("1080") -> "FHD"
            combined.contains("HD") || combined.contains("720") -> "HD"
            else -> "HD"
        }
    }
}
