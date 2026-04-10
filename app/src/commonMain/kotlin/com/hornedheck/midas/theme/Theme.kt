package com.hornedheck.midas.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColourScheme by lazy {
    darkColorScheme(

    )
}

private val Typography = Typography()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MidasAppTheme(
    content: @Composable () -> Unit
) {
    MaterialExpressiveTheme(
        colorScheme = DarkColourScheme,
        typography = Typography,
        content = content
    )
}

object MidasColor {
    val Income = Color(0xFF66BB6A)
    val Expense = Color(0xFFE57373)
}
