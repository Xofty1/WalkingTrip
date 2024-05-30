package com.hoofit.app.data

import java.io.Serializable
import java.util.Objects

class Trail : Serializable {
    var id: String? = null
    var name: String? = null
    var length = 0.0
    var difficulty: String? = null
    var timeRequired: String? = null
    var description: String? = null
    var coordinatesList: List<Coordinate>? = null
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as Trail
        return id == that.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }

    constructor()
    constructor(
        id: String?,
        name: String?,
        length: Double,
        difficulty: String?,
        timeRequired: String?,
        description: String?,
        coordinatesList: List<Coordinate>?
    ) {
        this.id = id
        this.name = name
        this.length = length
        this.difficulty = difficulty
        this.timeRequired = timeRequired
        this.description = description
        this.coordinatesList = coordinatesList
    }
}
