package com.telebot.client

import com.telebot.dto.GptRequestDTO
import com.telebot.dto.GptResponseDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@Component
@FeignClient(name = "gptClient", url = "https://api.groq.com/openai/v1")
interface GptClient {

    @PostMapping("/chat/completions")
    fun getChatCompletion(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody request: GptRequestDTO?
    ): GptResponseDTO?
}
