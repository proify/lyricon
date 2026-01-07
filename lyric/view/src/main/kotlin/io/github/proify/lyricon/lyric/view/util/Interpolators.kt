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
@file:Suppress("unused")

package io.github.proify.lyricon.lyric.view.util

import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

object Interpolators {

    val linear: LinearInterpolator by lazy {
        LinearInterpolator()
    }

    val accelerate: AccelerateInterpolator by lazy {
        AccelerateInterpolator()
    }

    val decelerate: DecelerateInterpolator by lazy {
        DecelerateInterpolator()
    }

    val accelerateDecelerate: AccelerateDecelerateInterpolator by lazy {
        AccelerateDecelerateInterpolator()
    }

    val fastOutSlowIn: FastOutSlowInInterpolator by lazy {
        FastOutSlowInInterpolator()
    }

    val bounce: BounceInterpolator by lazy {
        BounceInterpolator()
    }

    val anticipate: AnticipateInterpolator by lazy {
        AnticipateInterpolator()
    }

    val overshoot: OvershootInterpolator by lazy {
        OvershootInterpolator()
    }
}