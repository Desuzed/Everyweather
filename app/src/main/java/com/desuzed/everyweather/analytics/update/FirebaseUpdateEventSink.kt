package com.desuzed.everyweather.analytics.update

import android.content.Context
import com.desuzed.everyweather.analytics.Analytics

class FirebaseUpdateEventSink(context: Context) : Analytics(context), UpdateEventSink {
    override fun record(event: String, parameters: Map<String, String>) {
        logEvent(event, parameters)
    }
}
