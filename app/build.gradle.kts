import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    kotlin("plugin.serialization") version "2.1.21"
}

apply(from = "language-generator.gradle")

val versionCode: Int = rootProject.extra["appVersionCode"] as Int
val versionName: String = rootProject.extra["appVersionName"] as String
val buildTime: Long = rootProject.extra["buildTime"] as Long

android {
    namespace = "io.github.proify.lyricon.app"
    compileSdk {
        version = release(rootProject.extra.get("compileSdkVersion") as Int)
    }
    defaultConfig {
        minSdk = rootProject.extra.get("minSdkVersion") as Int

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("int", "VERSION_CODE", versionCode.toString())
        buildConfigField("String", "VERSION_NAME", "\"" + versionName + "\"")
        buildConfigField("long", "BUILD_TIME", System.currentTimeMillis().toString())
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        buildConfig = true
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

}

dependencies {

    implementation(project(":bridge"))
    implementation(project(":common"))
    implementation(project(":lyric:style"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.ktx)

    implementation(libs.miuix.android)
    implementation(libs.accompanist.drawablepainter)
    implementation(libs.androidx.browser)
    implementation(libs.chrisbanes.haze)
    implementation(libs.bonsai.core)
    implementation(libs.androidx.compose.material.icons.core)

    implementation("io.github.kyant0:backdrop:1.0.4")

    implementation("com.mocharealm.gaze:capsule-android:2.1.1-patch2")
    implementation("androidx.appcompat:appcompat-resources:1.7.1")

    // Xposed
    implementation(libs.yukihookapi.api)
    implementation(libs.kavaref.core)
    implementation(libs.kavaref.extension)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.foundation:foundation:1.10.0")
    implementation("androidx.compose.foundation:foundation-layout:1.10.0")
    implementation(libs.androidx.room.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}