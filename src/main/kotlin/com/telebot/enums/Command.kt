package com.telebot.enums


sealed class Command(
    val command: String
) {
    companion object {
        private const val PREFIX = "/"

        fun values() = listOf(Help, Start, GPT, Meme, Sticker)
    }

    data object Help : Command("${PREFIX}help")
    data object Start : Command("${PREFIX}start")

    data object GPT : Command(
        command = "${PREFIX}gpt"
    )

    data object Meme : Command(
        command = "${PREFIX}meme"
    )

    data object Sticker : Command(
        command = "${PREFIX}sticker"
    )
}
