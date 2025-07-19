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
        val help = Help(registry)
        val start = Start(registry)
        val gpt = Gpt(registry)
        val meme = Meme(registry)
        val sticker = Sticker(registry)
        val fact = Fact(registry)
        val tts = Tts(registry)
        val alias = Alias(registry)

        fun values() = listOf(help, start, gpt, meme, sticker, fact, tts, alias)
    }

    class Help(registry: CommandRegistry) : Command(registry.help, "Displays this help message", true)
    class Start(registry: CommandRegistry) : Command(registry.start, "Starts the bot", true)
    class Gpt(registry: CommandRegistry) :
        Command(
            registry.gpt,
            "Interact with the GPT model",
            subCommands = listOf(SubCommand.MEMORY, SubCommand.FORGET)
        )

    class Meme(registry: CommandRegistry) :
        Command(
            registry.meme,
            "Manage memes",
            subCommands = listOf(SubCommand.LIST, SubCommand.ADD, SubCommand.REMOVE)
        )

    class Sticker(registry: CommandRegistry) :
        Command(
            registry.sticker,
            "Manage stickers",
            subCommands = listOf(SubCommand.LIST, SubCommand.ADD, SubCommand.REMOVE)
        )

    class Fact(registry: CommandRegistry) :
        Command(registry.fact, "Add or manage facts", subCommands = listOf(SubCommand.ADD))

    class Tts(registry: CommandRegistry) :
        Command(registry.tts, "Text-to-speech")

    class Alias(registry: CommandRegistry) :
        Command(
            registry.alias,
            "Roullete winner commands",
            subCommands = listOf(SubCommand.ALL, SubCommand.STATS)
        )
}