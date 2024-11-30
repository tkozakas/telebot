package com.telebot.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "stickers")
open class Sticker {
    @Id
    @Column(name = "sticker_id", nullable = false)
    open var id: UUID? = null

    @Column(name = "emoji")
    open var emoji: String? = null

    @Column(name = "file_id")
    open var fileId: String? = null

    @Column(name = "file_size")
    open var fileSize: Int? = null

    @Column(name = "is_animated")
    open var isAnimated: Boolean? = null

    @Column(name = "is_video")
    open var isVideo: Boolean? = null

    @Column(name = "set_name")
    open var setName: String? = null
}
