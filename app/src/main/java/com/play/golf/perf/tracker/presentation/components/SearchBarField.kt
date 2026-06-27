package com.play.golf.perf.tracker.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.play.golf.perf.tracker.R
import com.play.golf.perf.tracker.ui.theme.golfBodyLargeRegular
import com.play.golf.perf.tracker.ui.theme.grey_999999
import com.play.golf.perf.tracker.ui.theme.grey_CCCCCC
import com.play.golf.perf.tracker.util.letCompose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarField(
    modifier: Modifier,
    isEnable: Boolean,
    isRequestFocus: Boolean,
    isShowCancelIcon: Boolean,
    inputText: MutableState<String>,
    placeHolderText: String,
    leadingIconResId: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onDone: ((text: String) -> Unit)? = null,
    onBarClick: (() -> Unit)? = null,
    onTextChange: ((text: String) -> Unit)? = null,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager   = LocalFocusManager.current
    var firstFocus by remember { mutableStateOf(true) }

    val colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor      = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor    = Color.grey_CCCCCC,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedContainerColor   = MaterialTheme.colorScheme.surface,
        disabledContainerColor  = MaterialTheme.colorScheme.surface,
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val singleLine = true

    if (isRequestFocus && firstFocus && !isFocused) {
        LaunchedEffect("RequestSearchBarFocus") {
            firstFocus = false
            focusRequester.requestFocus()
        }
    }

    BasicTextField(
        value       = inputText.value,
        onValueChange = {
            inputText.value = it
            onTextChange?.invoke(it)
        },
        modifier = modifier
            .clickable { onBarClick?.invoke() }
            .focusRequester(focusRequester),
        interactionSource = interactionSource,
        enabled           = isEnable,
        singleLine        = singleLine,
        keyboardOptions   = keyboardOptions,
        keyboardActions   = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onDone?.invoke(inputText.value)
            }
        ),
        textStyle = TextStyle.golfBodyLargeRegular.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
    ) {
        OutlinedTextFieldDefaults.DecorationBox(
            value                = inputText.value,
            visualTransformation = VisualTransformation.None,
            innerTextField       = it,
            enabled              = isEnable,
            singleLine           = singleLine,
            interactionSource    = interactionSource,
            contentPadding       = OutlinedTextFieldDefaults.contentPadding(
                top    = 8.dp,
                bottom = 8.dp,
                start  = 16.dp,
                end    = 16.dp,
            ),
            colors    = colors,
            container = {
                OutlinedTextFieldDefaults.Container(
                    enabled                  = isEnable,
                    isError                  = false,
                    interactionSource        = interactionSource,
                    colors                   = colors,
                    shape                    = RoundedCornerShape(8.dp),
                    focusedBorderThickness   = 0.5.dp,
                    unfocusedBorderThickness = 1.dp,
                )
            },
            leadingIcon = leadingIconResId.letCompose { resId ->
                Icon(
                    painter            = painterResource(id = resId),
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier           = Modifier
                        .padding(start = 8.dp)
                        .size(32.dp),
                )
            },
            trailingIcon = if (isShowCancelIcon && inputText.value.isNotBlank()) {
                {
                    Icon(
                        imageVector        = ImageVector.vectorResource(id = R.drawable.ic_cancel),
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier           = Modifier
                            .padding(end = 8.dp)
                            .size(24.dp)
                            .clickable {
                                inputText.value = ""
                                onTextChange?.invoke("")
                                focusRequester.requestFocus()
                            },
                    )
                }
            } else null,
            placeholder = {
                Text(
                    text     = placeHolderText,
                    style    = TextStyle.golfBodyLargeRegular,
                    color    = Color.grey_999999,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )
    }
}