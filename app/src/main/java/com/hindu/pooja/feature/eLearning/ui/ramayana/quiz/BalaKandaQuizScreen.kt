package com.hindu.pooja.feature.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalaKandaQuizScreen(
    repo: QuizModule,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val questions = repo.questions
    val total = questions.size

    // Map<QuestionId, SelectedIndexes>
    val answers = remember { mutableStateMapOf<String, Set<Int>>() }

    var index by rememberSaveable { mutableIntStateOf(0) }
    val q = questions[index]

    // Hold current question's selection as State; always assign a NEW set on toggle
    var selected by remember(q.id) {
        mutableStateOf(answers[q.id] ?: emptySet())
    }

    // Exit confirmation
    var showExit by remember { mutableStateOf(false) }
    BackHandler { showExit = true }

    // Final result
    var showResult by remember { mutableStateOf(false) }

    Surface {
        Column(Modifier.fillMaxSize()) {

            TopAppBar(
                title = { Text(text = repo.title) },
                navigationIcon = {},
                actions = {},
                colors = TopAppBarDefaults.topAppBarColors()
            )

            // Question header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Question ${index + 1} / $total",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = q.text,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Divider()

            // Options
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(q.options) { i, option ->
                    OptionRow(
                        text = option,
                        checked = i in selected,
                        onToggle = {
                            selected = if (i in selected) selected - i else selected + i
                            answers[q.id] = selected // persist for progress & result
                        }
                    )
                }
            }

            Divider()

            // Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Answered: ${answers.size} / $total",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showExit = true }) {
                        Text("Exit")
                    }
                    Button(
                        enabled = selected.isNotEmpty(), // answer mandatory
                        onClick = {
                            // Persist current selection
                            answers[q.id] = selected

                            if (index < total - 1) {
                                index += 1
                                val next = questions[index]
                                selected = answers[next.id] ?: emptySet()
                            } else {
                                showResult = true
                            }
                        }
                    ) {
                        Text(if (index < total - 1) "Next" else "Finish")
                    }
                }
            }
        }
    }

    if (showExit) {
        AlertDialog(
            onDismissRequest = { showExit = false },
            title = { Text("Exit quiz?") },
            text = { Text("Your answers so far will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    showExit = false
                    onBack()
                }) { Text("Exit") }
            },
            dismissButton = {
                TextButton(onClick = { showExit = false }) { Text("Stay") }
            }
        )
    }

    if (showResult) {
        val score = remember(answers) {
            repo.questions.count { qn ->
                (answers[qn.id] ?: emptySet()) == qn.correct
            }
        }
        val percent = (score * 100f / total).toInt()
        val pass = percent >= repo.passPercent
        AlertDialog(
            onDismissRequest = { /* block */ },
            title = { Text(if (pass) "🎉 You passed!" else "Keep practicing") },
            text = {
                Column {
                    Text("Score: $score / $total  (${percent}%)")
                    Spacer(Modifier.height(6.dp))
                    Text("Pass mark: ${repo.passPercent}%")
                }
            },
            confirmButton = {
                Button(onClick = {
                    showResult = false
                    onFinish()
                }) { Text("Done") }
            }
        )
    }
}

@Composable
private fun OptionRow(
    text: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF2B1E0A)
        )
    }
}
