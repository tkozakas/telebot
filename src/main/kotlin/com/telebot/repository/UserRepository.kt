package com.telebot.repository

import com.telebot.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long> {
    @Query("SELECT u FROM User u ORDER BY RANDOM() LIMIT 1")
    fun findRandomUser(): User?
}