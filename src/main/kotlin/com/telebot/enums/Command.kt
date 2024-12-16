package com.telebot.enums

object Commands {
    private const val PREFIX = "/"

    const val GPT = "${PREFIX}gpt"
    const val MEME = "${PREFIX}meme"
    const val STICKER = "${PREFIX}sticker"
    const val FACT = "${PREFIX}fact"
    const val TTS = "${PREFIX}tts"
    const val DAILY_MESSAGE = "${PREFIX}pidor" // TODO take from env somehow
    const val HELP = "${PREFIX}help"
    const val START = "${PREFIX}start"
}

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

    data object Help : Command(Commands.HELP, "Displays this help message", true)
    data object Start : Command(Commands.START, "Starts the bot", true)
    data object Gpt :
        Command(Commands.GPT, "Interact with the GPT model", subCommands = listOf(SubCommand.MEMORY, SubCommand.FORGET))

    data object Meme :
        Command(Commands.MEME, "Manage memes", subCommands = listOf(SubCommand.LIST, SubCommand.ADD, SubCommand.REMOVE))

    data object Sticker :
        Command(Commands.STICKER, "Manage stickers", subCommands = listOf(SubCommand.LIST, SubCommand.ADD, SubCommand.REMOVE))

    data object Fact :
        Command(Commands.FACT, "Add or manage facts", subCommands = listOf(SubCommand.ADD))

    data object Tts :
        Command(Commands.TTS, "Text-to-speech")

    data object DailyMessage :
        Command(
            Commands.DAILY_MESSAGE,
            "Daily message",
            subCommands = listOf(SubCommand.REGISTER, SubCommand.ALL, SubCommand.STATS)
        )
}
