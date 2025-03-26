package com.desuzed.everyweather.presentation.features.main_activity

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.os.ConfigurationCompat
import androidx.core.view.WindowCompat
import com.desuzed.everyweather.Config
import com.desuzed.everyweather.R
import com.desuzed.everyweather.data.repository.providers.action_result.GeoActionResultProvider
import com.desuzed.everyweather.domain.model.app_update.InAppUpdateStatus
import com.desuzed.everyweather.domain.model.result.ActionResult
import com.desuzed.everyweather.domain.model.result.ActionType
import com.desuzed.everyweather.domain.model.settings.DarkMode
import com.desuzed.everyweather.ui.extensions.collectAsStateWithLifecycle
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.collect
import com.desuzed.everyweather.util.setAppLocaleAndReturnContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    // private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModel<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Everyweather)//todo поменять сплешскрин на компоуз версию чтобы не видеть белый фон при входе в приложение
        super.onCreate(savedInstanceState)
        setContent {
            EveryweatherTheme {
                val activityState by viewModel.state.collectAsStateWithLifecycle(
                    initialState = MainActivityState()
                )
                MainActivityScreen(activityState)
            }
        }
        handleEdgeToEdge()
        handleFirstEnterApp()
        collectData()
        viewModel.startListeningForUpdates()
    }

    fun showSnackbar(
        message: String,
        @StringRes actionStringId: Int = R.string.ok,
        onActionClick: () -> Unit = {},
    ) {
//        snackbar(
//            text = message,
//            root = binding.root,
//            actionStringId = actionStringId,
//            onActionClick = onActionClick,
//        )
    }

    fun findUserLocation() {
        viewModel.findUserLocation()
    }

    //TODO переделать получение пермишенов
    fun requestLocationPermissions() {
        if (viewModel.areLocationPermissionsGranted()) {
            return
        } else {
            ActivityCompat
                .requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    LOCATION_CODE
                )
        }
    }

    //todo
    private fun changeDarkMode(darkMode: DarkMode) {
        val mode = when (darkMode) {
            DarkMode.ON -> MODE_NIGHT_YES
            DarkMode.OFF -> MODE_NIGHT_NO
            DarkMode.SYSTEM -> MODE_NIGHT_FOLLOW_SYSTEM
        }
        //  delegate.localNightMode = mode
    }

    private fun handleFirstEnterApp() {
        val isFirstRun = viewModel.isFirstRun()
        if (isFirstRun) {
            initFirstRunLanguage()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        val lang = getLocale().language
        viewModel.onLanguage(lang)
        super.onConfigurationChanged(newConfig)
    }

    private fun initFirstRunLanguage() {
        val lang = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]?.language
        viewModel.onLanguage(lang)
    }

    private fun collectData() {
        collect(viewModel.messageFlow, ::onNewActionResult)
        collect(viewModel.sideEffect, ::onNewAction)
    }

    fun showUpdateDialog(status: InAppUpdateStatus) { //todo delete
        //todo action
    }

    private fun onNewActionResult(actionResult: ActionResult) {
        val onClick: () -> Unit
        val buttonTextId: Int
        when (actionResult.actionType) {
            ActionType.OK -> {
                onClick = {}
                buttonTextId = R.string.ok
            }

            ActionType.RETRY -> {
                buttonTextId = R.string.retry
                onClick = {
                    findUserLocation()
                }
            }
        }
        val provider = GeoActionResultProvider(resources)
        val message = provider.parseCode(errorCode = actionResult.code)
        showSnackbar(
            message = message,
            actionStringId = buttonTextId,
            onActionClick = onClick,
        )
    }

    private fun onNewAction(action: MainActivitySideEffect) {
        when (action) {
            is MainActivitySideEffect.ChangeLanguage -> changeAppLanguage(action.lang)
            is MainActivitySideEffect.ChangeDarkMode -> changeDarkMode(action.mode)
            MainActivitySideEffect.UpdateAvailableDialog -> showUpdateDialog(InAppUpdateStatus.READY_TO_LAUNCH_UPDATE)
            MainActivitySideEffect.UpdateReadyToInstallDialog -> showUpdateDialog(InAppUpdateStatus.READY_TO_INSTALL)
        }
    }

    private fun changeAppLanguage(lang: String) {
        val locale = Locale(lang)
        val currentLocale = getLocale()
        if (locale == currentLocale || lang.isEmpty()) {
            return
        }
        Config.lang = lang
        recreate()
    }

    private fun getLocale(): Locale =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0]
        } else {
            resources.configuration.locale
        }


    override fun attachBaseContext(newBase: Context) {
        val language = Config.lang
        val newContext = if (language.isNotBlank()) {
            setAppLocaleAndReturnContext(language, newBase)
        } else {
            newBase
        }
        super.attachBaseContext(newContext)
    }

    private fun handleEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    companion object {
        private const val LOCATION_CODE = 100
    }

}
