package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DefaultData
import com.example.data.M3uParser
import com.example.data.XmltvParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("freboy media plus", appName)
    }

    @Test
    fun `m3u parser extracts channels successfully`() {
        val result = M3uParser.parse(DefaultData.SAMPLE_M3U)
        assertEquals(3, result.channels.size)
        assertEquals("NASA TV HD", result.channels[0].name)
        assertEquals("Science", result.channels[0].category)
        assertEquals("nasa.tv", result.channels[0].epgId)
    }

    @Test
    fun `xmltv parser extracts epg programs successfully`() {
        val programs = XmltvParser.parse(DefaultData.SAMPLE_XMLTV)
        assertEquals(2, programs.size)
        assertEquals("Spacewalk Mission Operations", programs[0].title)
        assertEquals("nasa.tv", programs[0].channelId)
    }
}
