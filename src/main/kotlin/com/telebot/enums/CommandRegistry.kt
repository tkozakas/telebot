package com.telebot.enums

import com.telebot.properties.CommandProperties
import org.springframework.stereotype.Component

@Component
class CommandRegistry(props: CommandProperties) {
    val gpt = props.gpt
    val meme = props.meme
    val sticker = props.sticker
    val fact = props.fact
    val tts = props.tts
    val alias = props.alias
    val help = props.help
    val start = props.start
}