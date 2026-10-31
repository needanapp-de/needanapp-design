package de.needanapp.design.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.needanapp.design.R
import de.needanapp.design.myzel.SegmentControl
import de.needanapp.design.myzel.TitleBeside
import de.needanapp.design.settings.ColorWorldPicker
import de.needanapp.design.settings.WorldPreviews
import de.needanapp.design.settings.withWorldAt
import de.needanapp.design.settings.worldChoices
import de.needanapp.design.theme.DesignStyle
import de.needanapp.design.theme.DesignType
import de.needanapp.design.theme.LocalDesignColors
import de.needanapp.design.theme.LocalDesignEffects
import de.needanapp.design.theme.LocalIsDarkTheme
import de.needanapp.design.theme.LocalThemeSettings
import de.needanapp.design.theme.NeedanappDefaults
import de.needanapp.design.theme.ThemeMode
import de.needanapp.design.theme.ThemeSettings
import de.needanapp.design.theme.ThemeSettingsStore
import de.needanapp.design.theme.designBackground
import de.needanapp.design.theme.isMyzelDesign
import de.needanapp.design.theme.labelCase
import kotlinx.coroutines.launch

/**
 * One step of an introduction at the first start: on top a live preview of the app, drawn smaller and closed to
 * touches, that follows every choice; below the step with its progress, title, text and buttons. The app keeps which
 * step is shown and what each one does. [OnboardingAppearance] lets choose the look, [OnboardingPoint] and
 * [OnboardingStatusRow] fill a step, [OnboardingButton]s go into [actions].
 *
 * @param step the step shown, counted from 0, of [stepCount]
 * @param stepName short name of the step above the title, e.g. "Files" in "Step 2 of 5 · Files"
 * @param preview the app as it looks now, laid out at the width of a phone screen
 * @param badge a word beside the title, e.g. "Optional"
 * @param onSkip ends the introduction at once; null hides "Skip", e.g. on the last step
 */
@Composable
fun OnboardingScreen(
    step: Int,
    stepCount: Int,
    stepName: String,
    title: String,
    preview: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    onSkip: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val design = LocalDesignColors.current
    // no Surface around it gives the text its color
    CompositionLocalProvider(LocalContentColor provides scheme.onSurface) {
        BoxWithConstraints(modifier.fillMaxSize().designBackground(design)) {
            // the step gets at most this much of the screen and scrolls within it, so the preview always stays in sight
            val stepMaxHeight = maxHeight * STEP_MAX_SHARE
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 56.dp).padding(start = 24.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(design.signal))
                    Text(
                        stringResource(R.string.nd_onboarding_preview).labelCase(),
                        style = DesignType.label,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp).weight(1f),
                    )
                    if (onSkip != null) {
                        TextButton(onClick = onSkip) { Text(stringResource(R.string.nd_onboarding_skip).labelCase()) }
                    }
                }
                PreviewFrame(Modifier.weight(1f).fillMaxWidth(), preview)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = STEP_CORNER, topEnd = STEP_CORNER))
                        .background(design.card)
                        .navigationBarsPadding()
                        .then(if (LocalDesignEffects.current.richMotion) Modifier.animateContentSize() else Modifier)
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
                ) {
                    StepProgress(step, stepCount)
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = (stepMaxHeight - STEP_CHROME_HEIGHT).coerceAtLeast(STEP_MIN_HEIGHT))
                            .verticalScrollWithBar()
                            .padding(top = 14.dp),
                    ) {
                        Text(
                            stringResource(R.string.nd_onboarding_step, step + 1, stepCount, stepName).labelCase(),
                            style = DesignType.label,
                            color = scheme.onSurfaceVariant,
                        )
                        Row(Modifier.padding(top = 6.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                title,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.weight(1f, fill = false).semantics { heading() },
                            )
                            if (badge != null) {
                                Text(
                                    badge.labelCase(),
                                    style = DesignType.labelSmall,
                                    color = scheme.onSecondaryContainer,
                                    modifier = Modifier
                                        .padding(start = 10.dp)
                                        .clip(CircleShape)
                                        .background(scheme.secondaryContainer)
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                        }
                        content()
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), content = actions)
                }
            }
        }
    }
}

/** A button at the bottom of an [OnboardingScreen]: [filled] for the step's main action, outlined for one beside it. */
@Composable
fun RowScope.OnboardingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    icon: ImageVector? = null,
) {
    val shape = if (isMyzelDesign) RoundedCornerShape(MYZEL_BUTTON_CORNER) else CircleShape
    val buttonModifier = modifier.weight(1f).heightIn(min = BUTTON_HEIGHT)
    val label: @Composable RowScope.() -> Unit = {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text.labelCase(),
            style = if (isMyzelDesign) DesignType.key else MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
    if (filled) {
        SignalButton(onClick = onClick, modifier = buttonModifier, shape = shape, content = label)
    } else {
        OutlinedButton(onClick = onClick, modifier = buttonModifier, shape = shape, content = label)
    }
}

/** One thing the app does, e.g. on the welcome step: an icon in the accent color and a line of text. */
@Composable
fun OnboardingPoint(icon: ImageVector, text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 14.dp))
    }
}

/**
 * A permission or a choice and how it stands, e.g. "Access to all files · Allowed ✓"; [done] sets it apart in the
 * accent's container color with a check mark, open ones are only outlined.
 */
