package com.telebot.service

import com.telebot.client.TtsClient
import com.telebot.dto.TtsRequestDTO
import com.telebot.properties.TtsProperties
import io.github.dehuckakpyt.telegrambot.model.telegram.input.ContentInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@Service
class TtsService(
    private val ttsProperties: TtsProperties,
    private val ttsClient: TtsClient,
    private val gptService: GptService,
    @Value("\${media.output-dir}") private val outputDir: String
) {
    companion object Constants {
        const val NO_RESPONSE = "TTS did not provide a response. Please try again."
        const val GPT_TITLE_PROMPT = "Create title for the message (10 charecters max):"
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
            val audio = ttsClient.generateSpeech(
                apiKey = ttsProperties.token,
                voiceId = ttsProperties.voiceId,
                request = request
            )
            val title = gptService.processPrompt(
                chatId = 0,
                username = "TTS",
                prompt = GPT_TITLE_PROMPT,
                saveInMemory = false
            )?.let { sendMessage(it) } ?: "audio"
            val path = Paths.get("$outputDir/${title}.mp3")
            if (audio != null) {
                Files.write(path, audio)
                sendAudio(input(path.toFile()))
            } else {
                sendMessage(NO_RESPONSE)
            }
        }
    }

}
