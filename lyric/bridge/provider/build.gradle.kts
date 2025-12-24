import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.vanniktech.maven.publish") version "0.35.0"
    signing
}

val version = "0.1.3-SNAPSHOT"

android {
    namespace = "io.github.proify.lyricon.provider"
    compileSdk {
        version = release(rootProject.extra.get("compileSdkVersion") as Int)
    }

    defaultConfig {
        minSdk = rootProject.extra.get("minSdkVersion") as Int

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        aidl = true
    }
}

dependencies {
    api(project(":lyric:bridge:core"))
    api(project(":lyric:model"))
    api(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

mavenPublishing {
    coordinates(
        "io.github.proify.lyricon.lyric",
        "provider",
        version
    )

    pom {
        name.set("provider")
        description.set("provider")
        inceptionYear.set("2025")
        url.set("https://github.com/proify/lyricon")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("Proify")
                name.set("Proify")
                url.set("https://github.com/proify")
            }
        }
        scm {
            url.set("https://github.com/proify/lyricon")
            connection.set("scm:git:git://github.com/proify/lyricon.git")
            developerConnection.set("scm:git:ssh://git@github.com/proify/lyricon.git")
        }
    }
    publishToMavenCentral()
    signAllPublications()
}

signing {
    useGpgCmd()
}