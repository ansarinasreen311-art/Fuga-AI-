package com.example.data.model

enum class ActionType(val label: String, val iconName: String) {
    OPEN_APP("Open App", "launch"),
    CALL_PHONE("Phone Call", "call"),
    SEND_MESSAGE("Send Message", "chat"),
    SET_ALARM("Set Alarm", "alarm"),
    TOGGLE_FLASHLIGHT("Flashlight", "flashlight_on"),
    WEB_SEARCH("Web Search", "search"),
    READ_SCREEN("Read Screen", "screen_share"),
    PAYMENT("Payment", "lock")
}

data class ActionCommand(
    val type: ActionType,
    val title: String,
    val description: String,
    val isSensitive: Boolean,
    val target: String,
    val param: String? = null
)
