package com.telebot.dto

data class GptResponseDTO(
    val choices: List<Choice>
) {
    data class Choice(
        val message: Message
    ) {
        data class Message(
            val role: String,
            val content: String
        )
    }
}
