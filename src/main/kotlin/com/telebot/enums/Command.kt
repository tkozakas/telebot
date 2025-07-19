package com.telebot.enums

sealed class Command(
    val command: String,
    val description: String,
    val listExcluded: Boolean = false,
    val subCommands: List<SubCommand> = emptyList()
) {
    companion object {
        fun values() = listOf(Help, Start, Gpt, Meme, Sticker, Fact, Tts, DailyMessage)
        fun isCommand(text: String): Boolean {
            return values().any { text.startsWith(it.command) }
        }
    }

    data object Help : Command(CommandConstants.HELP, "Displays this help message", true)
    data object Start : Command(CommandConstants.START, "Starts the bot", true)
    data object Gpt :
        Command(
            CommandConstants.GPT,
            "Interact with the GPT model",
            subCommands = listOf(SubCommand.MEMORY, SubCommand.FORGET)
        )

    data object Meme :
        Command(
            CommandConstants.MEME,
            "Manage memes",
            subCommands = listOf(SubCommand.LIST, SubCommand.ADD, SubCommand.REMOVE)
        )

    data object Sticker :
        Command(
            CommandConstants.STICKER,
            "Manage stickers",
            subCommands = listOf(SubCommand.LIST, SubCommand.ADD, SubCommand.REMOVE)
        )

    data object Fact :
        Command(CommandConstants.FACT, "Add or manage facts", subCommands = listOf(SubCommand.ADD))

    data object Tts :
        Command(CommandConstants.TTS, "Text-to-speech")

    data object DailyMessage :
        Command(
            CommandConstants.DAILY_MESSAGE,
            "Daily message",
            subCommands = listOf(SubCommand.ALL, SubCommand.STATS)
        )
}
