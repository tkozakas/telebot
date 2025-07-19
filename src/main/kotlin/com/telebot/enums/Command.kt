package com.telebot.enums

import org.springframework.stereotype.Component

sealed class Command(
    val command: String,
    val description: String,
    val listExcluded: Boolean = false,
    val subCommands: List<SubCommand> = emptyList()
) {
    @Component
    class CommandFactory(registry: CommandRegistry) {
        val Help = Help(registry)
        val Start = Start(registry)
        val Gpt = Gpt(registry)
        val Meme = Meme(registry)
        val Sticker = Sticker(registry)
        val Fact = Fact(registry)
        val Tts = Tts(registry)
        val DailyMessage = DailyMessage(registry)

        fun values() = listOf(Help, Start, Gpt, Meme, Sticker, Fact, Tts, DailyMessage)
        fun isCommand(text: String): Boolean {
            return values().any { text.startsWith(it.command) }
        }
    }

    class Help(registry: CommandRegistry) : Command(registry.HELP, "Displays this help message", true)
    class Start(registry: CommandRegistry) : Command(registry.START, "Starts the bot", true)
    class Gpt(registry: CommandRegistry) :
        Command(
            registry.GPT,
            "Interact with the GPT model",
            subCommands = listOf(SubCommand.MEMORY, SubCommand.FORGET)
        )

    class Meme(registry: CommandRegistry) :
        Command(
            registry.MEME,
            "Manage memes",
            subCommands = listOf(SubCommand.LIST, SubCommand.ADD, SubCommand.REMOVE)
        )

    class Sticker(registry: CommandRegistry) :
        Command(
            registry.STICKER,
            "Manage stickers",
            subCommands = listOf(SubCommand.LIST, SubCommand.ADD, SubCommand.REMOVE)
        )

    class Fact(registry: CommandRegistry) :
        Command(registry.FACT, "Add or manage facts", subCommands = listOf(SubCommand.ADD))

    class Tts(registry: CommandRegistry) :
        Command(registry.TTS, "Text-to-speech")

    class DailyMessage(registry: CommandRegistry) :
        Command(
            registry.DAILY_MESSAGE,
            "Daily message",
            subCommands = listOf(SubCommand.ALL, SubCommand.STATS)
        )
}