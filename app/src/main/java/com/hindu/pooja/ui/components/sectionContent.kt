package com.hindu.pooja.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SectionContent(title: String, content: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = title, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = content, fontSize = 16.sp)
    }
}
