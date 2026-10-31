package de.needanapp.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.needanapp.design.R

/** A round color sample; [selected] draws a check mark, [onLongClick] e.g. removes a saved color. */
@Composable
fun ColorSwatch(
    color: Color,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    size: Dp = 48.dp,
    onLongClick: (() -> Unit)? = null,
) {
    val outline = MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .border(if (selected) 3.dp else 1.dp, if (selected) MaterialTheme.colorScheme.onSurface else outline, CircleShape)
            .combinedClickable(role = Role.RadioButton, onLongClick = onLongClick, onClick = onClick)
            .semantics {
                contentDescription = description
                this.selected = selected
            },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = if (color.luminance() > 0.5f) Color.Black else Color.White)
        }
    }
}

/** "#RRGGBB" of [color]. */
fun Color.toHex(): String = "#%06X".format(toArgb() and 0xFFFFFF)

/** Parses "#RRGGBB" or "RRGGBB", null if it isn't a valid color. */
fun parseHexColor(text: String): Color? {
    val hex = text.trim().removePrefix("#")
    if (hex.length != 6 || hex.any { it.lowercaseChar() !in "0123456789abcdef" }) return null
    return Color(0xFF000000 or hex.toLong(16))
}

/**
 * A color picker: saturation/brightness field, hue bar and hex input, with a preview. [onPick] gets the chosen color.
 */
@Composable
fun ColorPicker(initial: Color, onPick: (Color) -> Unit, modifier: Modifier = Modifier) {
    val hsv = remember { FloatArray(3).also { android.graphics.Color.colorToHSV(initial.toArgb(), it) } }
    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }
    val color = Color.hsv(hue, saturation, value)
    var hexText by remember { mutableStateOf(color.toHex()) }

    fun update(newHue: Float = hue, newSaturation: Float = saturation, newValue: Float = value) {
        hue = newHue
        saturation = newSaturation
        value = newValue
        hexText = Color.hsv(newHue, newSaturation, newValue).toHex()
    }

    Column(modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SaturationValueField(hue, saturation, value) { s, v -> update(newSaturation = s, newValue = v) }
        HueBar(hue) { update(newHue = it) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(color))
            Spacer(Modifier.width(16.dp))
            OutlinedTextField(
                value = hexText,
                onValueChange = { text ->
                    hexText = text
                    parseHexColor(text)?.let { parsed ->
                        val values = FloatArray(3)
                        android.graphics.Color.colorToHSV(parsed.toArgb(), values)
                        hue = values[0]
                        saturation = values[1]
                        value = values[2]
                    }
                },
                label = { Text(stringResource(R.string.nd_color_hex)) },
                isError = parseHexColor(hexText) == null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.weight(1f),
            )
        }
        SignalButton(onClick = { onPick(color) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.nd_color_apply))
        }
    }
}

@Composable
private fun SaturationValueField(hue: Float, saturation: Float, value: Float, onChange: (Float, Float) -> Unit) {
    // the gesture detectors live across recompositions, they must always call the latest callback
    val currentOnChange by rememberUpdatedState(onChange)
    val description = stringResource(R.string.nd_color_field)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
            .clip(RoundedCornerShape(16.dp))
            .semantics { contentDescription = description }
            .pointerInput(Unit) {
                fun pick(offset: Offset) = currentOnChange(
                    (offset.x / size.width).coerceIn(0f, 1f),
                    1f - (offset.y / size.height).coerceIn(0f, 1f),
                )
                detectTapGestures { pick(it) }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    currentOnChange(
                        (change.position.x / size.width).coerceIn(0f, 1f),
                        1f - (change.position.y / size.height).coerceIn(0f, 1f),
                    )
                }
            },
    ) {
        drawRect(Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue, 1f, 1f))))
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
        val center = Offset(saturation * size.width, (1f - value) * size.height)
        drawCircle(Color.White, radius = 12.dp.toPx(), center = center, style = Stroke(3.dp.toPx()))
        drawCircle(Color.Black, radius = 14.dp.toPx(), center = center, style = Stroke(1.dp.toPx()))
    }
}

@Composable
private fun HueBar(hue: Float, onChange: (Float) -> Unit) {
    val currentOnChange by rememberUpdatedState(onChange)
    val hues = remember { (0..6).map { Color.hsv(it * 60f % 360f, 1f, 1f) } }
    val description = stringResource(R.string.nd_color_hue)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .semantics { contentDescription = description }
            .pointerInput(Unit) { detectTapGestures { currentOnChange((it.x / size.width).coerceIn(0f, 1f) * MAX_HUE) } }
            .pointerInput(Unit) { detectDragGestures { change, _ -> currentOnChange((change.position.x / size.width).coerceIn(0f, 1f) * MAX_HUE) } },
    ) {
        drawRect(Brush.horizontalGradient(hues))
        val x = hue / MAX_HUE * size.width
        drawCircle(Color.White, radius = 12.dp.toPx(), center = Offset(x, size.height / 2), style = Stroke(3.dp.toPx()))
    }
}

private const val MAX_HUE = 359.9f
