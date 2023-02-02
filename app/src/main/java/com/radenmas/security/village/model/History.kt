package com.radenmas.security.village.model

/**
 * Created by RadenMas on 18/01/2023.
 */
class History {
    lateinit var id: String
    lateinit var username: String
    lateinit var desc: String
    lateinit var image: String
    lateinit var status : String
    var timestamp: Long = 0

    constructor()

    constructor(
        id: String,
        username: String,
        desc: String,
        image: String,
        status : String,
        timestamp: Long
    ) {
        this.id = id
        this.username = username
        this.desc = desc
        this.image = image
        this.status = status
        this.timestamp = timestamp
    }
}
