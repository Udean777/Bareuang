import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Release credentials come from the ignored local properties file or CI secrets.
val keystoreProps = Properties()
rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use {
    keystoreProps.load(it)
}
val releaseStorePath = keystoreProps["storeFile"]?.toString() ?: System.getenv("KEYSTORE_FILE")
val releaseStoreFile = releaseStorePath?.let { path ->
    file(path).takeIf { it.exists() } ?: rootProject.file(path.removePrefix("/"))
}
val releaseStorePassword = System.getenv("KEYSTORE_PASSWORD") ?: keystoreProps["storePassword"] as String?
val releaseKeyAlias = System.getenv("KEY_ALIAS") ?: keystoreProps["keyAlias"] as String?
val releaseKeyPassword = System.getenv("KEY_PASSWORD") ?: keystoreProps["keyPassword"] as String?
val releaseVersionCodeInput = System.getenv("VERSION_CODE")
val releaseVersionNameInput = System.getenv("VERSION_NAME")

android {
    namespace = "com.ssajudn.bareuang"
    compileSdk {
        version = release(37)
    }

    signingConfigs {
        create("release") {
            storeFile = releaseStoreFile
            storePassword = releaseStorePassword
            keyAlias = releaseKeyAlias
            keyPassword = releaseKeyPassword
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    defaultConfig {
        applicationId = "com.ssajudn.bareuang"
        minSdk = 26
        targetSdk = 37
        versionCode = releaseVersionCodeInput?.toIntOrNull() ?: 1
        versionName = releaseVersionNameInput ?: "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.all { it.jvmArgs("-Dnet.bytebuddy.experimental=true") }
        // android.util.Log (used by data-layer mappers) returns defaults in JVM tests.
        unitTests.isReturnDefaultValues = true
    }
}

val validateReleaseInputs = tasks.register("validateReleaseInputs") {
    group = "verification"
    description = "Checks required signing and version inputs before creating a release artifact."
    doLast {
        val missing = buildList {
            if (releaseStoreFile == null || !releaseStoreFile.isFile) add("KEYSTORE_FILE / storeFile")
            if (releaseStorePassword.isNullOrBlank()) add("KEYSTORE_PASSWORD / storePassword")
            if (releaseKeyAlias.isNullOrBlank()) add("KEY_ALIAS / keyAlias")
            if (releaseKeyPassword.isNullOrBlank()) add("KEY_PASSWORD / keyPassword")
            if (releaseVersionCodeInput?.toIntOrNull()?.let { it > 0 } != true) add("VERSION_CODE (positive integer)")
            if (releaseVersionNameInput.isNullOrBlank()) add("VERSION_NAME")
        }
        check(missing.isEmpty()) {
            "Release build refused. Set valid values for: ${missing.joinToString()}."
        }
        check(releaseVersionNameInput!!.matches(Regex("\\d+\\.\\d+\\.\\d+(-[A-Za-z0-9.-]+)?"))) {
            "VERSION_NAME must use semantic version format, for example 1.2.3 or 1.2.3-beta.1."
        }
    }
}

tasks.configureEach {
    if (name == "bundleRelease" || name == "assembleRelease") {
        dependsOn(validateReleaseInputs)
    }
}

// Module-wide opt-in so no file needs @OptIn for Material 3 APIs.
// MaterialExpressiveTheme, ShortNavigationBar and WideNavigationRail are all
// still annotated as experimental in material3 1.4.0.
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        optIn.addAll(
            "androidx.compose.material3.ExperimentalMaterial3Api",
            "androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
        )
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":presentation"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navigation & ViewModel
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)

    // Hilt (Dependency Injection)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)
    implementation(libs.androidx.work.runtime.ktx)

    // Home screen widget (Glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    testImplementation(libs.junit)
    // Required only by repository integration fakes in app unit tests; production
    // Room implementation remains encapsulated by :data.
    testImplementation(libs.androidx.room.runtime)
    testImplementation(libs.androidx.room.ktx)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    // The BOM must be applied to the androidTest configuration too, otherwise the
    // versionless Compose test artifacts below cannot resolve — which failed the
    // build for `lint` and any instrumented test run.
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    
    implementation(libs.androidx.palette.ktx)
}
