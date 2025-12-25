package io.github.proify.lyricon.app

import android.content.Context

fun Any.getDefaultSharedPreferences(context: Context) = context.getSharedPreferences(
    context.getPackageName() + "_preferences",
    Context.MODE_PRIVATE
)