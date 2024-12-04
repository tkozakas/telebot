package com.telebot.config

import com.telebot.handler.CustomExceptionHandler
import io.github.dehuckakpyt.telegrambot.annotation.EnableTelegramBot
import io.github.dehuckakpyt.telegrambot.config.TelegramBotConfig
import io.github.dehuckakpyt.telegrambot.ext.strategy.invocation.chatSync
import io.github.dehuckakpyt.telegrambot.strategy.invocation.HandlerInvocationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableTelegramBot
@Configuration
class BotConfig {
    @Bean
    fun telegramBotConfig(): TelegramBotConfig = TelegramBotConfig().apply {
        receiving {
            invocationStrategy = { HandlerInvocationStrategy.chatSync }
            exceptionHandler = {
                CustomExceptionHandler(
                    bot = telegramBot
                )
            }
        }
    }
}
