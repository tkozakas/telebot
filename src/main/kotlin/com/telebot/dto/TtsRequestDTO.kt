package com.telebot.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TtsRequestDTO(
    val text: String,
    @JsonProperty("model_id")
    val modelId: String,
    @JsonProperty("voice_settings")
    val voiceSettings: Map<String, Any> = emptyMap()
)

