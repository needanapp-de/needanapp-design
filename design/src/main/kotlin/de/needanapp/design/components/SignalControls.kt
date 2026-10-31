package de.needanapp.design.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import de.needanapp.design.theme.LocalDesignColors
import de.needanapp.design.theme.isNeedanappDesign

/*
 * Material's controls that fill with the accent, in the signal color: the filled button, progress, switches, sliders
 * and checkboxes. Material fills them with the scheme's primary, which NeedanappTheme makes the accent's tone for text
 * and icons, a deep one for a bright accent; the needanapp look fills with the accent as it is (DesignColors.signal),
 * like DeviceKey and the pill. Apps take these in place of Button, CircularProgressIndicator and
 * LinearProgressIndicator, and pass the colors to Switch, Slider and Checkbox. Radio buttons only draw a ring and a
 * dot and stay in primary.
 */

/** The colors of a filled button in the signal color: [de.needanapp.design.theme.DesignColors.signal] with onSignal. */
@Composable
fun signalButtonColors(): ButtonColors {
    val design = LocalDesignColors.current
    return ButtonDefaults.buttonColors(containerColor = design.signal, contentColor = design.onSignal)
}

/** Material's filled [Button] in the signal color ([signalButtonColors]). */
@Composable
fun SignalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = signalButtonColors(),
        contentPadding = contentPadding,
        content = content,
    )
}

/** Material's round progress indicator in the signal color; without [progress] it turns endlessly. */
@Composable
fun SignalCircularProgressIndicator(
    modifier: Modifier = Modifier,
    progress: (() -> Float)? = null,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
) {
    val signal = LocalDesignColors.current.signal
    if (progress == null) {
        CircularProgressIndicator(modifier = modifier, color = signal, strokeWidth = strokeWidth)
    } else {
        CircularProgressIndicator(progress = progress, modifier = modifier, color = signal, strokeWidth = strokeWidth)
    }
}

/** Material's progress bar in the signal color; without [progress] it runs endlessly. */
@Composable
fun SignalLinearProgressIndicator(
    modifier: Modifier = Modifier,
    progress: (() -> Float)? = null,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
) {
    val signal = LocalDesignColors.current.signal
    if (progress == null) {
        LinearProgressIndicator(modifier = modifier, color = signal, trackColor = trackColor)
    } else {
        LinearProgressIndicator(progress = progress, modifier = modifier, color = signal, trackColor = trackColor)
    }
}

/**
 * The colors of a switch: on, the track in the signal color. In the Myzel style a light thumb in both states, as on a
 * device; in the NeedAnApp style Material's switch with the thumb in onSignal.
 */
@Composable
fun signalSwitchColors(): SwitchColors {
    val design = LocalDesignColors.current
    if (isNeedanappDesign) {
        return SwitchDefaults.colors(
            checkedThumbColor = design.onSignal,
            checkedTrackColor = design.signal,
            checkedIconColor = design.signal,
        )
    }
    val thumb = Color(0xFFF3F3F0)
    val off = if (design.isDark) Color(0xFF3A3A36) else MaterialTheme.colorScheme.outlineVariant
    return SwitchDefaults.colors(
        checkedThumbColor = thumb,
        checkedTrackColor = design.signal,
        checkedBorderColor = design.signal,
        checkedIconColor = design.signal,
        uncheckedThumbColor = thumb,
        uncheckedTrackColor = off,
        uncheckedBorderColor = off,
    )
}

/** The colors of a slider: thumb and the filled part of the track in the signal color. */
@Composable
fun signalSliderColors(): SliderColors {
    val design = LocalDesignColors.current
    return SliderDefaults.colors(
        thumbColor = design.signal,
        activeTrackColor = design.signal,
        activeTickColor = design.onSignal,
    )
}

/** The colors of a checkbox: checked, the box in the signal color with the check mark in onSignal. */
@Composable
fun signalCheckboxColors(): CheckboxColors {
    val design = LocalDesignColors.current
    return CheckboxDefaults.colors(checkedColor = design.signal, checkmarkColor = design.onSignal)
}
