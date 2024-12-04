package com.telebot.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableAspectJAutoProxy
@EnableFeignClients(basePackages = ["com.telebot.client"])
@EnableConfigurationProperties(
    value = [
        com.telebot.properties.GptProperties::class,
        com.telebot.properties.DailyMessageTemplate::class,
        com.telebot.properties.TtsProperties::class
    ]
)
class AppConfig
