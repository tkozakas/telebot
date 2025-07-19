package com.telebot.repository

import com.telebot.model.GptHistory
import org.springframework.data.repository.CrudRepository

interface GptHistoryRepository : CrudRepository<GptHistory, Long>