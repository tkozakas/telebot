package com.telebot.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "command")
data class CommandProperties(
    val gpt: String,
    val meme: String,
    val sticker: String,
    val fact: String,
    val tts: String,
    val alias: String,
    val help: String,
    val start: String
)