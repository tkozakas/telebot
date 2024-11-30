package com.telebot.repository

import com.telebot.model.Subreddit
import org.springframework.data.jpa.repository.JpaRepository

interface SubredditRepository : JpaRepository<Subreddit, Long>
