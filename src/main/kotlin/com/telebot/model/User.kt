package com.telebot.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
open class User(

    @Id
    @Column(name = "user_id", nullable = false)
    open var id: Long,

    @Column(name = "username")
    open var username: String? = null,

    @Column(name = "is_winner")
    open var isWinner: Boolean? = null,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    open var stats: MutableSet<Stat> = mutableSetOf()
)
