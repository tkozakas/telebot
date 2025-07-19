package com.telebot.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SchedulerService(
    private val dailyMessageService: DailyMessageService
) {
    @Scheduled(cron = "\${schedule.winner-reset}")
    fun resetWinner() {
        dailyMessageService.resetDailyWinner()
    }
}