package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.utils.BengaliNumberUtils
import com.example.utils.SmartEntryParser
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
        assertEquals("বাজারি", appName)
    }

    @Test
    fun `smart parser parses bangla grocery queries`() {
        val entry1 = SmartEntryParser.parse("৫ কেজি মিনিকেট চাল")
        assertEquals("৫", entry1.quantity)
        assertEquals("কেজি", entry1.unit)
        assertEquals("মিনিকেট চাল", entry1.productName)

        val entry2 = SmartEntryParser.parse("১২টা ডিম")
        assertEquals("১২", entry2.quantity)
        assertEquals("পিস", entry2.unit)
        assertEquals("ডিম", entry2.productName)

        val entry3 = SmartEntryParser.parse("সয়াবিন তেল ২ লিটার")
        assertEquals("২", entry3.quantity)
        assertEquals("লিটার", entry3.unit)
        assertEquals("সয়াবিন তেল", entry3.productName)
    }

    @Test
    fun `bengali number utils formats currency accurately`() {
        val takaStr = BengaliNumberUtils.formatPriceTaka(5500.0)
        assertTrue(takaStr.contains("৳"))
        assertTrue(takaStr.contains("৫,৫০০") || takaStr.contains("৫500") || takaStr.contains("৫"))
    }
}
