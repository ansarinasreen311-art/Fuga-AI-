package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class FugaAccessibilityService : AccessibilityService() {

    companion object {
        var isServiceConnected = false
            private set
        var latestScreenText: String? = null
            private set
        private var instance: FugaAccessibilityService? = null

        fun performTap(x: Float, y: Float): Boolean {
            return instance?.tap(x, y) ?: false
        }

        fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float): Boolean {
            return instance?.swipe(startX, startY, endX, endY) ?: false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceConnected = true
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val rootNode = rootInActiveWindow ?: return
        try {
            val textBuilder = StringBuilder()
            collectNodeText(rootNode, textBuilder)
            latestScreenText = textBuilder.toString()
        } catch (e: Exception) {
            // Ignore transient window access issues
        }
    }

    private fun collectNodeText(node: AccessibilityNodeInfo, builder: StringBuilder) {
        node.text?.let {
            if (it.isNotBlank()) {
                builder.append(it).append(" | ")
            }
        }
        node.contentDescription?.let {
            if (it.isNotBlank()) {
                builder.append(it).append(" | ")
            }
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                collectNodeText(child, builder)
            }
        }
    }

    fun tap(x: Float, y: Float): Boolean {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    fun swipe(startX: Float, startY: Float, endX: Float, endY: Float): Boolean {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 300)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    override fun onInterrupt() {
        isServiceConnected = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceConnected = false
        instance = null
    }
}
