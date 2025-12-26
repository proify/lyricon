package io.github.proify.lyricon.app.ui.activity.lyric.packagestyle.page

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.BasicComponent
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.Card
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.ScrollBehavior
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun AnimPage(scrollBehavior: ScrollBehavior) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .overScrollVertical()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        item(key = "anim_options") {
            Card(
                modifier = Modifier
                    .padding(start = 16.dp, top = 13.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth(),
            ) {
                BasicComponent(title = "3", summary = "")
            }
        }
    }
}