@Composable
fun OnboardingStatusRow(icon: ImageVector, label: String, status: String, done: Boolean, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val content = if (done) scheme.onSecondaryContainer else scheme.onSurface
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(STATUS_CORNER))
            // open ones only outlined: a fill could be the color of the step's own surface
            .then(
                if (done) Modifier.background(scheme.secondaryContainer)
                else Modifier.border(1.dp, scheme.outlineVariant, RoundedCornerShape(STATUS_CORNER)),
            )
            .heightIn(min = 52.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = content, modifier = Modifier.padding(start = 14.dp).weight(1f))
        Text(
            status,
            style = MaterialTheme.typography.labelLarge,
            color = if (done) scheme.primary else scheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp),
        )
        if (done) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = scheme.primary, modifier = Modifier.padding(start = 4.dp).size(18.dp))
        }
    }
}

/**
 * The look to choose during an introduction: the style (with at least two [styles]), system, light or dark, and the
 * color world of the theme on screen as small tiles. Every tap is stored and shows at once, the preview included; the
 * rest stays in the appearance settings.
 *
 * @param defaultAccent the app's own accent, as for the appearance settings
 */
@Composable
fun OnboardingAppearance(
    settings: ThemeSettings = LocalThemeSettings.current,
    styles: List<DesignStyle> = emptyList(),
    defaultAccent: Color = NeedanappDefaults.Accent,
) {
    val context = LocalContext.current
    val store = remember { ThemeSettingsStore(context) }
    val scope = rememberCoroutineScope()
    fun update(transform: (ThemeSettings) -> ThemeSettings) {
        scope.launch { store.update(transform) }
    }
    val isDark = LocalIsDarkTheme.current
    val previews = remember(settings, defaultAccent) { WorldPreviews(context, settings, defaultAccent) }
    val (choices, selected) = worldChoices(settings, isDark, previews)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (styles.size > 1) {
            ChoiceLine(stringResource(R.string.nd_design)) {
                Segments(styles, settings.design, { stringResource(it.label) }) { design -> update { it.copy(design = design) } }
            }
        }
        ChoiceLine(stringResource(R.string.nd_theme_mode)) {
            Segments(ThemeMode.entries, settings.mode, { stringResource(it.label) }) { mode -> update { it.copy(mode = mode) } }
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(if (isDark) R.string.nd_world_dark else R.string.nd_world_light),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                choices.getOrNull(selected)?.let { chosen ->
                    Text(chosen.name.labelCase(), style = DesignType.label, color = MaterialTheme.colorScheme.primary)
                }
            }
            ColorWorldPicker(choices, selected, compact = true) { index -> update { it.withWorldAt(isDark, index) } }
        }
    }
}

/** The title of a choice and its segments, beside each other when they fit, otherwise the segments below. */
@Composable
private fun ChoiceLine(title: String, content: @Composable () -> Unit) {
    TitleBeside(
        title = { Text(title, style = MaterialTheme.typography.bodyLarge) },
        modifier = Modifier.fillMaxWidth(),
        content = content,
    )
}

/** Segments for one of a few [options] in the look of the style: Material's segmented buttons, or Myzel's key strip. */
@Composable
private fun <T> Segments(options: List<T>, selected: T, label: @Composable (T) -> String, onSelect: (T) -> Unit) {
    if (isMyzelDesign) {
        SegmentControl(options, selected, label, onSelect)
        return
    }
    SingleChoiceSegmentedButtonRow {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) {
                Text(label(option), maxLines = 1)
            }
        }
    }
}

/** The segments of the progress bar, filled up to the step shown. */
@Composable
private fun StepProgress(step: Int, count: Int) {
    val scheme = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().clearAndSetSemantics {}, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { index ->
            val color by animateColorAsState(if (index <= step) LocalDesignColors.current.signal else scheme.outlineVariant, label = "step")
            Box(Modifier.weight(1f).height(4.dp).clip(CircleShape).background(color))
        }
    }
}

/**
 * The preview: [preview] laid out as wide as a phone screen and drawn smaller inside a card, so it looks like the app
 * itself. It only shows; touches go nowhere and TalkBack reads it as one picture.
 */
@Composable
private fun PreviewFrame(modifier: Modifier, preview: @Composable () -> Unit) {
    val description = stringResource(R.string.nd_onboarding_preview_description)
    BoxWithConstraints(modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp), contentAlignment = Alignment.TopCenter) {
        val frameWidth = minOf(maxWidth * PREVIEW_WIDTH_SHARE, PREVIEW_MAX_WIDTH)
        val scale = frameWidth / PREVIEW_SCREEN_WIDTH
        val screenHeight = maxHeight / scale
        Box(
            Modifier
                .width(frameWidth)
                .fillMaxHeight()
                .designCard(RoundedCornerShape(PREVIEW_CORNER))
                .clearAndSetSemantics { contentDescription = description },
        ) {
            Box(
                Modifier
                    .wrapContentSize(Alignment.TopStart, unbounded = true)
                    .requiredSize(PREVIEW_SCREEN_WIDTH, screenHeight)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0f)
                    },
            ) {
                preview()
            }
            Box(
                Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                        }
                    },
            )
        }
    }
}

private val STEP_CORNER = 28.dp
private const val STEP_MAX_SHARE = 0.62f
// progress, buttons and paddings around the scrolling part of a step
private val STEP_CHROME_HEIGHT = 130.dp
private val STEP_MIN_HEIGHT = 120.dp
private val BUTTON_HEIGHT = 52.dp
private val MYZEL_BUTTON_CORNER = 12.dp
private val STATUS_CORNER = 14.dp
private const val PREVIEW_WIDTH_SHARE = 0.78f
private val PREVIEW_MAX_WIDTH = 340.dp
private val PREVIEW_SCREEN_WIDTH = 400.dp
private val PREVIEW_CORNER = 24.dp
