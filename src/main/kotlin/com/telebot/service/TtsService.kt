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
    private val ttsClient: TtsClient,
    private val gptService: GptService
) {
    companion object Constants {
        const val NO_RESPONSE = "TTS did not provide a response. Please try again."
        const val GPT_TITLE_PROMPT = "Create title for the message (10 characters max) based on the message content."
        const val DEFAULT_TITLE = "audio"
    }

    suspend fun handleTtsCommand(
        messageText: String,
        sendAudio: suspend (ContentInput) -> Unit,
        sendMessage: suspend (String) -> Unit,
        input: (File) -> ContentInput
    ) {
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

        withContext(Dispatchers.IO) {
            val audio = getAudio(request)?.takeIf { it.isNotEmpty() }
            if (audio == null) {
                sendMessage(NO_RESPONSE)
                return@withContext
            }

            val title = gptService.processPrompt(
                chatId = 0,
                username = "TTS",
                prompt = GPT_TITLE_PROMPT + messageText,
                useMemory = false
            ) ?: DEFAULT_TITLE

            val tempFile = File.createTempFile(title, ".mp3").apply {
                deleteOnExit()
            }

            Files.write(tempFile.toPath(), audio)
            sendAudio(input(tempFile))
        }
    }

    private fun getAudio(request: TtsRequestDTO): ByteArray? {
        for (i in ttsProperties.token.indices) {
            val audio = ttsClient.generateSpeech(
                apiKey = ttsProperties.token[i],
                voiceId = ttsProperties.voiceId,
                request = request
            )
            if (audio != null) {
                return audio
            }
        }
        return null
    }
}
