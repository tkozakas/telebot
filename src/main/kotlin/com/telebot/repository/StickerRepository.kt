package com.telebot.repository

import com.telebot.model.Chat
import com.telebot.model.Sticker
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface StickerRepository : JpaRepository<Sticker, Long> {

    @Query("""
        SELECT *
          FROM stickers s
         WHERE s.chat_id = :chatId
         ORDER BY random()
         LIMIT 1
      """,
        nativeQuery = true
    )
    fun findRandomStickerByChatId(chatId: Long?): Sticker?
    fun findByChat(chat: Chat): List<Sticker>
    fun existsStickerByChatAndStickerSetName(chat: Chat, stickerSetName: String): Boolean
    fun deleteStickersByChatAndStickerSetName(chat: Chat, stickerName: String) : Int
}