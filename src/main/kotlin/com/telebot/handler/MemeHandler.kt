package com.telebot.handler

import com.telebot.client.RedditClient
import com.telebot.enums.SubCommand
import com.telebot.service.SubredditService
import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.handler.BotHandler

@HandlerComponent
class MemeHandler(
    private val redditClient: RedditClient,
    private val subredditService: SubredditService
) : BotHandler({
    command("/meme") {
        val chatId = message.chat.id
        val args = message.text?.split(" ") ?: emptyList()
        val subCommand = args.getOrNull(1)?.lowercase()
        val subredditName = args.getOrNull(2)?.removePrefix(REDDIT_URL)

        when (subCommand) {
            SubCommand.LIST.name.lowercase() -> {
                subredditService.findByChatId(chatId).takeIf { it.isNotEmpty() }
                    ?.joinToString("\n") { it.subredditName }
                    ?.let { sendMessage(it) }
                    ?: sendMessage(EMPTY_SUBREDDIT_LIST)
            }

            SubCommand.ADD.name.lowercase() -> subredditName
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
                val subreddit =
                    subredditService.findByChatId(chatId).takeIf { it.isNotEmpty() }?.randomOrNull()?.subredditName
                if (subreddit == null) {
                    sendMessage(NO_SUBREDDITS_FOUND)
                } else {
                    val memeUrl = redditClient.getRedditMemes(subreddit, 1)?.data?.getOrNull(0)?.url
                    memeUrl?.let {
                        when {
                            it.endsWith(
                                ".gif",
                                true
                            ) -> sendAnimation(it)

                            it.endsWith(
                                ".mp4",
                                true
                            ) -> sendVideo(it)

                            it.endsWith(
                                ".jpg",
                                true
                            ) || it.endsWith(
                                ".jpeg",
                                true
                            ) || it.endsWith(
                                ".png",
                                true
                            ) -> sendPhoto(it)

                            else -> sendMessage(UNSUPPORTED_MEDIA_TYPE.format(it))
                        }
                    } ?: sendMessage(NO_MEMES_FOUND)
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
        const val NO_MEMES_FOUND = "No memes found"
        const val UNSUPPORTED_MEDIA_TYPE = "Unsupported media type: %s"
    }
}
