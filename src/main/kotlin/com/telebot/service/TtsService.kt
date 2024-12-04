package com.telebot.service

import com.telebot.client.TtsClient
import com.telebot.dto.TtsRequestDTO
import com.telebot.properties.TtsProperties
import io.github.dehuckakpyt.telegrambot.model.telegram.input.ContentInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files

@Service
class TtsService(
    private val ttsProperties: TtsProperties,
    private val ttsClient: TtsClient
) {
    companion object Constants {
        const val NO_RESPONSE = "TTS did not provide a response. Please try again."
        const val DEFAULT_TITLE = "audio"
    }

    suspend fun handleTtsCommand(
        messageText: String,
        sendAudio: suspend (ContentInput, String) -> Unit,
        sendMessage: suspend (String) -> Unit,
        input: (File) -> ContentInput
    ) {
        withContext(Dispatchers.IO) {
            val tempFile = getAudio(messageText)
            if (tempFile == null) {
                sendMessage(NO_RESPONSE)
                return@withContext
            }

            val contentInput = input(tempFile)
            sendAudio(contentInput, messageText)
        }
    }

    private fun getRequest(messageText: String): TtsRequestDTO {
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
        return request
    }

    fun getAudio(messageText: String): File? {
        val request = getRequest(messageText)
        var audio: ByteArray?

        for (i in ttsProperties.token.indices) {
            try {
                audio = ttsClient.generateSpeech(
                    apiKey = ttsProperties.token[i],
                    voiceId = ttsProperties.voiceId,
                    request = request
                )
            } catch (e: Exception) {
                continue
            }
            if (audio != null) {
                val tempFile = File.createTempFile(DEFAULT_TITLE, ".mp3")
                Files.write(tempFile.toPath(), audio)
                return tempFile
            }
        }
        return null
    }

}
