package com.hindu.pooja.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hindu.pooja.model.PoojaIndexItem
import com.hindu.pooja.model.SectionCard
import kotlinx.coroutines.delay

@Composable
fun HomeSection(
    sectionTitle: String,
    items: List<PoojaIndexItem>,
    isSelected: Boolean = false,
    onItemClick: (PoojaIndexItem) -> Unit
) {
    val listState = rememberLazyListState()

    // ✅ Auto-scroll logic inside each section
    LaunchedEffect(key1 = items) {
        var index = 0
        while (true) {
            if (items.size > 1) {
                listState.animateScrollToItem(index)
                delay(2000L)
                index = (index + 1) % items.size
            } else {
                break
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = sectionTitle,
            style = if (isSelected)
                MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.primary)
            else
                MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(items) { item ->
                SectionCard(
                    title = item.name,
                    thumbnail = item.image,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}
