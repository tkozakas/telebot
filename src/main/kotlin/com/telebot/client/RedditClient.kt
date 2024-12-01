package com.telebot.client

import com.telebot.config.FeignConfig
import com.telebot.dto.RedditDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    name = "redditClient",
    url = "https://meme-api.com",
    configuration = [FeignConfig::class]
)
interface RedditClient {

    @GetMapping("/gimme/{subreddit}/{count}")
    fun getRedditMemes(
        @PathVariable subreddit: String?,
        @PathVariable count: Int
    ): RedditDTO? {
        val response = getRedditMemes(subreddit, count)
        println("Raw JSON response: $response")
        return response
    }
}
