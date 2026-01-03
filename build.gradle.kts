/*
 * Copyright 2026 Proify
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false

    id("com.vanniktech.maven.publish") version "0.30.0" apply false
}

extra["appPackageName"] = "io.github.proify.lyricon"
extra["appVersionCode"] = 1
extra["appVersionName"] = "1.0.0-dev"
extra["compileSdkVersion"] = 36
extra["targetSdkVersion"] = 36
extra["minSdkVersion"] = 28
extra["buildTime"] = System.currentTimeMillis()