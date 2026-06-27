package com.play.golf.perf.tracker.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.play.golf.perf.tracker.R

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    placeHolderText: String = "Search players",
    leadingIconResId: Int? = R.drawable.ic_search,
    isRequestFocus: Boolean = false,
    isEnable: Boolean = true,
    isShowCancelIcon: Boolean = false,
    inputText: MutableState<String> = remember { mutableStateOf("") },
    leadingIconInFront: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onDone: ((inputText: String) -> Unit)? = null,
    onBarClick: (() -> Unit)? = null,
    onTextChange: ((inputText: String) -> Unit)? = null,
) {
    Box(
        modifier         = modifier,
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIconInFront?.invoke()
            SearchBarField(
                modifier         = Modifier.weight(1f),
                isEnable         = isEnable,
                isRequestFocus   = isRequestFocus,
                isShowCancelIcon = isShowCancelIcon,
                inputText        = inputText,
                placeHolderText  = placeHolderText,
                leadingIconResId = leadingIconResId,
                keyboardOptions  = keyboardOptions,
                onDone           = onDone,
                onBarClick       = onBarClick,
                onTextChange     = onTextChange,
            )
        }
    }
}