package com.telebot.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.telebot.properties.GptProperties

data class GptRequestDTO(
    val messages: List<Message>,
    val model: String? = null,
    val n: Int? = null,
    @JsonProperty("frequency_penalty") val frequencyPenalty: Double? = null,
    @JsonProperty("max_tokens") val maxTokens: Int? = null,
    @JsonProperty("presence_penalty") val presencePenalty: Double? = null,
    val temperature: Double? = null,
    @JsonProperty("top_p") val topP: Double? = null,
    @JsonProperty("tool_choice") val toolChoice: Any? = null
) {
    constructor(messages: List<Message>, gptProperties: GptProperties) : this(
        messages = messages,
        model = gptProperties.model,
        n = gptProperties.n,
        frequencyPenalty = gptProperties.frequencyPenalty,
        maxTokens = gptProperties.maxTokens,
        presencePenalty = gptProperties.presencePenalty,
        temperature = gptProperties.temperature,
        topP = gptProperties.topP,
        toolChoice = gptProperties.toolChoice
    )

    data class Message(
        val role: String,
        var content: String
    )
}
