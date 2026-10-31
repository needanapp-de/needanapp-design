package de.needanapp.design.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.needanapp.design.R
import dev.chrisbanes.haze.HazeState

/**
 * A screen below the main one, e.g. about, a form or a viewer: the [AppScaffold] with a back arrow that runs [onBack]
 * and no navigation pill. A [bottomBar] can hold e.g. a [FloatingItemCard].
 */
@Composable
fun SubScreen(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable BoxScope.(HazeState) -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    AppScaffold(
        title = title,
        modifier = modifier,
        subtitle = subtitle,
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.nd_back)) } },
        actions = actions,
        bottomBar = bottomBar,
        content = content,
    )
}
