package com.play.golf.perf.tracker.util

import androidx.compose.runtime.Composable

/**
 * Maps a nullable value to a nullable composable lambda.
 * Returns null (rendering nothing) when the receiver is null,
 * or a composable block wrapping the non-null value otherwise.
 *
 * Usage:
 *   leadingIcon = leadingIconResId.letCompose { resId ->
 *       Icon(painter = painterResource(resId), ...)
 *   }
 */
@Composable
fun <T> T?.letCompose(
    block: @Composable (T) -> Unit,
): (@Composable () -> Unit)? {
    return if (this != null) {
        @Composable { block(this) }
    } else null
}