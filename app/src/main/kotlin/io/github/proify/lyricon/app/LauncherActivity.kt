package io.github.proify.lyricon.app

import android.content.Intent
import android.os.Bundle
import io.github.proify.lyricon.app.ui.activity.BaseActivity
import io.github.proify.lyricon.app.ui.activity.MainActivity

class LauncherActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isTaskRoot) {
            finish()
            return
        }
        startActivity(Intent(this, MainActivity::class.java).apply {
            //  flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

}