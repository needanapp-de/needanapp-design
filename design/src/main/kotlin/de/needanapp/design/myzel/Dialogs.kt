package de.needanapp.design.myzel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.needanapp.design.R
import de.needanapp.design.components.*
import de.needanapp.design.components.verticalScrollWithBar
import de.needanapp.design.theme.LocalDesignColors
import de.needanapp.design.theme.fadedOn

/** A key of a dialog: as in [DeviceKey], a little lower. */
private val DialogKeyHeight = 52.dp

/**
 * The frame of the dialogs: a panel with large corners, [icon], [title] and [content], the keys at the bottom. Keys of
 * two short labels stand side by side, otherwise one below the other.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeviceDialog(
    title: String,
    onDismiss: () -> Unit,
    keys: List<DialogKey>,
    icon: ImageVector? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val design = LocalDesignColors.current
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = design.card,
            border = BorderStroke(1.dp, design.cardBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 18.dp)) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.height(14.dp))
                }
                Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                if (content != null) {
                    Spacer(Modifier.height(10.dp))
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurface.fadedOn(0.86f, design.card),
                        LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                    ) {
                        Column(Modifier.weight(1f, fill = false).verticalScrollWithBar(), content = content)
                    }
                }
                Spacer(Modifier.height(20.dp))
                DialogKeys(keys)
            }
        }
    }
}

/** A key of a [DeviceDialog]. */
internal data class DialogKey(val label: String, val style: KeyStyle, val enabled: Boolean = true, val onClick: () -> Unit)

@Composable
private fun DialogKeys(keys: List<DialogKey>) {
    val sideBySide = keys.size <= 2 && keys.sumOf { it.label.length } <= SIDE_BY_SIDE_CHARS
    if (sideBySide) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            keys.forEach { key ->
                // the main action gets the wider key
                val weight = if (key.style == KeyStyle.SIGNAL || key.style == KeyStyle.DANGER) 3f else 2f
                DeviceKey(key.label, key.onClick, Modifier.weight(weight), style = key.style, enabled = key.enabled, height = DialogKeyHeight)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            // the main action on top, cancelling at the bottom
            keys.sortedBy { if (it.style == KeyStyle.GRAPHITE) 1 else 0 }.forEach { key ->
                DeviceKey(key.label, key.onClick, Modifier.fillMaxWidth(), style = key.style, enabled = key.enabled, height = DialogKeyHeight)
            }
        }
    }
}

/**
 * A short question with a confirming and a cancelling key. [isDestructive] paints the confirming key red, e.g. for
 * deleting.
 */
@Composable
internal fun ConfirmDialog(
    title: String,
    message: String?,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false,
    dismissLabel: String = stringResource(R.string.nd_cancel),
) {
    DeviceDialog(
        title = title,
        onDismiss = onDismiss,
        keys = listOf(
            DialogKey(dismissLabel, KeyStyle.GRAPHITE, onClick = onDismiss),
            DialogKey(confirmLabel, if (isDestructive) KeyStyle.DANGER else KeyStyle.SIGNAL) {
                onDismiss()
                onConfirm()
            },
        ),
        content = message?.let { { Text(it) } },
    )
}

/**
 * A dialog with its own [content], e.g. a small form or a list of details; the content scrolls when it gets long.
 * Unlike [ConfirmDialog], [onConfirm] does not close the dialog, so input can be checked first ([confirmEnabled]).
 * [extraLabel] adds a third key, e.g. "Skip" besides "Overwrite" and "Cancel"; then the keys are stacked. A null
 * [dismissLabel] leaves out the cancelling key.
 */
@Composable
internal fun ContentDialog(
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean = true,
    isDestructive: Boolean = false,
    dismissLabel: String? = stringResource(R.string.nd_cancel),
    extraLabel: String? = null,
    onExtra: () -> Unit = {},
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val keys = buildList {
        if (dismissLabel != null) add(DialogKey(dismissLabel, KeyStyle.GRAPHITE, onClick = onDismiss))
        if (extraLabel != null) add(DialogKey(extraLabel, KeyStyle.ALU, onClick = onExtra))
        add(DialogKey(confirmLabel, if (isDestructive) KeyStyle.DANGER else KeyStyle.SIGNAL, enabled = confirmEnabled, onClick = onConfirm))
    }
    DeviceDialog(title = title, onDismiss = onDismiss, keys = keys, icon = icon, content = content)
}

/**
 * Tells something and only needs an OK, e.g. a license text. Long [message]s scroll inside the dialog; [confirmLabel]
 * defaults to "OK".
 */
@Composable
internal fun InfoDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    confirmLabel: String = stringResource(R.string.nd_ok),
) {
    DeviceDialog(
        title = title,
        onDismiss = onDismiss,
        keys = listOf(DialogKey(confirmLabel, KeyStyle.SIGNAL, onClick = onDismiss)),
        content = { Text(message) },
    )
}

/**
 * Asks for one line of text, e.g. a name. [validate] returns an error message for invalid input, which keeps the
 * confirming key disabled. With [selectUntil] only the start of [initialValue] is selected, e.g. a file name
 * without its extension.
 */
@Composable
internal fun TextInputDialog(
    title: String,
    label: String,
    initialValue: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    validate: (String) -> String? = { null },
    selectUntil: Int = initialValue.length,
) {
    val state = rememberTextFieldState(initialValue, TextRange(0, selectUntil.coerceIn(0, initialValue.length)))
    val text = state.text.toString()
    val error = validate(text)
    val canConfirm = text.isNotBlank() && error == null
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    fun confirm() {
        if (canConfirm) {
            onDismiss()
            onConfirm(text.trim())
        }
    }

    DeviceDialog(
        title = title,
        onDismiss = onDismiss,
        keys = listOf(
            DialogKey(stringResource(R.string.nd_cancel), KeyStyle.GRAPHITE, onClick = onDismiss),
            DialogKey(confirmLabel, KeyStyle.SIGNAL, enabled = canConfirm, onClick = ::confirm),
        ),
    ) {
        // a held delete key takes one character after the other, slowly
        SteadyDeleteInput {
            OutlinedTextField(
                state = state,
                label = { Text(label) },
                lineLimits = TextFieldLineLimits.SingleLine,
                isError = error != null && text.isNotEmpty(),
                supportingText = error?.takeIf { text.isNotEmpty() }?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                onKeyboardAction = { confirm() },
                colors = deviceTextFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            )
        }
    }
}

/**
 * Pull down to refresh around scrolling [content]; wraps Material's still experimental pull-to-refresh. The indicator
 * appears [indicatorTop] below the top, i.e. below an [AppScaffold] header.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    indicatorTop: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    if (!enabled) {
        Box(modifier) { content() }
        return
    }
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = isRefreshing,
                containerColor = LocalDesignColors.current.bar,
                color = LocalDesignColors.current.signal,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = indicatorTop),
            )
        },
    ) { content() }
}

private const val SIDE_BY_SIDE_CHARS = 26
