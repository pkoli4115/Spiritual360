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
import com.hindu.pooja.R
import com.hindu.pooja.model.PoojaIndexItem
import com.hindu.pooja.model.getDrawableIdFromThumbnail
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeSection(
    sectionTitle: String,
    items: List<PoojaIndexItem>,
    isSelected: Boolean = false,
    autoScroll: Boolean = false, // 👈 Add this!
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

        // 🔁 Generic autoScroll logic
        if (autoScroll) {
            AutoScrollSectionRow(items = items, onItemClick = onItemClick)
        } else {
            LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                items(items) { item ->
                    val context = LocalContext.current
                    val imageResId = getDrawableIdFromThumbnail(item.image, context)
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .width(140.dp)
                            .height(180.dp)
                            .clickable { onItemClick(item) }
                    ) {
                        Card(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Image(
                                    painter = painterResource(id = imageResId),
                                    contentDescription = item.name,
                                    modifier = Modifier
                                        .height(100.dp)
                                        .fillMaxWidth(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit // never cropped!
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        // 👑 Crown Badge for premium items
                        if (item.isPremium) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_crown),
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

@Composable
fun AutoScrollSectionRow(
    items: List<PoojaIndexItem>,
    onItemClick: (PoojaIndexItem) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val repeatedItems = remember { List(100) { items[it % items.size] } }

    LaunchedEffect(Unit) {
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
        items(repeatedItems.size) { index ->
            val item = repeatedItems[index]
            val context = LocalContext.current
            val imageResId = getDrawableIdFromThumbnail(item.image, context)
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(180.dp)
                    .height(180.dp)
                    .clickable { onItemClick(item) }
            ) {
                Card(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = item.name,
                            modifier = Modifier
                                .height(100.dp)
                                .fillMaxWidth(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (item.isPremium) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_crown),
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
