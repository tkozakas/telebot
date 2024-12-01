package com.telebot.enums

sealed class Command(
    val command: String,
    val listExcluded: Boolean = false
) {
    companion object {
        const val PREFIX = "/"

        fun values() = listOf(Help, Start, GPT, Meme, Sticker, Menu)
        fun fromCommand(selectedCommandData: String): Command {
            return values().find { it.command == selectedCommandData } ?: Help
        }
    }

    data object Help : Command("${PREFIX}help", true)
    data object Start : Command("${PREFIX}start", true)
    data object GPT : Command("${PREFIX}gpt")
    data object Meme : Command("${PREFIX}meme")
    data object Sticker : Command("${PREFIX}sticker")
    data object Menu : Command("${PREFIX}menu")
    data object Fact : Command("${PREFIX}fact")
    data object DailyMessage : Command(PREFIX)
}
