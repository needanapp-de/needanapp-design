package de.needanapp.design.components

import android.os.SystemClock
import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.PlatformTextInputMethodRequest

/**
 * Text fields inside delete at a steady, slow pace: a held delete key removes one character after the other, about
 * ten a second, also where the keyboard would speed up or jump to whole words. For that the keyboard sends single key
 * presses instead of editing the text itself, as it does for a terminal; it then offers no suggestions or swipe
 * typing there, which rarely help with names anyway.
 */
@Composable
fun SteadyDeleteInput(content: @Composable () -> Unit) {
    val pace = remember { DeletePace() }
    InterceptPlatformTextInput(
        interceptor = { request, nextHandler ->
            nextHandler.startInputMethod(
                PlatformTextInputMethodRequest { outAttributes ->
                    val connection = request.createInputConnection(outAttributes)
                    // a keyboard that edits the text itself keeps its own copy of it, which a skipped step would
                    // put out of step; key presses it can't keep track of
                    outAttributes.inputType = InputType.TYPE_NULL
                    SteadyDeleteConnection(connection, pace)
                },
            )
        },
        content = content,
    )
}

/** When a character was deleted last; the next one has to wait [STEP_MILLIS]. */
private class DeletePace {
    private var last = 0L

    /** Whether a character may go now. */
    fun step(): Boolean {
        val now = SystemClock.uptimeMillis()
        if (now - last < STEP_MILLIS) return false
        last = now
        return true
    }
}

/** Lets delete key presses through at the [pace], and deletions sent as such one character at a time. */
private class SteadyDeleteConnection(target: InputConnection, private val pace: DeletePace) :
    InputConnectionWrapper(target, false) {

    override fun sendKeyEvent(event: KeyEvent): Boolean {
        val isDelete = event.keyCode == KeyEvent.KEYCODE_DEL || event.keyCode == KeyEvent.KEYCODE_FORWARD_DEL
        if (isDelete && event.action == KeyEvent.ACTION_DOWN && !pace.step()) return true
        return super.sendKeyEvent(event)
    }

    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean =
        deleteSurroundingTextInCodePoints(beforeLength, afterLength)

    override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean {
        if (beforeLength + afterLength <= 0) return super.deleteSurroundingTextInCodePoints(beforeLength, afterLength)
        if (!pace.step()) return true
        return super.deleteSurroundingTextInCodePoints(beforeLength.coerceAtMost(1), afterLength.coerceAtMost(1))
    }
}

/** The shortest time between two deleted characters; a keyboard repeating every 50 ms gets every other step. */
private const val STEP_MILLIS = 90L
