package com.telebot.service

import com.telebot.client.TtsClient
import com.telebot.dto.TtsRequestDTO
import com.telebot.model.UpdateContext
import com.telebot.properties.TtsProperties
import eu.vendeli.tgbot.api.media.sendAudio
import eu.vendeli.tgbot.api.message.sendMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files

@Service
class TtsService(
    private val ttsProperties: TtsProperties,
    private val ttsClient: TtsClient
) : CommandService {

    companion object {
        private const val NO_RESPONSE_MESSAGE = "TTS did not provide a response. Please try again."
        private const val TEMP_FILE_PREFIX = "audio"
        private const val TEMP_FILE_EXTENSION = ".mp3"
    }

    override suspend fun handle(update: UpdateContext) {
        val messageText = update.args.drop(1).joinToString(" ")
        val tempAudioFile = generateAudioFile(messageText)

        if (tempAudioFile == null) {
            sendMessage { NO_RESPONSE_MESSAGE }.send(update.chatId, update.bot)
        } else {
            sendAudio { tempAudioFile.absolutePath }.send(update.chatId, update.bot)
        }
    }

    suspend fun generateAudioFile(messageText: String): File? {
        val request = TtsRequestDTO(
            text = messageText,
            modelId = ttsProperties.modelId,
            voiceSettings = mapOf(
                "stability" to ttsProperties.stability,
                "similarity_boost" to ttsProperties.similarityBoost,
                "style" to ttsProperties.style,
                "use_speaker_boost" to ttsProperties.useSpeakerBoost
            )
        )

        for (apiKey in ttsProperties.token) {
            val audioBytes = ttsClient.generateSpeech(apiKey, ttsProperties.voiceId, request)

            if (audioBytes != null) {
                return withContext(Dispatchers.IO) {
                    File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_EXTENSION)
                }.apply {
                    Files.write(toPath(), audioBytes)
                }
            }
        }

        return null
    }
}
