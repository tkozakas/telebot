package com.telebot.repository

import com.telebot.model.Sticker
import org.springframework.data.jpa.repository.JpaRepository

interface StickerRepository : JpaRepository<Sticker, Long>
