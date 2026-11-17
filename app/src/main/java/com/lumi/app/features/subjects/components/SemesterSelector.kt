package com.lumi.app.features.subjects.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.lumi.app.ui.theme.LumiColors

@Composable
fun SemesterSelector(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 4.dp)
    ) {
        items(filters) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = {
                    onFilterSelected(filter)
                },
                label = {
                    Text(filter)
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LumiColors.Yellow,
                    selectedLabelColor = LumiColors.Dark,
                    containerColor = LumiColors.White,
                    labelColor = LumiColors.Dark
                )
            )
        }
    }
}
