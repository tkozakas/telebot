package com.telebot.client

import com.telebot.config.FeignConfig
import com.telebot.dto.GptRequestDTO
import com.telebot.dto.GptResponseDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "gptClient",
    url = "https://api.groq.com/openai/v1",
    configuration = [FeignConfig::class]
)
interface GptClient {

    @PostMapping("/chat/completions")
    fun getChatCompletion(@RequestBody request: GptRequestDTO?): GptResponseDTO?
}
