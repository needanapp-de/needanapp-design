package de.needanapp.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.needanapp.design.R
import de.needanapp.design.myzel.ConfirmDialog as MyzelConfirmDialog
import de.needanapp.design.myzel.ContentDialog as MyzelContentDialog
import de.needanapp.design.myzel.InfoDialog as MyzelInfoDialog
import de.needanapp.design.myzel.RefreshBox as MyzelRefreshBox
import de.needanapp.design.myzel.TextInputDialog as MyzelTextInputDialog
import de.needanapp.design.theme.isMyzelDesign

/**
 * A short question with a confirming and a cancelling key. [isDestructive] colors the dialog in the error color, e.g.
 * for deleting. [icon] tells what happens, e.g. a bin, and stands in the band at the top and on the confirming key.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String?,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false,
    dismissLabel: String = stringResource(R.string.nd_cancel),
    icon: ImageVector? = null,
) {
    if (isMyzelDesign) return MyzelConfirmDialog(
        title = title, message = message, confirmLabel = confirmLabel, onConfirm = onConfirm, onDismiss = onDismiss,
        isDestructive = isDestructive, dismissLabel = dismissLabel,
    )
    val confirm = {
        onDismiss()
        onConfirm()
    }
    DialogFrame(
        title = title,
        icon = icon ?: if (isDestructive) Icons.Outlined.WarningAmber else Icons.AutoMirrored.Outlined.HelpOutline,
        tone = if (isDestructive) DialogTone.DESTRUCTIVE else DialogTone.ACCENT,
        onDismiss = onDismiss,
        keys = listOf(cancelKey(dismissLabel, onDismiss), DialogKey(confirmLabel, icon ?: Icons.Filled.Check, confirm, isMain = true)),
        content = message?.let { { Text(it) } },
    )
}

/**
 * A dialog with its own [content], e.g. a small form or a list of details; the content scrolls when it gets long.
 * Unlike [ConfirmDialog], [onConfirm] does not close the dialog, so input can be checked first ([confirmEnabled]).
 * [extraLabel] adds a third key between cancelling and confirming, e.g. "Skip" besides "Overwrite" and "Cancel". A null
 * [dismissLabel] leaves out the cancelling key. [icon] stands in the band at the top, [confirmIcon] and [extraIcon] on
 * their keys.
 */
@Composable
fun ContentDialog(
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
    confirmIcon: ImageVector? = Icons.Filled.Check,
    extraIcon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (isMyzelDesign) return MyzelContentDialog(
        title = title, confirmLabel = confirmLabel, onConfirm = onConfirm, onDismiss = onDismiss,
        confirmEnabled = confirmEnabled, isDestructive = isDestructive, dismissLabel = dismissLabel,
        extraLabel = extraLabel, onExtra = onExtra, icon = icon, content = content,
    )
    DialogFrame(
        title = title,
        icon = icon,
        tone = if (isDestructive) DialogTone.DESTRUCTIVE else DialogTone.ACCENT,
        onDismiss = onDismiss,
        keys = listOfNotNull(
            dismissLabel?.let { cancelKey(it, onDismiss) },
            extraLabel?.let { DialogKey(it, extraIcon, onExtra) },
            DialogKey(confirmLabel, confirmIcon, onConfirm, isMain = true, enabled = confirmEnabled),
        ),
        content = content,
    )
}

/**
 * Tells something and only needs an OK, e.g. a license text, in a quiet band with [icon]. Long [message]s scroll inside
 * the dialog; [confirmLabel] defaults to "OK".
 */
@Composable
fun InfoDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    confirmLabel: String = stringResource(R.string.nd_ok),
    icon: ImageVector = Icons.Outlined.Info,
) {
    if (isMyzelDesign) return MyzelInfoDialog(
        title = title, message = message, onDismiss = onDismiss, confirmLabel = confirmLabel,
    )
    DialogFrame(
        title = title,
        icon = icon,
        tone = DialogTone.QUIET,
        onDismiss = onDismiss,
        keys = listOf(DialogKey(confirmLabel, Icons.Filled.Check, onDismiss, isMain = true)),
        content = { Text(message) },
    )
}

/**
 * A dialog without keys while something is under way, e.g. a vault that opens, with [icon] in its band; its owner
 * closes it when the work is done. Tapping beside it calls [onDismiss], which may well do nothing.
 */
@Composable
fun BusyDialog(
    title: String,
    onDismiss: () -> Unit = {},
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (isMyzelDesign) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {},
            icon = icon?.let { { Icon(it, contentDescription = null) } },
            title = { Text(title) },
            text = { Column(content = content) },
            shape = MaterialTheme.shapes.extraLarge,
        )
        return
    }
    DialogFrame(title = title, icon = icon, tone = DialogTone.ACCENT, onDismiss = onDismiss, keys = emptyList(), content = content)
}

/**
 * Asks for one line of text, e.g. a name. [validate] returns an error message for invalid input, which keeps the
 * confirming key disabled. With [selectUntil] only the start of [initialValue] is selected, e.g. a file name without
 * its extension. [icon] tells what the text is for, e.g. a new folder.
 */
@Composable
fun TextInputDialog(
    title: String,
    label: String,
    initialValue: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    validate: (String) -> String? = { null },
    selectUntil: Int = initialValue.length,
    icon: ImageVector = Icons.Outlined.Edit,
) {
    if (isMyzelDesign) return MyzelTextInputDialog(
        title = title, label = label, initialValue = initialValue, confirmLabel = confirmLabel, onConfirm = onConfirm,
        onDismiss = onDismiss, validate = validate, selectUntil = selectUntil,
    )
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

    DialogFrame(
        title = title,
        icon = icon,
        tone = DialogTone.ACCENT,
        onDismiss = onDismiss,
        keys = listOf(
            cancelKey(stringResource(R.string.nd_cancel), onDismiss),
            DialogKey(confirmLabel, Icons.Filled.Check, ::confirm, isMain = true, enabled = canConfirm),
        ),
        content = {
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
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                )
            }
        },
    )
}

private fun cancelKey(label: String, onDismiss: () -> Unit) = DialogKey(label, Icons.Rounded.Close, onDismiss)

/**
 * Pull down to refresh around scrolling [content]; wraps Material's still experimental pull-to-refresh. The indicator
 * appears [indicatorTop] below the top, i.e. below an [AppScaffold] header, in the accent colors. Switching [enabled]
 * keeps the content as it is, including a gesture that is under way.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    indicatorTop: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    if (isMyzelDesign) return MyzelRefreshBox(
        isRefreshing = isRefreshing, onRefresh = onRefresh, modifier = modifier, enabled = enabled,
        indicatorTop = indicatorTop, content = content,
    )
    val state = rememberPullToRefreshState()
    Box(modifier.pullToRefresh(isRefreshing = isRefreshing, state = state, enabled = enabled, onRefresh = onRefresh)) {
        content()
        if (enabled || isRefreshing) {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = indicatorTop),
                // accent colors: Material's default container has the color of the cards it covers
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
