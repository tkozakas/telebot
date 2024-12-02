package com.telebot.service

import com.telebot.client.TtsClient
import com.telebot.dto.TtsRequestDTO
import com.telebot.properties.TtsProperties
import io.github.dehuckakpyt.telegrambot.model.telegram.input.ContentInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileOutputStream

@Service
class TtsService(
    private val ttsProperties: TtsProperties,
    private val ttsClient: TtsClient
) {
    companion object Constants {
        const val NO_RESPONSE = "TTS did not provide a response. Please try again."
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

        val audio = withContext(Dispatchers.IO) {
            ttsClient.convertTextToSpeech(
                apiKey = ttsProperties.token,
                voiceId = ttsProperties.voiceId,
                request = request
            )
        }

        audio?.let {
            val tempFile = File.createTempFile("tts_audio_", ".mp3").apply {
                FileOutputStream(this).use { fos -> fos.write(it) }
            }
            sendAudio(input(tempFile))
        }
    }

}
