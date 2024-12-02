package com.telebot.client

import com.telebot.dto.TtsRequestDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@Component
@FeignClient(name = "ttsClient", url = "https://api.elevenlabs.io/v1")
interface TtsClient {

    @PostMapping("/text-to-speech/{voiceId}")
    fun convertTextToSpeech(
        @RequestHeader("xi-api-key") apiKey: String,
        @PathVariable("voiceId") voiceId: String,
        @RequestBody request: TtsRequestDTO
    ): ByteArray?
}
