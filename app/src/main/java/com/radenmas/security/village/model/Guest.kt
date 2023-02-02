package com.radenmas.security.village.model

/**
 * Created by RadenMas on 15/01/2023.
 */
class Guest {
    lateinit var uid: String
    lateinit var username: String
    lateinit var desc: String
    lateinit var image: String
    var entry: Long = 0
    var exit: Long = 0

    constructor()

    constructor(
        uid: String,
        username: String,
        desc: String,
        image: String,
        entry: Long,
        exit: Long
    ) {
        this.uid = uid
        this.username = username
        this.desc = desc
        this.image = image
        this.entry = entry
        this.exit = exit
    }
}
