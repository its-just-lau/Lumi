
package com.lumi.app.core.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lumi.app.ui.theme.LumiColors

@Composable
fun LumiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(label) },
        placeholder = {
            if (placeholder.isNotEmpty()) {
                Text(placeholder)
            }
        },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LumiColors.YellowStrong,
            unfocusedBorderColor = LumiColors.WarmGray,
            focusedLabelColor = LumiColors.Dark,
            cursorColor = LumiColors.YellowStrong,
            focusedTextColor = LumiColors.Dark,
            unfocusedTextColor = LumiColors.Dark
        )
    )
}
