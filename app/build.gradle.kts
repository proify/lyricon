import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    kotlin("plugin.serialization") version "2.1.21"
    id("io.github.philkes.auto-translation") version "+"
}

apply(from = "language-generator.gradle")

val versionCode = rootProject.extra["appVersionCode"] as Int
val versionName = rootProject.extra["appVersionName"] as String
val buildTime = rootProject.extra["buildTime"] as Long

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

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                abiFilters += listOf("arm64-v8a", "armeabi-v7a")
            }
        }

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
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

autoTranslate {
    sourceLanguage = "zh-CN"
    targetLanguages = listOf(
        "ar",
        "bn",
        "cs",
        "de",
        "el",
        "en",
        "en-GB",
        "es",
        "fa",
        "fr",
        "hi",
        "hu",
        "in",
        "it",
        "iw",
        "ja",
        "ko",
        "ms",
        "nl",
        "pl",
        "pt",
        "pt-BR",
        "ru",
        "sv",
        "th",
        "tl",
        "tr",
        "uk",
        "vi",
        "zh-CN",
        "zh-HK",
        "zh-MO",
        "zh-SG",
        "zh-TW"
    )
    excludeLanguages = listOf("zh-CN")

    translateStringsXml {
        enabled = true
        resDirectory = project.layout.projectDirectory.dir("src/main/res")
    }

    provider = libreTranslate {
        baseUrl = "http://127.0.0.1:5000/"
    }

}

dependencies {
    implementation(project(":bridge"))
    implementation(project(":common"))
    implementation(project(":lyric:style"))
    // Kotlin 和 AndroidX 库
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.ktx)

    // 第三方库
    implementation(libs.miuix.android)
    implementation(libs.accompanist.drawablepainter)
    implementation(libs.androidx.browser)
    implementation(libs.chrisbanes.haze)
    implementation(libs.bonsai.core)
    implementation(libs.androidx.compose.material.icons.core)
    implementation("io.github.kyant0:backdrop:1.0.2")
    implementation("sh.calvin.reorderable:reorderable:3.0.0")
    implementation("com.mocharealm.gaze:capsule-android:2.1.1-patch2")

    // Xposed
    implementation(libs.yukihookapi.api)
    implementation(libs.kavaref.core)
    implementation(libs.kavaref.extension)

    // AndroidX Lifecycle 和 Activity Compose
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.foundation:foundation:1.6.0")
    implementation("androidx.compose.foundation:foundation-layout:1.6.0")
    implementation(libs.androidx.room.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation(libs.androidx.appcompat)
    // 测试依赖
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug 依赖
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}