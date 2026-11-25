
package com.lumi.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lumi.app.R

/** Fuente de la app: Fredoka (OFL), incluida en res/font. */
val FredokaFamily = FontFamily(
    Font(R.font.fredoka_regular, FontWeight.Normal),
    Font(R.font.fredoka_medium, FontWeight.Medium),
    Font(R.font.fredoka_semibold, FontWeight.SemiBold),
    Font(R.font.fredoka_bold, FontWeight.Bold)
)

private val BaseTypography = Typography(

    displayLarge = TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold
    ),

    headlineLarge = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold
    ),

    headlineMedium = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold
    ),

    titleLarge = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    ),

    titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    ),

    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    ),

    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    ),

    bodySmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    ),

    labelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
)

/** Aplica Fredoka a todos los estilos de Material (también a los que no se redefinen arriba). */
val Typography = BaseTypography.let { t ->
    t.copy(
        displayLarge = t.displayLarge.copy(fontFamily = FredokaFamily),
        displayMedium = t.displayMedium.copy(fontFamily = FredokaFamily),
        displaySmall = t.displaySmall.copy(fontFamily = FredokaFamily),
        headlineLarge = t.headlineLarge.copy(fontFamily = FredokaFamily),
        headlineMedium = t.headlineMedium.copy(fontFamily = FredokaFamily),
        headlineSmall = t.headlineSmall.copy(fontFamily = FredokaFamily),
        titleLarge = t.titleLarge.copy(fontFamily = FredokaFamily),
        titleMedium = t.titleMedium.copy(fontFamily = FredokaFamily),
        titleSmall = t.titleSmall.copy(fontFamily = FredokaFamily),
        bodyLarge = t.bodyLarge.copy(fontFamily = FredokaFamily),
        bodyMedium = t.bodyMedium.copy(fontFamily = FredokaFamily),
        bodySmall = t.bodySmall.copy(fontFamily = FredokaFamily),
        labelLarge = t.labelLarge.copy(fontFamily = FredokaFamily),
        labelMedium = t.labelMedium.copy(fontFamily = FredokaFamily),
        labelSmall = t.labelSmall.copy(fontFamily = FredokaFamily)
    )
}
