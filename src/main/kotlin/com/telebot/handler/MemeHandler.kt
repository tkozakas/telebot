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
    val redditUrl = "https://www.reddit.com/r/";

    command("/meme", next = "meme") {
        val args = message.text?.split(" ") ?: emptyList()

        when (args.getOrNull(1)?.lowercase()) {
            SubCommand.LIST.name.lowercase() -> {
                next("list_subreddits")
                return@command
            }

            SubCommand.ADD.name.lowercase() -> {
                next("add_subreddit")
                return@command
            }
        }
    }

    step("list_subreddits") {
        val chatId = message.chat.id
        val subreddits = subredditService.findByChatId(chatId)

        if (subreddits.isEmpty()) {
            sendMessage("No subreddits found")
            return@step
        }

        val message = subreddits.joinToString("\n") { it.subredditName }
        sendMessage(message)
    }

    step("add_subreddit") {
        val chatId = message.chat.id
        val args = message.text?.split(" ") ?: emptyList()

        if (args.size < 3) {
            sendMessage("Please provide a subreddit name")
            return@step
        }

        val subreddit = args[2].contains(redditUrl).let {
            if (it) args[2].removePrefix(redditUrl) else args[2]
        }

        subredditService.addSubreddit(chatId, subreddit)
        sendMessage("Subreddit $subreddit added")
    }

    step("meme") {
        val chatId = message.chat.id
        val subreddits = subredditService.findByChatId(chatId)

        if (subreddits.isEmpty()) {
            sendMessage("No subreddits found")
            return@step
        }

        val subreddit = subreddits.random().subredditName
        val response = redditClient.getRedditMemes(subreddit, 1)
        val memeUrl = response?.data?.get(0)?.url ?: return@step

        when {
            memeUrl.endsWith(".gif", true) -> sendAnimation(memeUrl)
            memeUrl.endsWith(".mp4", true) -> sendVideo(memeUrl)
            memeUrl.endsWith(".jpg", true)
                    || memeUrl.endsWith(".jpeg", true)
                    || memeUrl.endsWith(".png", true) ->
                sendPhoto(memeUrl)

            else -> sendMessage("Unsupported media type: $memeUrl")
        }
    }
})
