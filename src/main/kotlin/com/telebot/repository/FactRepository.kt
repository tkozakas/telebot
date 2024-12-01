package com.telebot.repository

import com.telebot.model.Fact
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FactRepository : JpaRepository<Fact, Long> {
    @Query(value = "SELECT fact FROM Facts fact ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    fun findRandomFact(): String
}
