package com.desuzed.everyweather

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.test.runner.AndroidJUnitRunner
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.SuccessResult

/** Component tests own their fakes; they never initialise production Koin or network clients. */
class TestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application =
        super.newApplication(cl, Application::class.java.name, context).also { application ->
            // Weather UI formats remote icon URLs even for local fixtures. Short-circuit
            // every image request so instrumentation cannot fetch those URLs.
            Coil.setImageLoader(
                ImageLoader.Builder(application).components {
                    add(Interceptor { chain ->
                        SuccessResult(
                            drawable = ColorDrawable(Color.TRANSPARENT),
                            request = chain.request,
                            dataSource = DataSource.MEMORY_CACHE,
                        )
                    })
                }.build()
            )
        }
}
