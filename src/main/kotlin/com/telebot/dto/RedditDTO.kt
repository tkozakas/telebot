package com.telebot.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class RedditDTO(
    val data: List<RedditPostDTO> = emptyList()
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class RedditPostDTO(
        val title: String?,
        val url: String?,
        val author: String?
    )
}
