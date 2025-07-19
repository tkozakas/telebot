package com.telebot.service

import com.telebot.client.TtsClient
import com.telebot.model.UpdateContext
import com.telebot.properties.TtsProperties
import org.springframework.stereotype.Service

@Service
class TtsService(
    private val ttsProperties: TtsProperties,
    private val ttsClient: TtsClient
) : CommandService {

    companion object {
        private const val NO_RESPONSE_MESSAGE = "TTS did not provide a response. Please try again."
        private const val NO_MESSAGE_PROVIDED = "Please provide a message to convert to speech."
        private const val TEMP_FILE_PREFIX = "audio"
        private const val TEMP_FILE_EXTENSION = ".mp3"
    }

    override suspend fun handle(update: UpdateContext) {
        val messageText = update.args.drop(1).joinToString(" ")
    }
}
