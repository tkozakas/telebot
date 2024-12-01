package com.telebot.client

import com.telebot.config.FeignConfig
import com.telebot.dto.QuoteResponseDTO
import com.telebot.dto.ShitPostResponseDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam


@FeignClient(
    name = "shitpostingClient",
    url = "https://api.thedailyshitpost.net",
    configuration = [FeignConfig::class]
)
interface ShitPostingClient {

    @GetMapping("/random")
    fun getRandomShitpost(): ShitPostResponseDTO

    @GetMapping("/search")
    fun searchShitpost(@RequestParam("search") search: String): ShitPostResponseDTO

    @GetMapping("/quote/random")
    fun getRandomQuote(): QuoteResponseDTO
}
