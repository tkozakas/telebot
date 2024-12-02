package com.telebot.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableConfigurationProperties(
    value = [
        com.telebot.properties.GptProperties::class,
        com.telebot.properties.DailyMessageTemplate::class
    ]
)
class AppConfig
