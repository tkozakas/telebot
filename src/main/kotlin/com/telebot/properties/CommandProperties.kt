package com.telebot.properties

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class CommandProperties(
    @Value("\${daily-message.alias}") val dailyMessageAlias: String
)
