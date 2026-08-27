plugins {
    id("com.android.library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.ssajudn.bareuang.data"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    buildTypes {
        debug { buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\""); buildConfigField("String", "WEB_CLIENT_ID", "\"234922787074-fnm37va5028brlr45jmc9gvfp1ksgr17.apps.googleusercontent.com\"") }
        release { buildConfigField("String", "BASE_URL", "\"https://api.bareuang.app/\""); buildConfigField("String", "WEB_CLIENT_ID", "\"234922787074-fnm37va5028brlr45jmc9gvfp1ksgr17.apps.googleusercontent.com\"") }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    buildFeatures { buildConfig = true }
    testOptions {
        unitTests.all { it.jvmArgs("-Dnet.bytebuddy.experimental=true") }
        unitTests.isReturnDefaultValues = true
    }
}

kotlin { jvmToolchain(21) }

dependencies {
    api(project(":domain"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.google.mlkit.text.recognition)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)
    ksp(libs.hilt.compiler)

    // Translation & Preferences
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.gson)
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
}
