package com.telebot.config

import eu.vendeli.spring.starter.BotConfiguration
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.types.internal.LogLvl
import eu.vendeli.tgbot.utils.BotConfigurator
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BotConfig(
    @Value("\${ktgram.bot[0].token}") private val token: String
) : BotConfiguration() {

    @Bean
    fun telegramBot(): TelegramBot = TelegramBot(token)

    override fun applyCfg(): BotConfigurator = {
        logging {
            botLogLevel = LogLvl.INFO
        }
        commandParsing {
            commandDelimiter = '/'
            commandDelimiter = ' '
            restrictSpacesInCommands = true
        }
    }
}
