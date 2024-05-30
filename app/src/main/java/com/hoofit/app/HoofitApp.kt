package com.hoofit.app

import android.app.Application
import com.hoofit.app.data.Interesting
import com.hoofit.app.data.ReserveData
import com.hoofit.app.data.Trail
import com.hoofit.app.data.User
import com.yandex.mapkit.MapKitFactory

class HoofitApp : Application() {

    companion object {
        var user: User? = null
        var reserves: ReserveData? = null
        var allTrails: MutableList<Trail>? = null
        var interestings: MutableList<Interesting> = ArrayList()
    }

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("a075c213-cf50-41fb-994d-60f735f08f1b")
    }
}
