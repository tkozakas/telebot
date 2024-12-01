package com.telebot.service

import com.telebot.client.RedditClient
import com.telebot.dto.RedditResponseDTO
import com.telebot.model.Subreddit
import com.telebot.repository.SubredditRepository
import com.telebot.util.MediaUtil
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class SubredditService(
    private val redditClient: RedditClient,
    private val subredditRepository: SubredditRepository,
    private val mediaUtil: MediaUtil
) {
    fun findByChatId(chatId: Long): List<Subreddit> {
        return subredditRepository.findByChatId(chatId)
    }

    fun addSubreddit(chatId: Long, subreddit: String) {
        subredditRepository.save(
            Subreddit(
                chatId = chatId,
                subredditName = subreddit
            )
        )
    }

    fun isValidSubreddit(chatId: Long, subreddit: String): Boolean {
        return redditClient.getRedditMemes(subreddit, 1).memes.isNotEmpty()
    }

    @Transactional
    fun removeSubreddit(chatId: Long, it: String) {
        subredditRepository.deleteByChatIdAndSubredditName(chatId, it)
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

}
