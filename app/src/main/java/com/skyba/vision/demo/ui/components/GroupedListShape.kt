package com.skyba.vision.demo.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun getGroupedShape(
    index: Int,
    totalCount: Int,
    cornerRadius: Dp = 15.dp
): Shape {
    if (totalCount == 1) return RoundedCornerShape(cornerRadius)

    return when (index) {
        0 -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius,
            bottomStart = 5.dp, bottomEnd = 5.dp)
        totalCount - 1 -> RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp,
            bottomStart = cornerRadius, bottomEnd = cornerRadius)
        else -> RoundedCornerShape(5.dp)
    }
}