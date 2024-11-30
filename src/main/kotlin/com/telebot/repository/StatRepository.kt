package com.telebot.repository

import com.telebot.model.Stat
import org.springframework.data.jpa.repository.JpaRepository

interface StatRepository : JpaRepository<Stat, Long>
