package com.example

import com.example.ai.GeminiClient
import com.example.data.model.ActionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private val geminiClient = GeminiClient()

    @Test
    fun testPaymentIntent_isSensitive() {
        val action = geminiClient.detectLocalAction("Pay ₹500 to Rahul")
        assertNotNull(action)
        assertEquals(ActionType.PAYMENT, action?.type)
        assertTrue(action?.isSensitive == true)
        assertEquals("500", action?.param)
        assertEquals("Rahul", action?.target)
    }

    @Test
    fun testOpenAppIntent_notSensitive() {
        val action = geminiClient.detectLocalAction("Open YouTube")
        assertNotNull(action)
        assertEquals(ActionType.OPEN_APP, action?.type)
        assertTrue(action?.isSensitive == false)
        assertEquals("YouTube", action?.target)
    }

    @Test
    fun testFlashlightIntent() {
        val action = geminiClient.detectLocalAction("Turn on flashlight")
        assertNotNull(action)
        assertEquals(ActionType.TOGGLE_FLASHLIGHT, action?.type)
        assertEquals("on", action?.target)
    }

    @Test
    fun testAlarmIntent() {
        val action = geminiClient.detectLocalAction("Set an alarm for 7 AM")
        assertNotNull(action)
        assertEquals(ActionType.SET_ALARM, action?.type)
    }

    @Test
    fun testSearchIntent() {
        val action = geminiClient.detectLocalAction("Search nearest hospital")
        assertNotNull(action)
        assertEquals(ActionType.WEB_SEARCH, action?.type)
    }
}
