package com.telebot.service

import com.telebot.client.RedditClient
import com.telebot.dto.RedditResponseDTO
import com.telebot.enums.SubCommand
import com.telebot.handler.TelegramBotActions
import com.telebot.model.Chat
import com.telebot.model.Subreddit
import com.telebot.model.UpdateContext
import com.telebot.util.MediaUtil
import com.telebot.util.PrinterUtil
import io.github.dehuckakpyt.telegrambot.model.telegram.InputMediaPhoto
import io.github.dehuckakpyt.telegrambot.model.telegram.InputMediaVideo
import org.springframework.stereotype.Service
import java.io.File

@Service
class MemeService(
    private val chatService: ChatService,
    private val redditClient: RedditClient,
    private val mediaUtil: MediaUtil,
    private val printerUtil: PrinterUtil
) : CommandService {

    companion object {
        private const val REDDIT_URL = "https://www.reddit.com/r/"
        private const val NO_SUBREDDITS_FOUND = "No subreddits found"
        private const val EMPTY_SUBREDDIT_LIST = "Subreddit list is empty"
        private const val PROVIDE_SUBREDDIT_NAME = "Please provide a subreddit name"
        private const val SUBREDDIT_ADDED = "Subreddit %s added"
        private const val SUBREDDIT_REMOVED = "Subreddit %s removed"
        private const val UNSUPPORTED_MEDIA_TYPE = "Unsupported media type: %s"
        private const val NO_MEMES_FOUND = "No memes found for this subreddit."
    }

    override suspend fun handle(chat: Chat, update: UpdateContext) {
        val subredditName = update.args.getOrNull(2)?.removePrefix(REDDIT_URL)

        when (update.subCommand) {
            SubCommand.LIST.name.lowercase() -> listSubreddits(chat, update.bot)
            SubCommand.ADD.name.lowercase() -> addSubreddit(chat, subredditName, update.bot)
            SubCommand.REMOVE.name.lowercase() -> removeSubreddit(chat, subredditName, update.bot)
            else -> fetchAndSendMemes(chat, update.args, subredditName, update.bot)
        }
    }

    private suspend fun listSubreddits(chat: Chat, bot: TelegramBotActions) {
        val subreddits = chat.subreddits
        val message = if (subreddits.isEmpty()) EMPTY_SUBREDDIT_LIST else printerUtil.printSubreddits(subreddits)
        bot.sendMessage(message, parseMode = if (subreddits.isNotEmpty()) "Markdown" else null)
    }

    private suspend fun addSubreddit(chat: Chat, subredditName: String?, bot: TelegramBotActions) {
        if (subredditName.isNullOrBlank() || !isValidSubreddit(subredditName)) {
            bot.sendMessage(PROVIDE_SUBREDDIT_NAME)
        } else {
            chat.subreddits.add(Subreddit(chat = chat, subredditName = subredditName))
            chatService.save(chat)
            bot.sendMessage(SUBREDDIT_ADDED.format(subredditName))
        }
    }

    private suspend fun removeSubreddit(chat: Chat, subredditName: String?, bot: TelegramBotActions) {
        if (subredditName.isNullOrBlank()) {
            bot.sendMessage(PROVIDE_SUBREDDIT_NAME)
        } else {
            chat.subreddits.removeIf { it.subredditName == subredditName }
            chatService.save(chat)
            bot.sendMessage(SUBREDDIT_REMOVED.format(subredditName))
        }
    }

    private suspend fun fetchAndSendMemes(
        chat: Chat?,
        args: List<String>,
        subredditName: String?,
        bot: TelegramBotActions
    ) {
        val subreddit = args.getOrNull(1)?.ifBlank { subredditName } ?: chat?.subreddits?.randomOrNull()?.subredditName
        if (subreddit.isNullOrBlank()) {
            bot.sendMessage(NO_SUBREDDITS_FOUND)
            return
        }

        val count = args.getOrNull(2)?.toIntOrNull() ?: 1
        val memes = fetchRedditMemes(subreddit, count)
        if (memes.isEmpty()) {
            bot.sendMessage(NO_MEMES_FOUND)
            return
        }

        val mediaGroup = memes.mapNotNull { createMedia(it, subreddit, bot) }
        bot.sendMediaGroup(mediaGroup)
        mediaUtil.deleteTempFiles()
    }

    private fun isValidSubreddit(subreddit: String): Boolean {
        return redditClient.getRedditMemes(subreddit, 1).memes.isNotEmpty()
    }

    private fun fetchRedditMemes(subreddit: String, count: Int): List<RedditResponseDTO.RedditPostDTO> {
        return redditClient.getRedditMemes(subreddit, count).memes.map { post ->
            if (post.url?.endsWith(".gif", true) == true) {
                post.copy(url = mediaUtil.convertGifToMp4(post.url!!))
            } else post
        }
    }

    private suspend fun createMedia(
        post: RedditResponseDTO.RedditPostDTO,
        subreddit: String,
        bot: TelegramBotActions
    ): Any? {
        val caption = "r/$subreddit\n${post.title} by ${post.author}"
        val url = post.url ?: return null

        return when {
            url.endsWith(".mp4", true) -> {
                val inputMedia = bot.input?.invoke(File(url))
                inputMedia?.let { InputMediaVideo(media = it, caption = caption) }
            }

            url.endsWith(".jpg", true) || url.endsWith(".jpeg", true) || url.endsWith(".png", true) ->
                InputMediaPhoto(media = url, caption = caption)

            else -> {
                bot.sendMessage(UNSUPPORTED_MEDIA_TYPE.format(url))
                null
            }
        }
    }

}
