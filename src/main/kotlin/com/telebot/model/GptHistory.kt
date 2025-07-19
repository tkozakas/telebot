package com.telebot.model

import com.telebot.dto.GptRequestDTO
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("GptHistory")
data class GptHistory(
    @Id val chatId: Long,
    val messages: MutableList<GptRequestDTO.Message> = mutableListOf()
)