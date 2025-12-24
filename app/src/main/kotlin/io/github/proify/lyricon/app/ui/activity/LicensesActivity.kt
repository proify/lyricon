package io.github.proify.lyricon.app.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.proify.lyricon.app.ui.compose.AppToolBarListContainer

class LicensesActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Content() }
    }
}

@Composable
private fun Content() {
    AppToolBarListContainer { scope ->

    }
}


@Preview(showBackground = true)
@Composable
private fun LicensesContentPreview() {
    Content()
}