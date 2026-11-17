package com.lumi.app.core.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lumi.app.ui.theme.LumiColors

@Composable
fun LumiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        label = if (label.isNotBlank()) {
            { Text(label) }
        } else null,
        placeholder = {
            if (placeholder.isNotBlank()) {
                Text(placeholder)
            }
        },
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LumiColors.White,
            unfocusedContainerColor = LumiColors.White,
            disabledContainerColor = LumiColors.White,
            focusedBorderColor = LumiColors.YellowStrong,
            unfocusedBorderColor = LumiColors.WarmGray,
            focusedLabelColor = LumiColors.Dark,
            cursorColor = LumiColors.YellowStrong,
            focusedTextColor = LumiColors.Dark,
            unfocusedTextColor = LumiColors.Dark
        )
    )
}
