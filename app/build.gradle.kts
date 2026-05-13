import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "don.t.connect"
    compileSdk = 36

    defaultConfig {
        applicationId = "don.t.connect"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 🔥 placeholders مورد نیاز کتابخانه پرداخت مایکت
        manifestPlaceholders.apply {
            this["marketApplicationId"] = "ir.mservices.market"
            this["marketBindAddress"] = "ir.mservices.market.InAppBillingService.BIND"
            this["marketPermission"] = "ir.mservices.market.BILLING"
        }
        defaultConfig {
            // ...
            buildConfigField("String", "IAB_PUBLIC_KEY", "\"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC05bM+t27vC5BJgDXyGcIotinws26inGHiYIY7tZglBO0Dz4xs3cBxYe3zWtsu4sfkcPGiZi+n71TsdCQFOCzFlN/CZnh9hGBAeprqUNxzRo5Br3nByQtTqgOHfyaW53b5R4GHeP5OuP95JPKCSoDoVG4MN7A3Dmi/tuH0/R09wQIDAQAB\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true   // 🔥 فعال کردن تولید فایل BuildConfig
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("11")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.sdk)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.mpandroidchart)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.ui:ui:1.10.0")
    implementation("androidx.compose.ui:ui-graphics:1.10.0")

    implementation("com.github.myketstore:myket-billing-client:1.19")

}