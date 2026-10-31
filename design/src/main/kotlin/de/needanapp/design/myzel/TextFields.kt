package de.needanapp.design.myzel

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import de.needanapp.design.theme.LocalDesignColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.needanapp.design.R
import de.needanapp.design.components.*

/**
 * A text field as a row of a [SettingsGroup], for forms in the grouped style. [error] replaces [supportingText] and
 * marks the field; [isSecret] hides the text behind dots with a button to show it.
 */
@Composable
internal fun TextFieldRow(
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
    var revealed by rememberSaveable { mutableStateOf(false) }
    val hidden = isSecret && !revealed
    Surface(shape = position.shape(), color = LocalDesignColors.current.card, modifier = modifier.fillMaxWidth().rowOutline(position)) {
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
            colors = deviceTextFieldColors(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

/**
 * A password field on its own, e.g. inside a dialog: the text is hidden behind dots with a button to show it. [onDone]
 * runs when the keyboard's done key is pressed.
 */
@Composable
internal fun SecretTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    supportingText: String? = null,
    onDone: (() -> Unit)? = null,
) {
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
        colors = deviceTextFieldColors(),
        shape = RoundedCornerShape(14.dp),
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

/** Text fields of the device look: a recessed field with a fine border that darkens while typing. */
@Composable
internal fun deviceTextFieldColors(): TextFieldColors {
    val scheme = MaterialTheme.colorScheme
    val design = LocalDesignColors.current
    val field = if (design.isDark) design.graphiteKey else scheme.surfaceContainerLowest
    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = field,
        unfocusedContainerColor = field,
        disabledContainerColor = field,
        focusedBorderColor = scheme.onSurface,
        unfocusedBorderColor = design.aluKeyBorder,
        focusedLabelColor = scheme.onSurface,
        cursorColor = scheme.onSurface,
    )
}

private fun KeyboardOptions.secret() = copy(keyboardType = KeyboardType.Password, autoCorrectEnabled = false)
