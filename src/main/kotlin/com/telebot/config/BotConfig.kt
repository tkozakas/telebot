package com.telebot.config

import com.telebot.handler.CustomExceptionHandler
import io.github.dehuckakpyt.telegrambot.annotation.EnableTelegramBot
import io.github.dehuckakpyt.telegrambot.config.TelegramBotConfig
import io.github.dehuckakpyt.telegrambot.ext.strategy.invocation.smartSync
import io.github.dehuckakpyt.telegrambot.strategy.invocation.HandlerInvocationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableTelegramBot
@Configuration
class BotConfig {
    @Bean
    fun telegramBotConfig(): TelegramBotConfig = TelegramBotConfig().apply {
        receiving {
            invocationStrategy = { HandlerInvocationStrategy.smartSync }
            exceptionHandler = { CustomExceptionHandler(bot = telegramBot) }
        }
    }
}
