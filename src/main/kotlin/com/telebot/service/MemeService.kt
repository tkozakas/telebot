package com.telebot.service

import com.telebot.client.RedditClient
import com.telebot.dto.RedditResponseDTO
import com.telebot.enums.SubCommand
import com.telebot.model.Chat
import com.telebot.model.Subreddit
import com.telebot.repository.ChatRepository
import com.telebot.util.MediaUtil
import com.telebot.util.PrinterUtil
import io.github.dehuckakpyt.telegrambot.model.telegram.InputMedia
import io.github.dehuckakpyt.telegrambot.model.telegram.InputMediaPhoto
import io.github.dehuckakpyt.telegrambot.model.telegram.InputMediaVideo
import io.github.dehuckakpyt.telegrambot.model.telegram.input.ContentInput
import org.springframework.stereotype.Service
import java.io.File

@Service
class MemeService(
    private val chatRepository: ChatRepository,
    private val redditClient: RedditClient,
    private val mediaUtil: MediaUtil,
    private val printerUtil: PrinterUtil
) {
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

    suspend fun handleMemeCommand(
        args: List<String>,
        subCommand: String?,
        chat: Chat,
        sendMessage: suspend (String) -> Unit,
        sendMediaGroup: suspend (List<InputMedia>) -> Unit,
        input: (File) -> ContentInput
    ) {
        val subredditName = args.getOrNull(2)?.removePrefix(REDDIT_URL)
        when (subCommand) {
            SubCommand.LIST.name.lowercase() -> handleListSubreddits(chat, sendMessage)
            SubCommand.ADD.name.lowercase() -> handleAddSubreddit(chat, subredditName, sendMessage)
            SubCommand.REMOVE.name.lowercase() -> handleRemoveSubreddit(chat, subredditName, sendMessage)
            else -> handleDefaultCommand(chat, args, subCommand, sendMessage, sendMediaGroup, input)
        }
    }

    private suspend fun handleListSubreddits(chat: Chat, sendMessage: suspend (String) -> Unit) {
        val subreddits = chat.subreddits
        if (subreddits.isEmpty()) {
            sendMessage(EMPTY_SUBREDDIT_LIST)
            return
        }
        return sendMessage(printerUtil.printSubreddits(subreddits))
    }

    private suspend fun handleAddSubreddit(
        chat: Chat,
        subredditName: String?,
        sendMessage: suspend (String) -> Unit
    ) {
        subredditName?.takeIf { isValidSubreddit(chat, it) }
            ?.takeIf { it.isNotBlank() }
            ?.let {
                addSubreddit(chat, it)
                sendMessage(SUBREDDIT_ADDED.format(it))
            }
            ?: sendMessage(PROVIDE_SUBREDDIT_NAME)
    }

    private suspend fun handleRemoveSubreddit(
        chat: Chat,
        subredditName: String?,
        sendMessage: suspend (String) -> Unit
    ) {
        subredditName?.takeIf { it.isNotBlank() }
            ?.let {
                removeSubreddit(chat, it)
                sendMessage(SUBREDDIT_REMOVED.format(it))
            }
            ?: sendMessage(PROVIDE_SUBREDDIT_NAME)
    }

    private suspend fun handleDefaultCommand(
        chat: Chat?,
        args: List<String>,
        subredditName: String?,
        sendMessage: suspend (String) -> Unit,
        sendMediaGroup: suspend (List<InputMedia>) -> Unit,
        input: (File) -> ContentInput
    ) {
        val subreddit = args.getOrNull(1)?.takeIf { it.isNotBlank() }
            ?: run {
                chat?.subreddits?.randomOrNull()?.subredditName ?: subredditName
            }
        if (subreddit != null && subreddit.isBlank()) {
            sendMessage(NO_SUBREDDITS_FOUND)
            return
        }

        val count = args.getOrNull(2)?.toIntOrNull() ?: 1
        val redditPosts = subreddit?.let { getRedditMemes(it, count) }

        val mediaGroup = redditPosts?.mapNotNull { post ->
            val caption = "r/$subreddit\n${post.title} by ${post.author}"
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

        if (mediaGroup != null) {
            if (mediaGroup.isNotEmpty()) {
                sendMediaGroup(mediaGroup)
                deleteTempFiles()
            } else {
                sendMessage(NO_MEMES_FOUND)
            }
        }
    }

    fun addSubreddit(chat: Chat, subreddit: String) {
        chat.subreddits.add(
            Subreddit().apply {
                this.chat = chat
                this.subredditName = subreddit
            }
        )
        chatRepository.save(chat)
    }

    fun isValidSubreddit(chat: Chat, subreddit: String): Boolean {
        return redditClient.getRedditMemes(subreddit, 1).memes.isNotEmpty()
    }

    fun removeSubreddit(chat: Chat, subreddit: String) {
        chat.subreddits.remove(chat.subreddits.find { it.subredditName == subreddit })
        chatRepository.save(chat)
    }

    fun getRedditMemes(subredditName: String, count: Int): List<RedditResponseDTO.RedditPostDTO> {
        val memes = redditClient.getRedditMemes(subredditName, count).memes

        return memes.map { post ->
            if (post.url?.endsWith(".gif", true) == true) {
                val mp4File = runCatching {
                    mediaUtil.convertGifToMp4(post.url!!)
                }.getOrNull()
                post.copy(url = mp4File ?: post.url)
            } else {
                post
            }
        }
    }

    fun deleteTempFiles() {
        mediaUtil.deleteTempFiles()
    }
}
