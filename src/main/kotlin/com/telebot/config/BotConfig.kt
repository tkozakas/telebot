package com.telebot.config

import eu.vendeli.spring.starter.BotConfiguration
import eu.vendeli.tgbot.types.internal.LogLvl
import eu.vendeli.tgbot.utils.BotConfigurator
import org.springframework.context.annotation.Configuration

@Configuration
class BotConfig : BotConfiguration() {
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
