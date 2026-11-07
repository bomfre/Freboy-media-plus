package com.example.data

import com.example.model.Channel
import com.example.model.EpgProgram
import com.example.model.PlaylistSource
import com.example.model.SourceType
import java.util.UUID

object DefaultData {

    val defaultSource = PlaylistSource(
        id = "built_in_source",
        name = "StreamPulse Showcase (Free Legal IPTV)",
        type = SourceType.BUILT_IN,
        url = "https://iptv-org.github.io/iptv/index.m3u",
        channelCount = 10,
        isActive = true
    )

    fun getInitialChannels(): List<Channel> = listOf(
        Channel(
            id = "ch_nasa",
            channelNumber = 1,
            name = "NASA TV Live HD",
            logoUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=200",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            category = "Science & Tech",
            epgId = "nasa.tv",
            resolution = "FHD",
            sourceId = "built_in_source"
        ),
        Channel(
            id = "ch_bloomberg",
            channelNumber = 2,
            name = "Bloomberg Global Markets",
            logoUrl = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=200",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            category = "News & Finance",
            epgId = "bloomberg.tv",
            resolution = "HD",
            sourceId = "built_in_source"
        ),
        Channel(
            id = "ch_redbull",
            channelNumber = 3,
            name = "Red Bull TV Extreme Sports",
            logoUrl = "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=200",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            category = "Sports Live",
            epgId = "redbull.tv",
            resolution = "4K",
            sourceId = "built_in_source"
        ),
        Channel(
            id = "ch_dw",
            channelNumber = 4,
            name = "DW News English 24/7",
            logoUrl = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=200",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            category = "News & Finance",
            epgId = "dw.english",
            resolution = "FHD",
            sourceId = "built_in_source"
        ),
        Channel(
            id = "ch_france24",
            channelNumber = 5,
            name = "France 24 International",
            logoUrl = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=200",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            category = "News & Finance",
            epgId = "france24.en",
            resolution = "HD",
            sourceId = "built_in_source"
        ),
        Channel(
            id = "ch_retrotoons",
            channelNumber = 6,
            name = "Retro Toons & Cartoons",
            logoUrl = "https://images.unsplash.com/photo-1566140967404-b8b393271a22?w=200",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
            category = "Kids & Family",
            epgId = "retro.cartoons",
            resolution = "HD",
            sourceId = "built_in_source"
        ),
        Channel(
            id = "ch_nature",
            channelNumber = 7,
            name = "Wild Planet 4K Earth",
            logoUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=200",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
            category = "Documentary",
            epgId = "wild.planet",
            resolution = "4K",
            sourceId = "built_in_source"
        ),
        Channel(
            id = "ch_cinema",
            channelNumber = 8,
            name = "Silver Screen Classic Cinema",
            logoUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=200",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            category = "Movies & Cinema",
            epgId = "silver.screen",
            resolution = "FHD",
            sourceId = "built_in_source"
        ),
        Channel(
            id = "ch_synthwave",
            channelNumber = 9,
            name = "Neon Chill Beats & Synthwave",
            logoUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=200",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackSeeTheWorld.mp4",
            category = "Music & Vibes",
            epgId = "neon.chill",
            resolution = "FHD",
            sourceId = "built_in_source"
        ),
        Channel(
            id = "ch_euronews",
            channelNumber = 10,
            name = "Euronews European Live",
            logoUrl = "https://images.unsplash.com/photo-1526470608268-f674ce90ebd4?w=200",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            category = "News & Finance",
            epgId = "euronews.en",
            resolution = "HD",
            sourceId = "built_in_source"
        )
    )

    fun generateDynamicEpg(channels: List<Channel>): List<EpgProgram> {
        val now = System.currentTimeMillis() / 1000
        val programs = mutableListOf<EpgProgram>()

        for (channel in channels) {
            val templates = getProgramTemplatesForChannel(channel)
            // Generate 6 hours of schedules around current time (-1 hr to +5 hrs)
            val baseTime = (now / 3600) * 3600 - 3600

            for (i in 0 until 6) {
                val start = baseTime + (i * 3600)
                val end = start + 3600
                val t = templates[i % templates.size]
                programs.add(
                    EpgProgram(
                        id = UUID.randomUUID().toString(),
                        channelId = if (channel.epgId.isNotEmpty()) channel.epgId else channel.id,
                        title = t.title,
                        description = t.desc,
                        startEpochSeconds = start,
                        endEpochSeconds = end,
                        category = channel.category,
                        rating = t.rating
                    )
                )
            }
        }
        return programs
    }

    private data class ProgTemplate(val title: String, val desc: String, val rating: String = "PG")

