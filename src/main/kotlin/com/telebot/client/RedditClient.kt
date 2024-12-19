package com.telebot.client

import com.telebot.dto.RedditResponseDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    name = "redditClient",
    url = "https://meme-api.com",
    fallbackFactory = RedditClientFallbackFactory::class
)
interface RedditClient {
    @GetMapping("/gimme/{subreddit}/{count}")
    fun getRedditMemes(
        @PathVariable subreddit: String?,
        @PathVariable count: Int
    ): RedditResponseDTO
}
