package com.telebot.config

import com.telebot.handler.ExceptionHandler
import com.telebot.handler.UpdateHandler
import io.github.dehuckakpyt.telegrambot.annotation.EnableTelegramBot
import io.github.dehuckakpyt.telegrambot.config.TelegramBotConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableTelegramBot
@Configuration
class BotConfig {

    @Bean
    fun telegramBotConfig(): TelegramBotConfig = TelegramBotConfig().apply {
        receiving {
            exceptionHandler = { ExceptionHandler(telegramBot, receiving.messageTemplate, templater) }
            updateHandling = { UpdateHandler() }
        }
    }
}
