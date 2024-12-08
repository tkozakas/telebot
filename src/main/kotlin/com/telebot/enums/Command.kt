package com.telebot.enums

sealed class Command(
    val command: String,
    val description: String,
    val listExcluded: Boolean = false,
    val subCommands: List<SubCommand> = emptyList()
) {
    companion object {
        const val PREFIX = "/"

        fun values() = listOf(Help, Start, GPT, Meme, Sticker, Fact, DailyMessage)

        fun fromCommand(selectedCommandData: String): Command {
            return values().find { it.command == selectedCommandData } ?: Help
        }

        fun isCommand(text: String?): Boolean {
            return text?.startsWith(PREFIX) == true
        }
    }

    data object Help : Command(
        "${PREFIX}help",
        "Displays this help message",
        true,
        emptyList()
    )

    data object Start : Command(
        "${PREFIX}start",
        "Starts the bot",
        true,
        emptyList()
    )

    data object GPT : Command(
        "${PREFIX}gpt",
        "Interact with the GPT model",
        subCommands = listOf(SubCommand.MEMORY, SubCommand.FORGET)
    )

    data object Meme : Command(
        "${PREFIX}meme",
        "Manage memes",
        subCommands = listOf(SubCommand.LIST, SubCommand.ADD, SubCommand.REMOVE)
    )

    data object Sticker : Command(
        "${PREFIX}sticker",
        "Manage stickers",
        subCommands = listOf(SubCommand.LIST, SubCommand.ADD, SubCommand.REMOVE)
    )

    data object Fact : Command(
        "${PREFIX}fact",
        "Add or manage facts",
        subCommands = listOf(SubCommand.ADD)
    )

    data object Tts : Command(
        "${PREFIX}tts",
        "Text-to-speech",
        subCommands = emptyList()
    )

    data object DailyMessage : Command(
        "$PREFIX%s",
        "Daily message",
        subCommands = listOf(SubCommand.REGISTER, SubCommand.ALL, SubCommand.STATS)
    )
}
