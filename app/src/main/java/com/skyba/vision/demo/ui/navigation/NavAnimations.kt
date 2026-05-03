package com.artems_apps.vision_pause.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

@OptIn(ExperimentalAnimationApi::class)
fun materialSharedAxisXIn(forward: Boolean) = slideInHorizontally(
    initialOffsetX = { fullWidth -> if (forward) fullWidth else -fullWidth },
    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
) + fadeIn(animationSpec = tween(300))

@OptIn(ExperimentalAnimationApi::class)
fun materialSharedAxisXOut(forward: Boolean) = slideOutHorizontally(
    targetOffsetX = { fullWidth -> if (forward) -fullWidth else fullWidth },
    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
) + fadeOut(animationSpec = tween(300))
