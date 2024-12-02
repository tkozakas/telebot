package com.telebot.enums


sealed class Command(
    val command: String,
    val listExcluded: Boolean = false,
    val subCommands: List<SubCommand> = emptyList()
) {
    companion object {
        const val PREFIX = "/"

        fun values() = listOf(Help, Start, GPT, Meme, Sticker, Menu, Fact, DailyMessage)

        fun fromCommand(selectedCommandData: String): Command {
            return values().find { it.command == selectedCommandData } ?: Help
        }
    }

    data object Menu : Command("${PREFIX}menu", subCommands = emptyList())
    data object Help : Command("${PREFIX}help", true, emptyList())
    data object Start : Command("${PREFIX}start", true, emptyList())

    data object GPT :
        Command("${PREFIX}gpt", subCommands = listOf(SubCommand.MEMORY, SubCommand.FORGET))

    data object Meme :
        Command("${PREFIX}meme", subCommands = listOf(SubCommand.LIST, SubCommand.ADD, SubCommand.REMOVE))

    data object Sticker :
        Command("${PREFIX}sticker", subCommands = listOf(SubCommand.LIST, SubCommand.ADD, SubCommand.REMOVE))

    data object Fact :
        Command("${PREFIX}fact", subCommands = listOf(SubCommand.ADD))

    data object DailyMessage :
        Command("${PREFIX}daily", subCommands = listOf(SubCommand.REGISTER, SubCommand.ALL, SubCommand.STATS))
}

