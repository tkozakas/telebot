package com.telebot.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gpt")
data class GptProperties(
    val model: String,
    val n: Int,
    val frequencyPenalty: Double,
    val maxTokens: Int,
    val presencePenalty: Double,
    val temperature: Double,
    val topP: Double,
    val toolChoice: String?
)
