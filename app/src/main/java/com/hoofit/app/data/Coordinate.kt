package com.hoofit.app.data

import java.io.Serializable

class Coordinate : Serializable {
    var latitude = 0.0
    var longitude = 0.0

    constructor()
    constructor(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }
}
