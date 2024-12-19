package com.telebot.client

import com.telebot.dto.RedditResponseDTO
import org.springframework.cloud.openfeign.FallbackFactory
import org.springframework.stereotype.Component

@Component
class RedditClientFallbackFactory : FallbackFactory<RedditClient> {
    override fun create(cause: Throwable): RedditClient {
        println("FallbackFactory triggered due to: ${cause.message}")
        return object : RedditClient {
            override fun getRedditMemes(subreddit: String?, count: Int): RedditResponseDTO {
                println("Providing fallback for subreddit: $subreddit")
                return RedditResponseDTO(memes = emptyList())
            }
        }
    }
}

