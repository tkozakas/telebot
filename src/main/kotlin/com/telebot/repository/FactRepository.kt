package com.telebot.repository

import com.telebot.model.Fact
import org.springframework.data.jpa.repository.JpaRepository

interface FactRepository : JpaRepository<Fact, Long>
