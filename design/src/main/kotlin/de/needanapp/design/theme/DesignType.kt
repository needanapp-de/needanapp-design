package de.needanapp.design.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import de.needanapp.design.R

/**
 * The fonts of the Myzel style, all under the SIL Open Font License 1.1 (see fonts/ in the repo): Geist for text,
 * Geist Mono for labels, keys and values, Doto as the dot matrix of the displays. All three are variable fonts; each
 * weight is an instance of the same file.
 */
@OptIn(ExperimentalTextApi::class)
object DesignFonts {
    private fun geist(resource: Int, weight: Int) =
        Font(resource, FontWeight(weight), variationSettings = FontVariation.Settings(FontVariation.weight(weight)))

    val Geist = FontFamily(
        geist(R.font.geist, 400),
        geist(R.font.geist, 500),
        geist(R.font.geist, 600),
        geist(R.font.geist, 700),
    )

    val GeistMono = FontFamily(
        geist(R.font.geist_mono, 400),
        geist(R.font.geist_mono, 500),
        geist(R.font.geist_mono, 600),
    )

    /** Square dots (roundness 0) in the heaviest weight, like the dot matrix of a device display. */
    val Doto = FontFamily(
        Font(
            R.font.doto,
            FontWeight.Black,
            variationSettings = FontVariation.Settings(FontVariation.weight(900), FontVariation.Setting("ROND", 0f)),
        ),
    )
}

/**
 * The text styles of the look beyond Material's [Typography]: small labels, the lettering of keys, values in the
 * monospace font and the dot matrix of a display. In the Myzel style labels and keys are small capitals in Geist Mono
 * (callers capitalize them with [labelCase]), in the needanapp look they are set in the system font as they are. The
 * dot matrix is the same in both.
 */
@Immutable
object DesignType {
    /** Small label above or beside a value, e.g. "INSTALLED", and the titles of cards and groups. */
    val label: TextStyle
        @Composable @ReadOnlyComposable get() = if (isNeedanappDesign) NeedanappLabel else DeviceLabel

    /** Even smaller, e.g. under the icons of a row of keys. */
    val labelSmall: TextStyle
        @Composable @ReadOnlyComposable get() = if (isNeedanappDesign) NeedanappLabelSmall else DeviceLabelSmall

    /** The lettering of a key, e.g. "UPDATE". */
    val key: TextStyle
        @Composable @ReadOnlyComposable get() = if (isNeedanappDesign) NeedanappKey else DeviceKeyStyle

    /** Values that are read rather than read out: versions, package names, sizes, fingerprints. */
    val value: TextStyle
        @Composable @ReadOnlyComposable get() = if (isNeedanappDesign) NeedanappValue else DeviceValue

    /** The dot matrix of a display, e.g. "UPDATE 2.5.0". */
    val display = TextStyle(fontFamily = DesignFonts.Doto, fontWeight = FontWeight.Black, fontSize = 22.sp, lineHeight = 26.sp, letterSpacing = 0.04.em)

    private val DeviceLabel = TextStyle(fontFamily = DesignFonts.GeistMono, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.14.em)
    private val DeviceLabelSmall = TextStyle(fontFamily = DesignFonts.GeistMono, fontWeight = FontWeight.Medium, fontSize = 9.5.sp, lineHeight = 12.sp, letterSpacing = 0.12.em)
    private val DeviceKeyStyle = TextStyle(fontFamily = DesignFonts.GeistMono, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.1.em)
    private val DeviceValue = TextStyle(fontFamily = DesignFonts.GeistMono, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp)

    private val NeedanappLabel = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
    private val NeedanappLabelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp)
    private val NeedanappKey = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp)
    private val NeedanappValue = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp)
}

/** Material's type scale set in Geist, headings a little tighter and heavier: the type of the Myzel style. */
internal fun designTypography(family: FontFamily = DesignFonts.Geist): Typography {
    val base = Typography()
    fun TextStyle.set(weight: FontWeight? = null, spacing: Float? = null) =
        copy(fontFamily = family, fontWeight = weight ?: fontWeight, letterSpacing = spacing?.em ?: letterSpacing)
    return Typography(
        displayLarge = base.displayLarge.set(FontWeight.SemiBold, -0.02f),
        displayMedium = base.displayMedium.set(FontWeight.SemiBold, -0.02f),
        displaySmall = base.displaySmall.set(FontWeight.SemiBold, -0.02f),
        headlineLarge = base.headlineLarge.set(FontWeight.SemiBold, -0.02f),
        headlineMedium = base.headlineMedium.set(FontWeight.SemiBold, -0.02f),
        headlineSmall = base.headlineSmall.set(FontWeight.SemiBold, -0.015f),
        titleLarge = base.titleLarge.set(FontWeight.SemiBold, -0.01f),
        titleMedium = base.titleMedium.set(FontWeight.SemiBold),
        titleSmall = base.titleSmall.set(FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.set(),
        bodyMedium = base.bodyMedium.set(),
        bodySmall = base.bodySmall.set(),
        labelLarge = base.labelLarge.set(FontWeight.Medium),
        labelMedium = base.labelMedium.set(FontWeight.Medium),
        labelSmall = base.labelSmall.set(FontWeight.Medium),
    )
}
