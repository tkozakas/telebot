package com.telebot.enums

import com.telebot.properties.CommandProperties
import org.springframework.stereotype.Component

@Component
class CommandRegistry(props: CommandProperties) {
    val GPT = props.gpt
    val MEME = props.meme
    val STICKER = props.sticker
    val FACT = props.fact
    val TTS = props.tts
    val DAILY_MESSAGE = props.alias
    val HELP = props.help
    val START = props.start
}