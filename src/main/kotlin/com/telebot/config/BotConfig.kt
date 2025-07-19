package com.telebot.config

import com.telebot.enums.Command
import eu.vendeli.spring.starter.BotConfiguration
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.api.botactions.setMyCommands
import eu.vendeli.tgbot.types.internal.LogLvl
import eu.vendeli.tgbot.utils.BotConfigurator
import kotlinx.coroutines.Dispatchers
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BotConfig(
    @Value("\${ktgram.bot[0].token}") private val token: String,
    private val commandFactory: Command.CommandFactory
) : BotConfiguration() {

    @Bean
    fun telegramBot(): TelegramBot = TelegramBot(token)

    override fun applyCfg(): BotConfigurator = {
        logging {
            botLogLevel = LogLvl.INFO
        }
        updatesListener {
            dispatcher = Dispatchers.IO
            processingDispatcher = Dispatchers.Unconfined
            pullingDelay = 1000L
        }
        commandParsing {
            commandDelimiter = '/'
            commandDelimiter = ' '
            restrictSpacesInCommands = true
        }
        setMyCommands {
            commandFactory.values().forEach { command ->
                if (command.listExcluded) return@forEach
                botCommand(command.command, command.description)
            }
        }
    }
}
