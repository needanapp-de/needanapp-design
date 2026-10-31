package de.needanapp.design.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.needanapp.design.R
import de.needanapp.design.theme.LocalDesignColors
import de.needanapp.design.theme.LocalDesignEffects
import de.needanapp.design.theme.designBackground
import de.needanapp.design.theme.emphasizedSpec
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

/** Search in the header: [isActive] turns the title into a search field. */
@Stable
class SearchState internal constructor(active: Boolean, query: String) {
    var isActive by mutableStateOf(active)
    var query by mutableStateOf(query)

    fun close() {
        isActive = false
        query = ""
    }

    internal companion object {
        val Saver = listSaver<SearchState, Any>(
            save = { listOf(it.isActive, it.query) },
            restore = { SearchState(it[0] as Boolean, it[1] as String) },
        )
    }
}

@Composable
fun rememberSearchState(): SearchState = rememberSaveable(saver = SearchState.Saver) { SearchState(active = false, query = "") }

private val HeaderHeight = 64.dp

/**
 * The app shell: edge to edge, content scrolls below a header that fades out with a progressive blur (status bar
 * included). The header shows [title] (and [subtitle]) or the search field, [actions] on the right and a magnifier when
 * [search] is given; while searching, [searchActions] sit next to the field (e.g. previous/next match) and
 * [onSearchSubmit] runs on the keyboard's search key. [headerBottom] adds a row below, e.g. a path. [bottomBar] is
 * drawn floating over the content, typically a [NavigationPill] that gets the [HazeState] for its blur. The content's
 * top padding follows the header's real height. [headerVisible] false slides the header away (full screen, e.g. a
 * picture); [backgroundColor] replaces the background gradient (e.g. black behind pictures).
 */
@Composable
fun AppScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    search: SearchState? = null,
    searchActions: @Composable RowScope.() -> Unit = {},
    onSearchSubmit: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    headerBottom: (@Composable () -> Unit)? = null,
    bottomBar: @Composable BoxScope.(HazeState) -> Unit = {},
    headerVisible: Boolean = true,
    backgroundColor: Color? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val hazeState = rememberHazeState()
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // the header grows with its optional bottom row; the content's padding follows its measured height
    var measuredHeaderHeight by remember { mutableStateOf<Dp?>(null) }
    val headerHeight = measuredHeaderHeight ?: (statusBarHeight + HeaderHeight)
    val designColors = LocalDesignColors.current
    // the gradient of the card look; it stays in place while the content scrolls
    val background = if (backgroundColor != null) Modifier.background(backgroundColor) else Modifier.designBackground(designColors)

    // text and icons outside of surfaces take the background's content color
    CompositionLocalProvider(LocalContentColor provides colors.onBackground) {
        Box(modifier.fillMaxSize()) {
            // the background belongs to what header and pill blur, otherwise they fill empty spots with their fallback color
            Box(Modifier.fillMaxSize().hazeSource(hazeState).then(background)) {
                content(PaddingValues(top = headerHeight, bottom = navigationBarHeight))
            }
            AnimatedVisibility(
                visible = headerVisible,
                enter = slideInVertically(emphasizedSpec()) { -it } + fadeIn(emphasizedSpec()),
                exit = slideOutVertically(emphasizedSpec()) { -it } + fadeOut(emphasizedSpec()),
                modifier = Modifier.align(Alignment.TopCenter),
            ) {
                Box {
                    Header(
                        title, subtitle, statusBarHeight, hazeState, navigationIcon, search, searchActions, onSearchSubmit, actions,
                        bottomRow = { headerBottom?.invoke() },
                        onHeightChanged = { measuredHeaderHeight = with(density) { it.toDp() } },
                    )
                }
            }
            bottomBar(hazeState)
        }
    }
}

@Composable
private fun BoxScope.Header(
    title: String,
    subtitle: String?,
    statusBarHeight: Dp,
    hazeState: HazeState,
    navigationIcon: (@Composable () -> Unit)?,
    search: SearchState?,
    searchActions: @Composable RowScope.() -> Unit,
    onSearchSubmit: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    bottomRow: @Composable () -> Unit,
    onHeightChanged: (Int) -> Unit,
) {
    val effects = LocalDesignEffects.current
    // the top of the background gradient, so the header blends into it
    val background = LocalDesignColors.current.background.first()
    // opaque behind status bar and title, fading out towards the content, so the text always stays readable
    val fade = Brush.verticalGradient(0f to background, FADE_SOLID_UNTIL to background.copy(alpha = FADE_MID_ALPHA), 1f to Color.Transparent)
    val headerModifier = if (effects.blur) {
        Modifier.hazeBlur(
            HazeInput.Sources(hazeState),
            HazeBlurStyle {
                blurRadius(20.dp)
                backgroundColor(background)
                progressive(HazeProgressive.verticalGradient(startIntensity = 1f, endIntensity = 0f))
            },
        ).background(fade)
    } else {
        // without blur, content would show sharply behind the title
        Modifier.background(background)
    }

    Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().onSizeChanged { onHeightChanged(it.height) }.then(headerModifier)) {
        Column(Modifier.fillMaxWidth().padding(top = statusBarHeight)) {
            Row(
                modifier = Modifier.fillMaxWidth().height(HeaderHeight).padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (search != null && search.isActive) {
                    SearchField(search, onSearchSubmit)
                    searchActions()
                } else {
                    if (navigationIcon != null) navigationIcon() else Box(Modifier.padding(start = 12.dp))
                    Column(Modifier.weight(1f).padding(horizontal = 4.dp)) {
                        // long titles (e.g. file names) scroll through instead of being cut
                        MarqueeText(
                            title,
                            style = MaterialTheme.typography.titleLarge,
                            wrapWithoutMotion = false,
                            modifier = Modifier.semantics { heading() },
                        )
                        if (!subtitle.isNullOrEmpty()) {
                            MarqueeText(
                                subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                wrapWithoutMotion = false,
                            )
                        }
                    }
                    if (search != null) {
                        IconButton(onClick = { search.isActive = true }) {
                            Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.nd_search))
                        }
                    }
                    actions()
                }
            }
            bottomRow()
        }
    }
}

@Composable
private fun RowScope.SearchField(search: SearchState, onSubmit: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    BackHandler { search.close() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    IconButton(onClick = { search.close() }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nd_search_close))
    }
    TextField(
        value = search.query,
        onValueChange = { search.query = it },
        placeholder = { Text(stringResource(R.string.nd_search_hint)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier.weight(1f).focusRequester(focusRequester),
    )
    if (search.query.isNotEmpty()) {
        IconButton(onClick = { search.query = "" }) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.nd_search_clear))
        }
    }
}

/** Top padding for a screen's own content when it isn't inside [AppScaffold]. */
@Composable
fun headerPadding(): Dp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + HeaderHeight

private const val FADE_SOLID_UNTIL = 0.35f
private const val FADE_MID_ALPHA = 0.85f
