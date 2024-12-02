package com.telebot.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "daily-message")
data class DailyMessageTemplate(
    val noStats: String,
    val statsHeader: String,
    val statsHeaderAll: String,
    val statsFooter: String,
    val userStats: String,
    val userStatsNoScore: String,
    val winnerExists: String,
    val userAlreadyRegistered: String,
    val userRegistered: String,
)
