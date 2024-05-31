package com.hoofit.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hoofit.app.data.Trail

class InfoTrailViewModel : ViewModel() {
    val trailLiveData: MutableLiveData<Trail> = MutableLiveData<Trail>()
    fun setTrail(trail: Trail?) {
        trailLiveData.setValue(trail)
    }

    fun getTrailLiveData(): LiveData<Trail> {
        return trailLiveData
    }
}