    private fun getProgramTemplatesForChannel(channel: Channel): List<ProgTemplate> {
        return when (channel.category) {
            "Science & Tech" -> listOf(
                ProgTemplate("ISS Live Expedition: Spacewalk", "Direct downlink and audio from astronauts on the International Space Station.", "G"),
                ProgTemplate("Artemis Deep Space Telemetry", "Latest orbital maneuvers and lunar gateway docking tests.", "G"),
                ProgTemplate("James Webb Cosmic Frontiers", "High-resolution infrared deep field observations and stellar genesis.", "G"),
                ProgTemplate("Space Science Hour", "Interviews with propulsion engineers and planetary scientists.", "G")
            )
            "Sports Live" -> listOf(
                ProgTemplate("Extreme Mountain Downhill Finals", "World champions tackle steep alpine terrain with drone flybys.", "PG"),
                ProgTemplate("Freestyle Motocross Red Bull X", "High-flying aerial acrobatics and stunt competition.", "PG"),
                ProgTemplate("Cliff Diving World Championship", "High-adrenaline 27-meter leaps into crystal Mediterranean coves.", "PG"),
                ProgTemplate("Formula Drift: Speed Circuit", "Turbocharged tandem battles around asphalt tracks.", "PG")
            )
            "Movies & Cinema" -> listOf(
                ProgTemplate("Golden Era Sci-Fi: Destination Moon", "Classic 1950s space exploration masterpiece with restored audio.", "PG"),
                ProgTemplate("Noir Mystery: The Midnight Chase", "Detective drama through rain-soaked streets with vintage saxophone score.", "PG-13"),
                ProgTemplate("Western Legends: High Plains Outlaw", "Classic technicolor showdown under the desert sun.", "PG"),
                ProgTemplate("Cinema Retrospective & Director Cuts", "Rare behind-the-scenes footage and commentary on filmmaking classics.", "PG")
            )
            "Kids & Family" -> listOf(
                ProgTemplate("Classic Cartoons: Popeye & Fleischer", "Vintage animated adventures with spinach power and silly antics.", "G"),
                ProgTemplate("Looney Tunes Golden Classics", "Bugs Bunny, Daffy Duck, and Elmer Fudd antics restored.", "G"),
                ProgTemplate("Betty Boop & Felix the Cat Hours", "Hand-drawn animation from the birth of cartoon studios.", "G"),
                ProgTemplate("Saturday Morning Cartoon Cavalcade", "Wholesome morning entertainment for all ages.", "G")
            )
            "Documentary" -> listOf(
                ProgTemplate("Serengeti Great Migration 4K", "Millions of wildebeest and predators crossing African savanna.", "G"),
                ProgTemplate("Ocean Depths: Mariana Trench", "Submersible expedition uncovering bioluminescent organisms.", "G"),
                ProgTemplate("Arctic Wilderness Survivors", "Polar bears and arctic foxes adapting to changing glaciers.", "G"),
                ProgTemplate("Ancient Amazon Rainforest", "Canopy biodiversity and undiscovered plant remedies.", "G")
            )
            "Music & Vibes" -> listOf(
                ProgTemplate("Midnight Retrowave Drive", "Atmospheric 80s synthesizers with cyberpunk highway visuals.", "G"),
                ProgTemplate("Lo-Fi Beats to Relax / Study", "Smooth jazz hip-hop beats with cozy anime rain aesthetics.", "G"),
                ProgTemplate("Electronic Pulse Live Sessions", "Modular synth performances and visualizer audio-reactive art.", "G"),
                ProgTemplate("Ambient Chillout Lounge", "Gentle downtempo pads and soothing cosmic panoramas.", "G")
            )
            else -> listOf(
                ProgTemplate("World News Live: Global Headlines", "Comprehensive live reporting and analysis on international affairs.", "PG"),
                ProgTemplate("Financial Briefing & Stock Indices", "Wall Street, European, and Asian market developments.", "PG"),
                ProgTemplate("In-Depth Investigative Report", "Journalistic deep dive into environmental and economic trends.", "PG"),
                ProgTemplate("Global Roundtable Discussion", "Panel of correspondents discussing the week's breaking stories.", "PG")
            )
        }
    }

    const val SAMPLE_M3U = """#EXTM3U x-tvg-url="https://iptv-org.github.io/epg/guides/us.xml"
#EXTINF:-1 tvg-id="nasa.tv" tvg-name="NASA TV" tvg-logo="https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=200" group-title="Science",NASA TV HD
https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4
#EXTINF:-1 tvg-id="redbull.tv" tvg-name="Red Bull TV" tvg-logo="https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=200" group-title="Sports",Red Bull TV Live
https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4
#EXTINF:-1 tvg-id="bloomberg.tv" tvg-name="Bloomberg" tvg-logo="https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=200" group-title="News",Bloomberg Global Finance
https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4
"""

    const val SAMPLE_XMLTV = """<?xml version="1.0" encoding="UTF-8"?>
<tv>
  <channel id="nasa.tv">
    <display-name>NASA TV HD</display-name>
  </channel>
  <programme start="20261107060000 +0000" stop="20261107080000 +0000" channel="nasa.tv">
    <title>Spacewalk Mission Operations</title>
    <desc>Live video and audio telemetry from the ISS crew conducting external orbital maintenance.</desc>
    <category>Science</category>
  </programme>
  <programme start="20261107080000 +0000" stop="20261107100000 +0000" channel="nasa.tv">
    <title>Deep Space Exploration Briefing</title>
    <desc>NASA engineers present preliminary findings from deep space probes.</desc>
    <category>Science</category>
  </programme>
</tv>"""
}
