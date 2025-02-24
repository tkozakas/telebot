package com.telebot.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "stickers")
open class Sticker(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "sticker_id", nullable = false)
    open var id: UUID? = null,

    @Column(name = "file_id")
    open var fileId: String? = null,

    @Column(name = "emoji")
    open var emoji: String? = null,

    @Column(name = "sticker_set_name")
    open var stickerSetName: String? = null,

    @ManyToOne
    @JoinColumn(name = "chat_id")
    open var chat: Chat? = null,

)
