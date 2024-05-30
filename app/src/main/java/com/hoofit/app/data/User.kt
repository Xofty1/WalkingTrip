package com.hoofit.app.data

class User {
    var id: String? = null
    var name: String? = null
    var username: String? = null
    var email: String? = null
    var admin = false
    var likedTrails: MutableList<Trail> = ArrayList()

    constructor(
        id: String?,
        name: String?,
        username: String?,
        email: String?,
        trails: MutableList<Trail>,
        admin: Boolean
    ) {
        this.id = id
        this.name = name
        this.username = username
        this.email = email
        likedTrails = trails
        this.admin = admin
    }

    constructor()
}
