package com.hindu.pooja.feature.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalaKandaQuizScreen(
    repo: QuizModule,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val total = repo.questions.size
    var index by remember { mutableIntStateOf(0) }
    val answers = remember { MutableList<Set<Int>?>(total) { null } }  // null = unanswered
    var showExitConfirm by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }

    BackHandler(enabled = !showResult) { showExitConfirm = true }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text("Quit quiz?") },
            text = { Text("Your progress will be lost.") },
            confirmButton = { TextButton(onClick = { showExitConfirm = false; onBack() }) { Text("Exit") } },
            dismissButton = { TextButton(onClick = { showExitConfirm = false }) { Text("Stay") } }
        )
    }

    // Final result view
    if (showResult) {
        val score = answers.indices.count { answers[it] == repo.questions[it].correct }
        val percent = (score * 100f / total).roundToInt()
        val passed = percent >= repo.passPercent

        Surface {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(repo.title, style = MaterialTheme.typography.headlineSmall)
                LinearProgressIndicator(progress = percent / 100f, modifier = Modifier.fillMaxWidth())
                Text("Score: $score / $total  ($percent%)", fontWeight = FontWeight.SemiBold)
                Text(if (passed) "✅ Passed" else "❌ Failed (need ≥ ${repo.passPercent}%)",
                    color = if (passed) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold)

                // Brief review list
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    repo.questions.forEachIndexed { i, q ->
                        val user = answers[i] ?: emptySet()
                        val correct = q.correct
                        val ok = user == correct
                        ElevatedCard(
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (ok) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Q${i + 1}. ${q.text}", fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                q.options.forEachIndexed { oi, opt ->
                                    val mark = when {
                                        oi in correct && oi in user -> "✔︎"
                                        oi in correct && oi !in user -> "✓"
                                        oi !in correct && oi in user -> "✗"
                                        else -> "•"
                                    }
                                    Text("$mark  $opt")
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }

                Button(onClick = onFinish, modifier = Modifier.align(Alignment.End)) {
                    Text("Done")
                }
            }
        }
        return
    }

    // Question view
    val q = repo.questions[index]
    val selected = answers[index] ?: emptySet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(repo.title) },
                navigationIcon = { /* back disabled during quiz */ },
                actions = {
                    Text("${index + 1} / $total", modifier = Modifier.padding(end = 16.dp))
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LinearProgressIndicator(
                progress = (answers.count { it != null } / total.toFloat()),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Q${index + 1}. ${q.text}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            // Multi-select with Checkboxes (no experimental chips)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                q.options.forEachIndexed { i, opt ->
                    val checked = i in selected
                    OutlinedCard(onClick = {
                        val newSet = selected.toMutableSet().apply {
                            if (checked) remove(i) else add(i)
                        }.toSet()
                        answers[index] = newSet
                    }) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = checked, onCheckedChange = {
                                val newSet = selected.toMutableSet().apply {
                                    if (it) add(i) else remove(i)
                                }.toSet()
                                answers[index] = newSet
                            })
                            Spacer(Modifier.width(8.dp))
                            Text(opt)
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                val canNext = (answers[index] != null) // must answer
                Button(onClick = {
                    if (index < total - 1) {
                        // Lock current (no back UI). Move to next.
                        index++
                    } else {
                        // all answered? If some not answered, jump to first null.
                        val firstUnanswered = answers.indexOfFirst { it == null }
                        if (firstUnanswered >= 0) {
                            index = firstUnanswered
                        } else {
                            showResult = true
                        }
                    }
                }, enabled = canNext) {
                    Text(if (index < total - 1) "Next" else "Finish")
                }
            }
        }
    }
}
