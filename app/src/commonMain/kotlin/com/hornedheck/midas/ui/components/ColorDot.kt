package com.hornedheck.midas.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
internal fun ColorDot(
    color: Int?,
    modifier: Modifier = Modifier,
) {
    val dotColor = color?.let { Color(it) } ?: MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = modifier, contentDescription = "") {
        drawCircle(dotColor)
    }
}
