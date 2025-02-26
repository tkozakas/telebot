package com.telebot.service

import com.telebot.client.TtsClient
import com.telebot.dto.TtsRequestDTO
import com.telebot.model.UpdateContext
import com.telebot.properties.TtsProperties
import eu.vendeli.tgbot.api.media.sendAudio
import eu.vendeli.tgbot.api.message.sendMessage
import eu.vendeli.tgbot.types.internal.ImplicitFile
import eu.vendeli.tgbot.types.internal.InputFile
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
        private const val NO_MESSAGE_PROVIDED = "Please provide a message to convert to speech."
        private const val TEMP_FILE_PREFIX = "audio"
        private const val TEMP_FILE_EXTENSION = ".mp3"
    }

    override suspend fun handle(update: UpdateContext) {
        val messageText = update.args.drop(1).joinToString(" ")
        if (messageText.isBlank()) {
            sendMessage { NO_MESSAGE_PROVIDED }.send(update.telegramChatId, update.bot)
            return
        }
        val tempAudioFile = generateAudioFile(messageText)

        if (tempAudioFile == null) {
            sendMessage { NO_RESPONSE_MESSAGE }.send(update.telegramChatId, update.bot)
        } else {
            sendAudio(ImplicitFile.InpFile(tempAudioFile))
                .caption { messageText }
                .send(update.telegramChatId, update.bot)
        }
    }

    suspend fun generateAudioFile(messageText: String): InputFile? {
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
                val file = withContext(Dispatchers.IO) {
                    File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_EXTENSION)
                }.apply {
                    Files.write(toPath(), audioBytes)
                }
                return InputFile(file.readBytes())
            }
        }

        return null
    }
}
