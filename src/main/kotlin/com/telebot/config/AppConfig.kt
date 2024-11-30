package com.telebot.config

import com.telebot.properties.GptProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(GptProperties::class)
class AppConfig
