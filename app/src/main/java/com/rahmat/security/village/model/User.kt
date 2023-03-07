package com.rahmat.security.village.model

/**
 * Created by RadenMas on 15/01/2023.
 */
class User {
    lateinit var uid: String
    lateinit var username: String
    lateinit var phone: String
    lateinit var address: String
    lateinit var email: String
    lateinit var password: String
    lateinit var profile: String
    lateinit var role: String

    constructor()

    constructor(
        uid: String,
        username: String,
        phone: String,
        address: String,
        email: String,
        password: String,
        profile: String,
        role: String
    ) {
        this.uid = uid
        this.username = username
        this.phone = phone
        this.address = address
        this.email = email
        this.password = password
        this.profile = profile
        this.role = role
    }
}
