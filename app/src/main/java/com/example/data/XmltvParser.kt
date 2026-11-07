package com.example.data

import com.example.model.EpgProgram
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

object XmltvParser {

    /**
     * Parses XMLTV format string into a list of EpgProgram items.
     * Format example:
     * <programme start="20261107080000 +0000" stop="20261107090000 +0000" channel="nasa.tv">
     *     <title lang="en">Space Station Live</title>
     *     <desc lang="en">Live coverage from the International Space Station.</desc>
     *     <category lang="en">Science</category>
     * </programme>
     */
    fun parse(xmlContent: String): List<EpgProgram> {
        val programs = mutableListOf<EpgProgram>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlContent))

            var eventType = parser.eventType
            var currentChannelId: String? = null
            var currentStartEpoch: Long = 0
            var currentEndEpoch: Long = 0
            var currentTitle = ""
            var currentDesc = ""
            var currentCategory = ""
            var currentRating = ""
            var inProgramme = false

            val dateFormats = listOf(
                SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US),
                SimpleDateFormat("yyyyMMddHHmmss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            )

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (tagName?.lowercase(Locale.ROOT)) {
                            "programme" -> {
                                inProgramme = true
                                currentChannelId = parser.getAttributeValue(null, "channel")
                                val startAttr = parser.getAttributeValue(null, "start")
                                val stopAttr = parser.getAttributeValue(null, "stop")

                                currentStartEpoch = parseDate(startAttr, dateFormats)
                                currentEndEpoch = parseDate(stopAttr, dateFormats)
                                currentTitle = ""
                                currentDesc = ""
                                currentCategory = ""
                                currentRating = ""
                            }
                            "title" -> if (inProgramme) currentTitle = parser.nextText().trim()
                            "desc" -> if (inProgramme) currentDesc = parser.nextText().trim()
                            "category" -> if (inProgramme && currentCategory.isEmpty()) currentCategory = parser.nextText().trim()
                            "value" -> if (inProgramme && currentRating.isEmpty()) currentRating = parser.nextText().trim()
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagName?.lowercase(Locale.ROOT) == "programme" && inProgramme) {
                            if (!currentChannelId.isNullOrEmpty() && currentTitle.isNotEmpty()) {
                                programs.add(
                                    EpgProgram(
                                        id = UUID.randomUUID().toString(),
                                        channelId = currentChannelId,
                                        title = currentTitle,
                                        description = currentDesc,
                                        startEpochSeconds = currentStartEpoch,
                                        endEpochSeconds = if (currentEndEpoch > currentStartEpoch) currentEndEpoch else currentStartEpoch + 3600,
                                        category = currentCategory,
                                        rating = currentRating
                                    )
                                )
                            }
                            inProgramme = false
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return programs
    }

    private fun parseDate(dateStr: String?, formats: List<SimpleDateFormat>): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis() / 1000
        for (format in formats) {
            try {
                val parsed = format.parse(dateStr.trim())
                if (parsed != null) {
                    return parsed.time / 1000
                }
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis() / 1000
    }
}
