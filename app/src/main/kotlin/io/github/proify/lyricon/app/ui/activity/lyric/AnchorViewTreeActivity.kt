package io.github.proify.lyricon.app.ui.activity.lyric

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.compose.ui.graphics.Color
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.Node
import io.github.proify.lyricon.app.util.LyricPrefs
import io.github.proify.lyricon.app.util.Utils.commitEdit
import io.github.proify.lyricon.common.util.ViewTreeNode
import io.github.proify.lyricon.lyric.style.BasicStyle

class AnchorViewTreeActivity : BaseViewTreeActivity() {
    private val preferences by lazy { LyricPrefs.basicStylePrefs }
    private var currentAnchor: String = BasicStyle.Defaults.ANCHOR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences.registerOnSharedPreferenceChangeListener(this)
        currentAnchor =
            preferences.getString("lyric_style_base_anchor", currentAnchor) ?: currentAnchor
    }

    override fun getToolBarTitle(): String = getString(R.string.activity_anchor)

    override fun onDestroy() {
        super.onDestroy()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onTreeNodeClick(node: Node<ViewTreeNode>) {
        val value = node.content
        val id = value.id ?: return
        if (id == "status_bar" || id == currentAnchor) return

        preferences.commitEdit { putString("lyric_style_base_anchor", id) }
        currentAnchor = id
        refreshTreeDisplay()

        window.decorView.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }

    override fun getViewTreeNodeColor(node: ViewTreeNode): Color? {
        return when (node.id) {
            currentAnchor -> Color(0xFF66bb6a)
            else -> null
        }
    }
}