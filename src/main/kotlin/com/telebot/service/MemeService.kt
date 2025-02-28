package com.telebot.service

import com.telebot.client.RedditClient
import com.telebot.dto.RedditResponseDTO
import com.telebot.enums.SubCommand
import com.telebot.model.Subreddit
import com.telebot.model.UpdateContext
import com.telebot.util.MediaUtil
import com.telebot.util.PrinterUtil
import eu.vendeli.tgbot.api.media.sendAnimation
import eu.vendeli.tgbot.api.media.sendMediaGroup
import eu.vendeli.tgbot.api.media.sendPhoto
import eu.vendeli.tgbot.api.message.sendMessage
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.ImplicitFile
import eu.vendeli.tgbot.types.media.InputMedia
import feign.FeignException
import org.springframework.stereotype.Service

@Service
class MemeService(
    private val chatService: ChatService,
    private val redditClient: RedditClient,
    private val mediaUtil: MediaUtil,
    private val printerUtil: PrinterUtil
) : CommandService {

    companion object {
        private const val REDDIT_URL = "https://www.reddit.com/r/"
        private const val NO_SUBREDDITS_FOUND = "No subreddits found."
        private const val EMPTY_SUBREDDIT_LIST = "Subreddit list is empty."
        private const val PROVIDE_SUBREDDIT_NAME = "Please provide a valid subreddit name."
        private const val SUBREDDIT_ADDED = "Subreddit `%s` added."
        private const val SUBREDDIT_REMOVED = "Subreddit `%s` removed."
        private const val NO_MEMES_FOUND = "No memes found for this subreddit."
        private const val ALREADY_EXISTS = "Subreddit `%s` already exists."
    }

    override suspend fun handle(update: UpdateContext) {
        val subredditName = update.args.getOrNull(2)?.removePrefix(REDDIT_URL)

        when (update.subCommand?.lowercase()) {
            SubCommand.LIST.name.lowercase() -> listSubreddits(update)
            SubCommand.ADD.name.lowercase() -> addSubreddit(subredditName, update)
            SubCommand.REMOVE.name.lowercase() -> removeSubreddit(subredditName, update)
            else -> fetchAndSendMemes(subredditName, update)
        }
    }

    private suspend fun listSubreddits(update: UpdateContext) {
        val subreddits = update.chat.subreddits
        val message = if (subreddits.isEmpty()) EMPTY_SUBREDDIT_LIST else printerUtil.printSubreddits(subreddits)
        sendMessage { message }.options { parseMode = ParseMode.Markdown }.send(update.telegramChatId, update.bot)
    }

    private suspend fun addSubreddit(subredditName: String?, update: UpdateContext) {
        if (update.chat.subreddits.any { it.subredditName == subredditName }) {
            sendMessage { ALREADY_EXISTS.format(subredditName) }
                .options { parseMode = ParseMode.Markdown }
                .send(update.telegramChatId, update.bot)
            return
        }
        if (subredditName.isNullOrBlank() || !isValidSubreddit(subredditName)) {
            sendMessage { PROVIDE_SUBREDDIT_NAME }.send(update.telegramChatId, update.bot)
        } else {
            update.chat.subreddits.add(Subreddit(chat = update.chat, subredditName = subredditName))
            chatService.save(update.chat)
            sendMessage { SUBREDDIT_ADDED.format(subredditName) }
                .options { parseMode = ParseMode.Markdown }
                .send(update.telegramChatId, update.bot)
        }
    }

    private suspend fun removeSubreddit(subredditName: String?, update: UpdateContext) {
        if (subredditName.isNullOrBlank()) {
            sendMessage { PROVIDE_SUBREDDIT_NAME }.send(update.telegramChatId, update.bot)
        } else {
            update.chat.subreddits.removeIf { it.subredditName == subredditName }
            chatService.save(update.chat)
            sendMessage { SUBREDDIT_REMOVED.format(subredditName) }
                .options { parseMode = ParseMode.Markdown }
                .send(update.telegramChatId, update.bot)
        }
    }

    private suspend fun fetchAndSendMemes(subredditName: String?, update: UpdateContext) {
        val subreddit =
            update.args.getOrNull(1)?.ifBlank { subredditName } ?: update.chat.subreddits.randomOrNull()?.subredditName
        if (subreddit.isNullOrBlank()) {
            sendMessage { NO_SUBREDDITS_FOUND }.send(update.telegramChatId, update.bot)
            return
        }

        val count = update.args.getOrNull(2)?.toIntOrNull() ?: 1
        val memes = fetchRedditMemes(subreddit, count)
        if (memes.isEmpty()) {
            sendMessage { NO_MEMES_FOUND }.send(update.telegramChatId, update.bot)
            return
        }

        if (memes.size == 1) {
            sendSingleMeme(memes.first(), subreddit, update)
            mediaUtil.deleteTempFiles()
            return
        }

        val mediaGroup = memes.mapNotNull { buildMediaGroupEntry(it, subreddit) }
        if (mediaGroup.isEmpty()) {
            sendMessage { "No suitable photos to display from the fetched memes." }.send(
                update.telegramChatId,
                update.bot
            )
        } else {
            sendMediaGroup(mediaGroup).send(update.telegramChatId, update.bot)
        }

        mediaUtil.deleteTempFiles()
    }

    private suspend fun sendSingleMeme(
        meme: RedditResponseDTO.RedditPostDTO,
        subreddit: String,
        update: UpdateContext
    ) {
        val url = meme.url ?: run {
            sendMessage { NO_MEMES_FOUND }.send(update.telegramChatId, update.bot)
            return
        }

        val caption = "r/$subreddit\n${meme.title} by ${meme.author}"
        when {
            isPhoto(url) -> {
                sendPhoto(ImplicitFile.Str(url))
                    .caption { caption }
                    .send(update.telegramChatId, update.bot)
            }

            url.endsWith(".gif", true) -> {
                sendAnimation(ImplicitFile.Str(url))
                    .caption { caption }
                    .send(update.telegramChatId, update.bot)
            }

            else -> sendMessage { "Unsupported media type." }.send(update.telegramChatId, update.bot)
        }
    }

    private fun buildMediaGroupEntry(
        post: RedditResponseDTO.RedditPostDTO,
        subreddit: String
    ): InputMedia? {
        val url = post.url ?: return null
        if (isPhoto(url)) {
            val caption = "r/$subreddit\n${post.title} by ${post.author}"
            return InputMedia.Photo(media = url, caption = caption)
        }
        return null
    }

    private fun isPhoto(url: String): Boolean {
        return url.endsWith(".jpg", true) || url.endsWith(".jpeg", true) || url.endsWith(".png", true)
    }

    private fun isValidSubreddit(subreddit: String): Boolean {
        return redditClient.getRedditMemes(subreddit, 1).memes.isNotEmpty()
    }

    private suspend fun fetchRedditMemes(subreddit: String, count: Int): List<RedditResponseDTO.RedditPostDTO> {
        return try {
            redditClient.getRedditMemes(subreddit, count).memes
        } catch (e: FeignException) {
            print("Caught FeignException: ${e.message}")
            emptyList()
        }
    }

}
