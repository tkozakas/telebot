package com.telebot.enums


sealed class Command(
    val command: String,
    val listExcluded: Boolean = false
) {
    companion object {
        private const val PREFIX = "/"

        fun values() = listOf(Help, Start, GPT, Meme, Sticker)
    }

    data object Help : Command(
        command = "${PREFIX}help", listExcluded = true
    )

    data object Start : Command(
        command = "${PREFIX}start", listExcluded = true
    )

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
