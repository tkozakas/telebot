package com.telebot.util

import com.telebot.enums.Command
import com.telebot.model.Stat
import com.telebot.model.Sticker
import com.telebot.model.Subreddit
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PrinterUtil(
    @Value("\${daily-message.alias}") private val alias: String
) {

    fun printStats(
        header: String,
        stats: Map<Long, Stat>,
        year: String,
        footer: String? = null,
        bodyTemplate: String
    ): String {
        val body = stats.entries
            .sortedByDescending { it.value.score ?: 0L }
            .withIndex()
            .joinToString("\n") { (index, entry) ->
                val (_, stat) = entry
                bodyTemplate.format(
                    index + 1,
                    (if (stat.isWinner == true) "👑 " else "") + stat.user.telegramUsername,
                    stat.score
                )
            }

        val formattedHeader = "*$header*"
        val finalFooter = footer?.let { "\n\n*$it*" } ?: ""
        return "$formattedHeader\n\n$body$finalFooter".trim()
    }

    fun printHelp(): String {
        return """
            |*Available Commands:*
            |
            |${
            Command.values().filterNot { command -> command.listExcluded }.joinToString("\n") {
                "- `${it.command.format(alias)}` — ${it.description} `<${
                    it.subCommands.joinToString(", ").lowercase()
                }>`"
            }
        }
            """.trimMargin()
    }

    fun printStickers(stickers: List<Sticker>): String {
        val uniqueStickerSetNames = stickers
            .mapNotNull { it.stickerSetName }
            .toSet()
            .toList()

        return """
        |*Available Sticker Sets:*
        |
        |${
            uniqueStickerSetNames.joinToString("\n") { stickerSetName ->
                "- `${stickerSetName}`"
            }
        }
        """.trimMargin()
    }


    fun printSubreddits(subreddits: MutableSet<Subreddit>): String {
        return """
                |*Subreddits:*
                |
                |${
            subreddits.joinToString("\n") { subreddit ->
                "- `${subreddit.subredditName}`"
            }
        }
        """.trimMargin()

    }
}
