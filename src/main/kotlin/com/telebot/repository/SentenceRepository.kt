package com.telebot.repository

import com.telebot.model.Sentence
import org.springframework.data.jpa.repository.JpaRepository

interface SentenceRepository : JpaRepository<Sentence, Long>
