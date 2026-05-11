import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.reader()?.use { load(it) }
}

fun quoteForBuildConfig(value: String): String =
    value.replace("\\", "\\\\").replace("\"", "\\\"")

fun normalizeApiUrl(raw: String): String {
    val trimmed = raw.trim()
    return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
}

fun resolveApiUrl(vararg keys: String, fallback: String): String {
    for (key in keys) {
        val value = localProps.getProperty(key)?.trim()
        if (!value.isNullOrEmpty()) {
            return normalizeApiUrl(value)
        }
    }
    return normalizeApiUrl(fallback)
}

val apiUrlDebug = resolveApiUrl("MAYA_API_BASE_URL", fallback = "http://10.0.2.2:8081/")
val apiUrlRelease = resolveApiUrl(
    "MAYA_API_BASE_URL_RELEASE",
    "MAYA_API_BASE_URL",
    fallback = "http://10.0.2.2:8081/"
)

if (apiUrlRelease.contains("10.0.2.2")) {
    logger.warn(
        "APK release ainda aponta para 10.0.2.2 (so funciona no emulador). " +
            "Defina MAYA_API_BASE_URL_RELEASE em local.properties antes de gerar o APK."
    )
}

android {
    namespace = "com.example.mayarpg"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.mayarpg"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_BASE_URL", "\"${quoteForBuildConfig(apiUrlDebug)}\"")
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"${quoteForBuildConfig(apiUrlDebug)}\"")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", "\"${quoteForBuildConfig(apiUrlRelease)}\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.kizitonwose.calendar:view:2.5.0")
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}