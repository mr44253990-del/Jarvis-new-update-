package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.action.ActionParser
import com.example.action.DeviceAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Rakib Jarvis", appName)
    }

    @Test
    fun `parse phone call command`() {
        val action = ActionParser.parse("01712345678 নাম্বারে কল করো")
        assertTrue(action is DeviceAction.CallPhone)
        assertEquals("01712345678", (action as DeviceAction.CallPhone).phoneNumber)
    }

    @Test
    fun `parse youtube command`() {
        val action = ActionParser.parse("ইউটিউবে গান বাজাও")
        assertTrue(action is DeviceAction.OpenYouTube)
    }

    @Test
    fun `parse yt text file command`() {
        val action = ActionParser.parse("yt.txt ফাইল তৈরি করো")
        assertTrue(action is DeviceAction.CreateTextFile)
        assertEquals("yt.txt", (action as DeviceAction.CreateTextFile).fileName)
    }
}
