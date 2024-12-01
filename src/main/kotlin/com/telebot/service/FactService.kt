package com.telebot.service

import com.telebot.repository.FactRepository
import org.springframework.stereotype.Service

@Service
class FactService(
    private val factRepository: FactRepository
) {
    fun getRandomFact(): String {
        return factRepository.findRandomFact()
    }
}
