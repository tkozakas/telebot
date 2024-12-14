package com.telebot.client

import com.telebot.dto.RedditResponseDTO
import org.springframework.stereotype.Component

@Component
class RedditClientFallback : RedditClient {
    override fun getRedditMemes(subreddit: String?, count: Int): RedditResponseDTO {
        return RedditResponseDTO(memes = emptyList())
    }
}
