package io.github.proify.lyricon.app.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import io.github.proify.lyricon.app.BuildConfig
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.compose.AppToolBarListContainer
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.BasicComponent
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.Card
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.IconActions
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.SuperArrow
import io.github.proify.lyricon.app.util.Utils.launchBrowser
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class AboutActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AboutContent()
        }
    }

    @Composable
    private fun AboutContent() {
        AppToolBarListContainer(
            title = stringResource(id = R.string.activity_about),
            canBack = true
        ) { scope ->
            scope.item {
                val context = LocalContext.current
                Card(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 0.dp)
                        .fillMaxWidth(),
                    pressFeedbackType = PressFeedbackType.Sink,
                ) {
                    val drawable =
                        AppCompatResources.getDrawable(this@AboutActivity, R.mipmap.ic_launcher)
                    Box(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .matchParentSize()
                                .blur(200.dp),
                        ) {
                            Image(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .scale(2f)
                                    .rotate(40f),
                                painter = rememberDrawablePainter(

                                    AppCompatResources.getDrawable(
                                        this@AboutActivity,
                                        R.mipmap.ic_launcher
                                    )
                                ),
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(54.dp),
                                painter = rememberDrawablePainter(drawable),
                                contentDescription = null,
                                tint = null,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(id = R.string.app_name),
                                style = MiuixTheme.textStyles.title3,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

            scope.item {
                val context = LocalContext.current

                Card(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp)
                        .fillMaxWidth(),
                    insideMargin = PaddingValues(0.dp)
                ) {
                    val buildTime = BuildConfig.BUILD_TIME
                    val buildTimeFormat = Instant.ofEpochMilli(buildTime)
                        .atZone(ZoneId.systemDefault())
                        .format(
                            DateTimeFormatter
                                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                                .withLocale(Locale.getDefault())
                        )
                    BasicComponent(
                        leftAction = { IconActions(painterResource(R.drawable.ic_info)) },
                        title = stringResource(id = R.string.item_app_version),
                        summary = stringResource(
                            id = R.string.item_app_version_summary,
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE.toString(),
                            BuildConfig.BUILD_TYPE
                        )
                    )
                    BasicComponent(
                        leftAction = { IconActions(painterResource(R.drawable.ic_build)) },
                        title = stringResource(id = R.string.item_build_time),
                        summary = buildTimeFormat,
                    )
                }
                Card(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp)
                        .fillMaxWidth(),
                    insideMargin = PaddingValues(0.dp)
                ) {
                    val url = stringResource(id = R.string.github_home)
                    val toolbarColor = MiuixTheme.colorScheme.surface.toArgb()
                    SuperArrow(
                        leftAction = { IconActions(painterResource(R.drawable.ic_github)) },
                        title = stringResource(id = R.string.item_view_on_github),
                        onClick = {
                            launchBrowser(
                                url,
                                toolbarColor
                            )
                        }
                    )
                    SuperArrow(
                        leftAction = { IconActions(painterResource(R.drawable.ic_license)) },
                        title = stringResource(id = R.string.item_open_source_license),
                        onClick = {
                            val intent = Intent(context, LicensesActivity::class.java)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun AboutContentPreview() {
        AboutContent()
    }

}