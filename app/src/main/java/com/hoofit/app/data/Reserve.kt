package com.hoofit.app.data

import java.io.Serializable
import java.util.Objects

class Reserve : Serializable {
    var id: String? = null
    var name: String? = null
    var description: String? = null
    var trails: MutableList<Trail>? = null

    constructor(id: String?, name: String?, description: String?, trails: MutableList<Trail>?) {
        this.id = id
        this.name = name
        this.description = description
        this.trails = trails
    }

    constructor()

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as Reserve
        return id == that.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }
}
