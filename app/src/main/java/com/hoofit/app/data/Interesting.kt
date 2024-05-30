package com.hoofit.app.data

import java.io.Serializable
import java.util.Date
import java.util.Objects

class Interesting : Serializable {
    var id: String? = null
    var type: String? = null
    var name: String? = null
    var description: String? = null
    var uri: String? = null
    private val date: Date? = null
    var trail: Trail? = null
    var reserve: Reserve? = null
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as Interesting
        return id == that.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }
}
