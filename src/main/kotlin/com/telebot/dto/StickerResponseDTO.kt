package com.telebot.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.telebot.model.Sticker

@JsonIgnoreProperties(ignoreUnknown = true)
data class StickerResponseDTO(
    val ok: Boolean,
    val result: StickerSet
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class StickerSet(
        val name: String,
        val title: String,
        val stickers: Collection<Sticker> = emptyList()
    )
}

