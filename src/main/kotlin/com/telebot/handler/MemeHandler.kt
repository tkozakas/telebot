package com.telebot.handler

import com.telebot.enums.SubCommand
import com.telebot.service.SubredditService
import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.factory.input.input
import io.github.dehuckakpyt.telegrambot.handler.BotHandler
import io.github.dehuckakpyt.telegrambot.model.telegram.InputMediaPhoto
import io.github.dehuckakpyt.telegrambot.model.telegram.InputMediaVideo
import java.io.File

@HandlerComponent
class MemeHandler(
    private val subredditService: SubredditService
) : BotHandler({
    command("/meme") {
        val chatId = message.chat.id
        val args = message.text?.split(" ") ?: emptyList()
        val subCommand = args.getOrNull(1)?.lowercase()
        var subredditName = args.getOrNull(2)?.removePrefix(REDDIT_URL)

        when (subCommand) {
            SubCommand.LIST.name.lowercase() -> {
                subredditService.findByChatId(chatId).takeIf { it.isNotEmpty() }
                    ?.joinToString("\n") { it.subredditName }
                    ?.let { sendMessage(it) }
                    ?: sendMessage(EMPTY_SUBREDDIT_LIST)
            }

            SubCommand.ADD.name.lowercase() -> subredditName
                ?.takeIf { subredditService.isValidSubreddit(chatId, it) }
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    subredditService.addSubreddit(chatId, it)
                    sendMessage(SUBREDDIT_ADDED.format(it))
                }
                ?: sendMessage(PROVIDE_SUBREDDIT_NAME)

            SubCommand.REMOVE.name.lowercase() -> subredditName
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    subredditService.removeSubreddit(chatId, it)
                    sendMessage(SUBREDDIT_REMOVED.format(it))
                }
                ?: sendMessage(PROVIDE_SUBREDDIT_NAME)

            else -> {
                subredditName = args.getOrElse(1) {
                    subredditService.findByChatId(chatId)
                        .takeIf { it.isNotEmpty() }
                        ?.randomOrNull()?.subredditName
                }

                if (subredditName == null || subredditName!!.isBlank()) {
                    sendMessage(NO_SUBREDDITS_FOUND)
                    return@command
                }

                val count = args.getOrNull(2)?.toIntOrNull() ?: 1
                val redditPosts = subredditService.getRedditMemes(subredditName!!, count)

                val mediaGroup = redditPosts.mapNotNull { post ->
                    val caption = "r/$subredditName\n${post.title} by ${post.author}"
                    val url = post.url ?: return@mapNotNull null

                    when {
                        url.endsWith(".mp4", true) -> InputMediaVideo(media = input(File(url)), caption = caption)
                        url.endsWith(".jpg", true) || url.endsWith(".jpeg", true) || url.endsWith(".png", true) ->
                            InputMediaPhoto(media = url, caption = caption)
                        else -> {
                            sendMessage(UNSUPPORTED_MEDIA_TYPE.format(url))
                            null
                        }
                    }
                }

                if (mediaGroup.isNotEmpty()) {
                    sendMediaGroup(mediaGroup)
                    subredditService.deleteTempFiles()
                } else {
                    sendMessage(NO_MEMES_FOUND)
                }
            }
        }
    }
}) {
    companion object Constants {
        const val REDDIT_URL = "https://www.reddit.com/r/"
        const val NO_SUBREDDITS_FOUND = "No subreddits found"
        const val EMPTY_SUBREDDIT_LIST = "Subreddit list is empty"
        const val PROVIDE_SUBREDDIT_NAME = "Please provide a subreddit name"
        const val SUBREDDIT_ADDED = "Subreddit %s added"
        const val SUBREDDIT_REMOVED = "Subreddit %s removed"
        const val UNSUPPORTED_MEDIA_TYPE = "Unsupported media type: %s"
        const val NO_MEMES_FOUND = "No memes found for this subreddit."
    }
}
