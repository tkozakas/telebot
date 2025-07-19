package com.telebot.handler

import eu.vendeli.tgbot.annotations.UpdateHandler
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.types.internal.UpdateType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UpdateMessageHandler {
    private val logger = LoggerFactory.getLogger(UpdateMessageHandler::class.java)

    @UpdateHandler([UpdateType.MESSAGE])
    suspend fun handleUpdate(update: ProcessedUpdate) {
        try {
            logger.info("Message received from chat: ${update.text}")
        } catch (e: IllegalStateException) {
            logger.error("Serialization failed for update ${update.updateId}: ${e.message}", e)
        } catch (e: Exception) {
            logger.warn("Failed to handle update: ${update.updateId}. Reason: ${e.message}", e)
        }
    }

}
