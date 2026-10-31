package de.needanapp.design.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

/** Material 3 motion tokens used by the building blocks. */
object NeedanappMotion {
    /** "Emphasized decelerate": things that grow or come in. */
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** "Emphasized accelerate": things that shrink or leave. */
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    val Standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    const val MEDIUM_DURATION = 300
    const val SHORT_DURATION = 150
}

/** An emphasized tween, or no animation at all when effects are reduced or the system turned animations off. */
@Composable
@ReadOnlyComposable
fun <T> emphasizedSpec(durationMillis: Int = NeedanappMotion.MEDIUM_DURATION): FiniteAnimationSpec<T> =
    if (LocalDesignEffects.current.richMotion) tween(durationMillis, easing = NeedanappMotion.EmphasizedDecelerate) else snap()
