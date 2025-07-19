package com.telebot.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
open class User(

    @Id
    @Column(name = "user_id", nullable = false)
    open var userId: Long,

    @Column(name = "username")
    open var username: String? = null

)

