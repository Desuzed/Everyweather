package com.desuzed.everyweather.presentation.features.main_activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.getValue
import androidx.core.os.ConfigurationCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.desuzed.everyweather.Config
import com.desuzed.everyweather.R
import com.desuzed.everyweather.data.repository.providers.action_result.GeoActionResultProvider
import com.desuzed.everyweather.domain.model.result.ActionResult
import com.desuzed.everyweather.domain.model.result.ActionType
import com.desuzed.everyweather.domain.model.settings.DarkMode
import com.desuzed.everyweather.ui.extensions.collectAsStateWithLifecycle
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.collect
import com.desuzed.everyweather.util.setAppLocaleAndReturnContext
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel by viewModel<MainActivityViewModel>()
    private val snackbarHostState = SnackbarHostState()
    private val requestLocationPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission) {
            findUserLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Everyweather)//todo поменять сплешскрин на компоуз версию чтобы не видеть белый фон при входе в приложение
        super.onCreate(savedInstanceState)
        setContent {
            val activityState by viewModel.state.collectAsStateWithLifecycle(
                initialState = MainActivityState()
            )
            EveryweatherTheme(darkTheme = activityState.darkMode.isDarkTheme()) {
                MainActivityScreen(
                    state = activityState,
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                )
            }
        }
        handleEdgeToEdge()
        handleFirstEnterApp()
        collectData()
        viewModel.startListeningForUpdates()
    }

    @Deprecated("Play's existing update flow uses a request code callback")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onUpdateActivityResult(requestCode, resultCode)
    }

    fun showSnackbar(
        message: String,
        @StringRes actionStringId: Int = R.string.ok,
        onActionClick: () -> Unit = {},
    ) {
        lifecycleScope.launch {
            if (
                snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = getString(actionStringId),
                    duration = SnackbarDuration.Long,
                ) == SnackbarResult.ActionPerformed
            ) {
                onActionClick()
            }
        }
    }

    fun findUserLocation() {
        viewModel.findUserLocation()
    }

    fun requestLocationPermissions() {
        if (viewModel.areLocationPermissionsGranted()) {
            findUserLocation()
            return
        }
        requestLocationPermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )
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
            else -> {} // handled in MainActivityScreen
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

    @androidx.compose.runtime.Composable
    private fun DarkMode.isDarkTheme(): Boolean = when (this) {
        DarkMode.ON -> true
        DarkMode.OFF -> false
        DarkMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }
}
