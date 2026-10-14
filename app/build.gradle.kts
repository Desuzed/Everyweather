import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinParcelize)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
}

val localConfig = Properties().apply {
    rootProject.file("local.properties").takeIf { it.isFile }?.inputStream()?.use { load(it) }
}
fun configValue(name: String): String? = providers.environmentVariable(name).orNull
    ?: providers.gradleProperty(name).orNull
    ?: localConfig.getProperty(name)
fun String.asBuildConfigString(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""
val releaseStore = configValue("KEYSTORE_PATH") ?: "sign/everyweather_keystore.jks"
val releaseAlias = configValue("KEY_ALIAS")
val releasePassword = configValue("KEYSTORE_PASSWORD")
val releaseKeyPassword = configValue("KEY_PASSWORD") ?: releasePassword
val hasReleaseSigning = !releaseAlias.isNullOrBlank() && !releasePassword.isNullOrBlank()
    && rootProject.file(releaseStore).isFile

android {
    namespace = libs.versions.appPackageName.get()
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig {
        applicationId = libs.versions.appPackageName.get()
        minSdk = libs.versions.androidMinSdk.get().toInt()
        targetSdk = libs.versions.androidTargetSdk.get().toInt()
        versionCode = libs.versions.appVersion.get().toInt()
        versionName = libs.versions.appVersionName.get()
        testInstrumentationRunner = "com.desuzed.everyweather.TestRunner"
        buildConfigField("String", "WEATHER_API_KEY", (configValue("WEATHER_API_KEY") ?: "").asBuildConfigString())
        buildConfigField("String", "LOCATION_IQ_API", (configValue("LOCATION_IQ_API") ?: "").asBuildConfigString())
        manifestPlaceholders["MAPS_API_KEY"] = configValue("MAPS_API_KEY") ?: ""
    }
    sourceSets.getByName("androidTest").assets.directories.add("$projectDir/schemas")
    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStore)
                keyAlias = releaseAlias
                storePassword = releasePassword
                keyPassword = releaseKeyPassword
            }
        }
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    testOptions {
        animationsDisabled = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

val validateReleaseCredentials by tasks.registering {
    doLast {
        check(hasReleaseSigning) { "Release signing requires KEYSTORE_PATH, KEY_ALIAS and KEYSTORE_PASSWORD. Debug and tests need no signing secrets." }
        check(listOf("WEATHER_API_KEY", "LOCATION_IQ_API", "MAPS_API_KEY").all { !configValue(it).isNullOrBlank() }) {
            "Release requires WEATHER_API_KEY, LOCATION_IQ_API and MAPS_API_KEY."
        }
    }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach { dependsOn(validateReleaseCredentials) }

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.bundles.compose)
    implementation(libs.compose.navigation)
    implementation(libs.bundles.koin)
    implementation(libs.bundles.network)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.bundles.analytic)
    implementation(libs.bundles.google.services)
    implementation(libs.app.update)
    implementation(libs.kotlinx.serialization)
    implementation(libs.android.viewmodel)
    implementation(libs.androidx.appcompat)
    implementation(libs.material.views)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.composeUiTooling)
    debugImplementation(libs.compose.ui.test.manifest)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.mockwebserver)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.coroutines.test)
}
