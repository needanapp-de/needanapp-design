package de.needanapp.design.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.needanapp.design.R
import de.needanapp.design.myzel.TextFieldRow as MyzelTextFieldRow
import de.needanapp.design.theme.isMyzelDesign
import de.needanapp.design.myzel.SecretTextField as MyzelSecretTextField

/**
 * A text field as a row of a [SettingsGroup], for forms in the grouped style. [error] replaces [supportingText] and
 * marks the field; [isSecret] hides the text behind dots with a button to show it.
 */
@Composable
fun TextFieldRow(
    position: RowPosition,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    error: String? = null,
    isSecret: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    if (isMyzelDesign) return MyzelTextFieldRow(
        position = position, value = value, onValueChange = onValueChange, label = label, modifier = modifier,
        placeholder = placeholder, supportingText = supportingText, error = error, isSecret = isSecret,
        singleLine = singleLine, minLines = minLines, maxLines = maxLines, keyboardOptions = keyboardOptions,
        leadingIcon = leadingIcon, enabled = enabled,
    )
    TextFieldSurface(
        position.shape(), MaterialTheme.colorScheme.surfaceContainer, value, onValueChange, label, modifier, placeholder, supportingText, error,
        isSecret, singleLine, minLines, maxLines, keyboardOptions, leadingIcon, enabled,
    )
}

/**
 * A text field as a tile of a [TileCard], e.g. the password below the switch that asks for one; otherwise like
 * [TextFieldRow].
 */
@Composable
fun TextFieldTile(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    shape: Shape,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    error: String? = null,
    isSecret: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true,
) {
    TextFieldSurface(
        shape, TileDefaults.containerColor(), value, onValueChange, label, modifier, placeholder, supportingText, error,
        isSecret, singleLine, 1, if (singleLine) 1 else Int.MAX_VALUE, keyboardOptions, null, enabled,
    )
}

@Composable
private fun TextFieldSurface(
    shape: Shape,
    color: Color,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
    placeholder: String?,
    supportingText: String?,
    error: String?,
    isSecret: Boolean,
    singleLine: Boolean,
    minLines: Int,
    maxLines: Int,
    keyboardOptions: KeyboardOptions,
    leadingIcon: ImageVector?,
    enabled: Boolean,
) {
    var revealed by rememberSaveable { mutableStateOf(false) }
    val hidden = isSecret && !revealed
    Surface(shape = shape, color = color, modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            supportingText = (error ?: supportingText)?.let { { Text(it) } },
            isError = error != null,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            enabled = enabled,
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
            trailingIcon = if (isSecret) {
                { RevealButton(hidden) { revealed = !revealed } }
            } else {
                null
            },
            visualTransformation = if (hidden) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = if (isSecret) keyboardOptions.secret() else keyboardOptions,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

/**
 * A password field on its own, e.g. inside a dialog: the text is hidden behind dots with a button to show it. [onDone]
 * runs when the keyboard's done key is pressed.
 */
@Composable
fun SecretTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    supportingText: String? = null,
    onDone: (() -> Unit)? = null,
) {
    if (isMyzelDesign) return MyzelSecretTextField(
        value = value, onValueChange = onValueChange, label = label, modifier = modifier, error = error,
        supportingText = supportingText, onDone = onDone,
    )
    var revealed by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = (error ?: supportingText)?.let { { Text(it) } },
        isError = error != null,
        singleLine = true,
        trailingIcon = { RevealButton(!revealed) { revealed = !revealed } },
        visualTransformation = if (revealed) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(imeAction = if (onDone != null) ImeAction.Done else ImeAction.Default).secret(),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun RevealButton(hidden: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            if (hidden) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
            contentDescription = stringResource(if (hidden) R.string.nd_show_secret else R.string.nd_hide_secret),
        )
    }
}

private fun KeyboardOptions.secret() = copy(keyboardType = KeyboardType.Password, autoCorrectEnabled = false)
