package com.telebot.handler

import com.telebot.enums.Command
import com.telebot.service.DailyMessageService
import com.telebot.service.FactService
import com.telebot.service.GptService
import com.telebot.service.MemeService
import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.factory.input.input
import io.github.dehuckakpyt.telegrambot.handler.BotHandler
import org.springframework.beans.factory.annotation.Value

@HandlerComponent
class CommandHandler(
    private val factService: FactService,
    private val memeService: MemeService,
    private val gptService: GptService,
    private val dailyMessageService: DailyMessageService,
    @Value("\${daily-message.alias}") private val alias: String
) : BotHandler({

    command(Command.GPT.command) {
        val chatId = message.chat.id
        val username = message.from?.username ?: "User"
        val args = message.text?.split(" ") ?: emptyList()
        val subCommand = args.getOrNull(1)?.lowercase()

        gptService.handleGptCommand(
            chatId = chatId,
            username = username,
            args = args,
            subCommand = subCommand,
            sendMessage = { text -> sendMessage(text = text) },
            sendDocument = { filePath -> sendDocument(filePath) },
            input = { file -> input(file) }
        )
    }

    command(Command.Meme.command) {
        val chatId = message.chat.id
        val args = message.text?.split(" ") ?: emptyList()

        memeService.handleMemeCommand(
            args = args,
            chatId = chatId,
            sendMessage = { text -> sendMessage(text = text) },
            sendMediaGroup = { media -> sendMediaGroup(media = media) },
            input = { file -> input(file) }
        )
    }

    command(Command.DailyMessage.command + alias) {
        val chatId = message.chat.id
        val userId = message.from?.id ?: 0
        val username = message.from?.username ?: "User"
        val args = message.text?.split(" ") ?: emptyList()
        val subCommand = args.getOrNull(1)?.lowercase()
        val year = args.getOrNull(2)?.toIntOrNull() ?: DailyMessageService.CURRENT_YEAR

        dailyMessageService.handleDailyMessage(
            chatId = chatId,
            userId = userId,
            username = username,
            subCommand = subCommand,
            year = year,
            sendMessage = { text -> sendMessage(text = text) }
        )
    }

    command(Command.Fact.command) {
        val args = message.text?.split(" ") ?: emptyList()
        val subCommand = args.getOrNull(1)?.lowercase()
        val comment = args.drop(2).joinToString(" ")

        factService.handleFactCommand(
            args = args,
            subCommand = subCommand,
            comment = comment,
            sendMessage = { text -> sendMessage(text = text) }
        )
    }

    command(Command.Help.command) {
        sendMessage(helpMessage())
    }

    command(Command.Start.command) {
        sendMessage(helpMessage())
    }
})

private fun helpMessage(): String {
    return """
        **Available Commands:**
        ${
        Command.values().joinToString("\n") {
            "- `${it.command}`: `<${it.subCommands.joinToString(", ")}>`"
        }
    }
    """.trimIndent()
}
