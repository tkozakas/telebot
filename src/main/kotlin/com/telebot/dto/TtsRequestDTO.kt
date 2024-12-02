package com.telebot.dto

data class TtsRequestDTO(
    val text: String,
    val modelId: String,
    val voiceSettings: Map<String, Any> = emptyMap()
)

