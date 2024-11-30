package com.telebot.client

import com.telebot.dto.QuoteDTO
import com.telebot.dto.ShitPostDTO
import com.telebot.config.FeignConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam


@FeignClient(
    name = "shitpostingClient",
    url = "https://api.thedailyshitpost.net",
    configuration = [FeignConfig::class]
)
interface ShitpostingClient {

    @GetMapping("/random")
    fun getRandomShitpost(): ShitPostDTO

    @GetMapping("/search")
    fun searchShitpost(@RequestParam("search") search: String): ShitPostDTO

    @GetMapping("/quote/random")
    fun getRandomQuote(): QuoteDTO
}
