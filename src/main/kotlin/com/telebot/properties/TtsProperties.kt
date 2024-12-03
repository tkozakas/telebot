package com.telebot.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "tts")
data class TtsProperties(
    val token: List<String>,
    val voiceId: String,
    val modelId: String,
    val stability: Double,
    val similarityBoost: Double,
    val style: Double,
    val useSpeakerBoost: Boolean
)
