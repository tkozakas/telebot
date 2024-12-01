package com.telebot.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class RedditResponseDTO(
    val memes: List<RedditPostDTO> = emptyList()
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class RedditPostDTO(
        val title: String? = "",
        var url: String? = "",
        val author: String? = ""
    )
}
