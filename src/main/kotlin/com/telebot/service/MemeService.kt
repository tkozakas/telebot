package com.telebot.service

import com.telebot.client.RedditClient
import com.telebot.dto.RedditResponseDTO
import com.telebot.enums.SubCommand
import com.telebot.model.Subreddit
import com.telebot.model.UpdateContext
import com.telebot.repository.SubredditRepository
import com.telebot.util.MediaUtil
import com.telebot.util.PrinterUtil
import com.telebot.util.TelegramMediaSender
import com.telebot.util.TelegramMessageSender
import eu.vendeli.tgbot.types.media.InputMedia
import feign.FeignException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemeService(
    private val redditClient: RedditClient,
    private val mediaUtil: MediaUtil,
    private val printerUtil: PrinterUtil,
    private val telegramMediaSender: TelegramMediaSender,
    private val telegramMessageSender: TelegramMessageSender,
    private val subredditRepository: SubredditRepository
) : CommandService {

    companion object {
        private const val REDDIT_URL = "https://www.reddit.com/r/"
        private const val NO_SUBREDDITS_FOUND = "No subreddits found."
        private const val NO_SUBREDDIT_FOUND = "No subreddit found with the provided name."
        private const val EMPTY_SUBREDDIT_LIST = "Subreddit list is empty."
        private const val PROVIDE_SUBREDDIT_NAME = "Please provide a valid subreddit name."
        private const val SUBREDDIT_ADDED = "Subreddit `%s` added."
        private const val SUBREDDIT_REMOVED = "Subreddit `%s` removed."
        private const val NO_MEMES_FOUND = "No memes found for this subreddit."
        private const val ALREADY_EXISTS = "Subreddit `%s` already exists."
    }

    @Transactional
    override suspend fun handle(update: UpdateContext) {
        when (update.subCommand?.lowercase()) {
            SubCommand.LIST.name.lowercase() -> handleListSubreddits(update)
            SubCommand.ADD.name.lowercase() -> handleAddSubreddit(update)
            SubCommand.REMOVE.name.lowercase() -> handleRemoveSubreddit(update)
            else -> handleFetchAndSendMemes(update)
        }
    }

    private suspend fun handleListSubreddits(update: UpdateContext) {
        val subreddits = subredditRepository.findByChat(update.chat)
        val message = if (subreddits.isEmpty()) EMPTY_SUBREDDIT_LIST else printerUtil.printSubreddits(subreddits)
        telegramMessageSender.send(update.bot, update.chat.chatId, message)
    }

    suspend fun handleAddSubreddit(update: UpdateContext) {
        val subredditName = update.args.getOrNull(2)?.removePrefix(REDDIT_URL)
        val subreddits = subredditRepository.findByChat(update.chat)
        if (subreddits.any { it.subredditName == subredditName }) {
            telegramMessageSender.send(update.bot, update.chat.chatId, ALREADY_EXISTS.format(subredditName))
            return
        }
        if (subredditName.isNullOrBlank() || !isValidSubreddit(subredditName)) {
            telegramMessageSender.send(update.bot, update.chat.chatId, PROVIDE_SUBREDDIT_NAME)
        } else {
            subredditRepository.save(Subreddit(chat = update.chat, subredditName = subredditName))
            telegramMessageSender.send(
                update.bot,
                update.chat.chatId,
                SUBREDDIT_ADDED.format(subredditName)
            )
        }
    }

    suspend fun handleRemoveSubreddit(update: UpdateContext) {
        val subredditName = update.args.getOrNull(2)?.removePrefix(REDDIT_URL)

        if (subredditName.isNullOrBlank()) {
            telegramMessageSender.send(update.bot, update.chat.chatId, PROVIDE_SUBREDDIT_NAME)
        } else {
            val deletedCount = subredditRepository.deleteSubredditByChatAndSubredditName(update.chat, subredditName)
            val message = if (deletedCount > 0) {
                SUBREDDIT_REMOVED.format(subredditName)
            } else {
                NO_SUBREDDIT_FOUND
            }
            telegramMessageSender.send(
                update.bot,
                update.chat.chatId,
                message
            )
        }
    }

    private suspend fun handleFetchAndSendMemes(update: UpdateContext) {
        val subredditName = update.args.getOrNull(2)?.removePrefix(REDDIT_URL)

        val subreddit =
            update.args.getOrNull(1)?.ifBlank { subredditName }
                ?: subredditRepository.findRandomByChatId(update.chat.chatId)?.subredditName
                ?: run {
                    telegramMessageSender.send(update.bot, update.chat.chatId, NO_SUBREDDITS_FOUND)
                    return
                }
        if (subreddit.isBlank()) {
            telegramMessageSender.send(update.bot, update.chat.chatId, NO_SUBREDDITS_FOUND)
            return
        }

        val count = update.args.getOrNull(2)?.toIntOrNull() ?: 1
        val memes = fetchRedditMemes(subreddit, count)
        if (memes.isEmpty()) {
            telegramMessageSender.send(update.bot, update.chat.chatId, NO_MEMES_FOUND)
            return
        }

        if (memes.size == 1) {
            sendSingleMeme(memes.first(), subreddit, update)
            mediaUtil.deleteTempFiles()
            return
        }

        val mediaGroup = memes.mapNotNull { buildMediaGroupEntry(it, subreddit) }
        if (mediaGroup.isEmpty()) {
            telegramMessageSender.send(
                update.bot,
                update.chat.chatId,
                "No suitable photos to display from the fetched memes."
            )
        } else {
            telegramMediaSender.sendMediaGroup(update.bot, update.chat.chatId, mediaGroup)
        }

        mediaUtil.deleteTempFiles()
    }

    private suspend fun sendSingleMeme(
        meme: RedditResponseDTO.RedditPostDTO,
        subreddit: String,
        update: UpdateContext
    ) {
        val url = meme.url ?: run {
            telegramMessageSender.send(update.bot, update.chat.chatId, NO_MEMES_FOUND)
            return
        }

        val caption = "r/$subreddit\n${meme.title} by ${meme.author}"
        when {
            isPhoto(url) -> telegramMediaSender.sendPhoto(update.bot, update.chat.chatId, url, caption)
            url.endsWith(".gif", true) -> telegramMediaSender.sendAnimation(
                update.bot,
                update.chat.chatId,
                url,
                caption
            )

            else -> telegramMessageSender.send(update.bot, update.chat.chatId, "Unsupported media type.")
        }
    }

    private fun buildMediaGroupEntry(post: RedditResponseDTO.RedditPostDTO, subreddit: String): InputMedia? {
        val url = post.url ?: return null
        return if (isPhoto(url)) InputMedia.Photo(
            media = url,
            caption = "r/$subreddit\n${post.title} by ${post.author}"
        ) else null
    }

    private fun isPhoto(url: String): Boolean {
        return url.endsWith(".jpg", true) ||
                url.endsWith(".jpeg", true) ||
                url.endsWith(".png", true)
    }

    private fun isValidSubreddit(subreddit: String): Boolean {
        return try {
            redditClient.getRedditMemes(subreddit, 1).memes.isNotEmpty()
        } catch (_: FeignException) {
            false
        }
    }

    private suspend fun fetchRedditMemes(subreddit: String, count: Int): List<RedditResponseDTO.RedditPostDTO> {
        return try {
            redditClient.getRedditMemes(subreddit, count).memes
        } catch (e: FeignException) {
            println("Caught FeignException: ${e.message}")
            emptyList()
        }
    }
}