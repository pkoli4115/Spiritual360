package com.hindu.pooja.ui.components

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hindu.pooja.model.PoojaIndexItem
import com.hindu.pooja.model.getDrawableIdFromThumbnail
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeSection(
    sectionTitle: String,
    items: List<PoojaIndexItem>,
    isSelected: Boolean = false,
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
                style = if (isSelected) MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.primary)
                else MaterialTheme.typography.headlineSmall
            )

            TextButton(onClick = onViewAllClick) {
                Text(text = "View All")
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View All"
                )
            }
        }

        if (sectionTitle == "Daily Poojas") {
            AutoScrollSectionRow(items = items, onItemClick = onItemClick)
        } else {
            LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                items(items) { item ->
                    val context = LocalContext.current
                    val imageResId = getDrawableIdFromThumbnail(item.image, context)
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .width(140.dp)
                            .clickable { onItemClick(item) }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = item.name,
                                modifier = Modifier
                                    .height(100.dp)
                                    .fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AutoScrollSectionRow(
    items: List<PoojaIndexItem>,
    onItemClick: (PoojaIndexItem) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 🔁 Repeat items to fake infinite scroll
    val repeatedItems = remember { List(100) { items[it % items.size] } }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2500)
            val nextIndex = listState.firstVisibleItemIndex + 1
            coroutineScope.launch {
                if (nextIndex < repeatedItems.size - 1) {
                    listState.animateScrollToItem(nextIndex)
                } else {
                    // Smooth scroll reset to start
                    listState.scrollToItem(0)
                }
            }
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(repeatedItems.size) { index ->
            val item = repeatedItems[index]
            val context = LocalContext.current
            val imageResId = getDrawableIdFromThumbnail(item.image, context)

            Card(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(180.dp)
                    .clickable { onItemClick(item) }
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = item.name,
                        modifier = Modifier
                            .height(100.dp)
                            .fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
