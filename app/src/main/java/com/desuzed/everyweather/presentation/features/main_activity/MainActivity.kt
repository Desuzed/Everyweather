package com.desuzed.everyweather.presentation.features.main_activity

import android.Manifest
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.os.ConfigurationCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.desuzed.everyweather.App
import com.desuzed.everyweather.R
import com.desuzed.everyweather.databinding.ActivityMainBinding
import com.desuzed.everyweather.util.NetworkConnection
import com.desuzed.everyweather.domain.model.UserLatLng
import com.desuzed.everyweather.util.LocationHandler
import com.desuzed.everyweather.presentation.base.AppViewModelFactory
import com.desuzed.everyweather.util.collect
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val locationHandler by lazy { LocationHandler(this, viewModel) }
    private val locationCode = 100
    private val viewModel: MainActivityViewModel by lazy {
        ViewModelProvider(
            this,
            AppViewModelFactory(App.instance.getRepo())
        )
            .get(MainActivityViewModel::class.java)
    }
    private lateinit var networkConnection : NetworkConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Everyweather)//todo поменять сплешскрин на компоуз версию чтобы не видеть белый фон при входе в приложение
        super.onCreate(savedInstanceState)
        networkConnection = NetworkConnection(this)
        bind()
        requestLocationPermissions()
        setLangForRequest()
        collectData()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.toggleLookingForLocation(true)
        locationHandler.findUserLocation()
    }

    //todo рефакторинг на нормальное взаимодействие
    fun getUserLatLngFlow(): SharedFlow<UserLatLng?> = viewModel.userLatLng.asSharedFlow()

    fun requestLocationPermissions() {
        //if (locationHandler.permissionsGranted()) return
        ActivityCompat
            .requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                locationCode
            )
    }

    private fun setLangForRequest() {
        val lang = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0].language
        App.instance.setLang(lang)
    }

    private fun collectData() {
        collect(networkConnection.hasInternetFlow(), ::onNewNetworkState)
      //  collect(viewModel.hasInternet, ::onNewNetworkState)
        collect(viewModel.isLookingForLocation, ::isLookingForLocation)
        collect(viewModel.messageFlow, ::onNewMessage)
    }

    private fun onNewNetworkState(networkState: Boolean) {
        binding.tvInternetConnection.isVisible = !networkState
    }

    private fun isLookingForLocation(isLooking: Boolean) {
        with(binding) {
            geoLayout.isVisible = isLooking
        }
    }

    private fun onNewMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun bind() {
        binding = ActivityMainBinding.inflate(
            layoutInflater
        )
        val view: View = binding.root
        setContentView(view)
    }

}