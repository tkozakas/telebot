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

    fun printStats(header: String, stats: Map<Long, Stat>, year: String, footer: String? = null): String {
        val body = stats.entries
            .sortedByDescending { it.value.score ?: 0L }
            .withIndex()
            .joinToString("\n") { (index, entry) ->
                val (_, stat) = entry
                "**${index + 1}.** ${stat.username} — Score: ${stat.score ?: 0} ${if (stat.isWinner == true) "👑" else ""}"
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
        val uniqueStickerSetName = stickers.mapNotNull { it.stickerSetName }.toSet()
        return """
            |*Available Stickers:*
            |
            |${
            stickers.joinToString("\n") { sticker ->
                "- `${sticker.emoji ?: "No Emoji"}` — Pack: `${sticker.stickerSetName ?: "Unknown Set"}`"
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
