package com.hindu.pooja.ui.components

import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hindu.pooja.R
import com.hindu.pooja.model.PoojaIndexItem
import com.hindu.pooja.util.rememberSafePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeSection(
    sectionTitle: String,
    items: List<PoojaIndexItem>,
    isSelected: Boolean = false,
    autoScroll: Boolean = false,
    onItemClick: (PoojaIndexItem) -> Unit,
    onViewAllClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sectionTitle,
                style = if (isSelected)
                    MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.primary)
                else
                    MaterialTheme.typography.headlineSmall
            )

            TextButton(onClick = onViewAllClick) {
                Text(text = "View All")
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View All"
                )
            }
        }

        when {
            items.isEmpty() -> {
                EmptySectionPlaceholder()
            }
            autoScroll -> {
                AutoScrollSectionRow(items = items, onItemClick = onItemClick)
            }
            else -> {
                LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                    items(items) { item ->
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .width(140.dp)
                                .height(180.dp)
                                .clickable { onItemClick(item) }
                        ) {
                            Card(Modifier.fillMaxSize()) {
                                Column(Modifier.padding(8.dp)) {
                                    Image(
                                        painter = rememberSafePainter(item.image), // <- no fallbackRes
                                        contentDescription = item.name,
                                        modifier = Modifier
                                            .height(100.dp)
                                            .fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            if (item.isPremium) {
                                Icon(
                                    painter = rememberSafePainterRes(R.drawable.ic_crown),
                                    contentDescription = "Premium",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-8).dp, y = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Auto-scrolling row that is SAFE when items is empty. */
@Composable
fun AutoScrollSectionRow(
    items: List<PoojaIndexItem>,
    onItemClick: (PoojaIndexItem) -> Unit
) {
    if (items.isEmpty()) {
        EmptySectionPlaceholder()
        return
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val repeatedItems = remember(items) { List(100) { items[it % items.size] } }

    LaunchedEffect(repeatedItems) {
        while (true) {
            delay(2500)
            val nextIndex = listState.firstVisibleItemIndex + 1
            coroutineScope.launch {
                if (nextIndex < repeatedItems.size - 1) {
                    listState.animateScrollToItem(nextIndex)
                } else {
                    listState.scrollToItem(0)
                }
            }
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(repeatedItems) { item ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(180.dp)
                    .height(180.dp)
                    .clickable { onItemClick(item) }
            ) {
                Card(Modifier.fillMaxSize()) {
                    Column(Modifier.padding(8.dp)) {
                        Image(
                            painter = rememberSafePainter(item.image), // <- no fallbackRes
                            contentDescription = item.name,
                            modifier = Modifier
                                .height(100.dp)
                                .fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (item.isPremium) {
                    Icon(
                        painter = rememberSafePainterRes(R.drawable.ic_crown),
                        contentDescription = "Premium",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                    )
                }
            }
        }
    }
}

/** Small helper to show a subtle 'empty' placeholder */
@Composable
private fun EmptySectionPlaceholder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssistChip(
            onClick = {},
            label = { Text("No items yet") },
            leadingIcon = {
                Icon(
                    painter = rememberSafePainterRes(R.drawable.shiva),
                    contentDescription = null
                )
            }
        )
    }
}

/** Wrap a static resource id as a painter via painterResource. */
@Composable
private fun rememberSafePainterRes(resId: Int) = painterResource(resId)
