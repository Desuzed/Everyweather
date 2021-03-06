package com.desuzed.everyweather.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.desuzed.everyweather.data.repository.RepositoryApp
import com.desuzed.everyweather.model.Event
import com.desuzed.everyweather.model.NetworkLiveData
import com.desuzed.everyweather.model.entity.UserLatLng

class MainActivityViewModel (private val repo: RepositoryApp) : ViewModel() {
    val userLatLngLiveData = MutableLiveData<UserLatLng>()
    val messageLiveData = MutableLiveData<Event<String>>()
    val toggleLookingForLocation = MutableLiveData<Boolean>(false)

    fun getNetworkLiveData(): NetworkLiveData {
        return repo.getNetworkLiveData()
    }

}