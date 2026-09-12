package com.example.cglprep.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cglprep.data.ErrorBucket
import com.example.cglprep.data.Question
import com.example.cglprep.ui.ExamResultData
import com.example.cglprep.ui.theme.*

@Composable
fun ExamResultScreen(
    result: ExamResultData,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onLogCustomErrorBucket: (question: Question, bucket: ErrorBucket) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSolutionsTab by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                modifier = Modifier.fillMaxWidth().testTag("result_score_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        result.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF93C5FD)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "%.2f".format(result.score),
                        fontWeight = FontWeight.Black,
                        fontSize = 42.sp,
                        color = AccentGold
                    )
                    Text(
                        "out of ${result.maxScore.toInt()} Marks",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Stat Badges Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ResultMetric(title = "Accuracy", value = "${result.accuracy}%", color = Color(0xFF10B981))
                        ResultMetric(title = "Attempted", value = "${result.attempted}", color = Color.White)
                        ResultMetric(title = "Correct", value = "${result.correct}", color = Color(0xFF10B981))
                        ResultMetric(title = "Incorrect", value = "${result.wrong}", color = Color(0xFFEF4444))
                    }
                }
            }
        }

        // Time Telemetry Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
                        Text("Time Telemetry Analysis", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyDark)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Time Sinks
                    if (result.telemetry.timeSinks.isNotEmpty()) {
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "⚠️ Time Sinks (>90s spent on wrong answers):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB91C1C)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    result.telemetry.timeSinks.joinToString(", ") { "Q${it.first} (${it.second}s)" },
                                    fontSize = 12.sp,
                                    color = Color(0xFF7F1D1D)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Speed Wins
                    if (result.telemetry.speedWins.isNotEmpty()) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "⚡ Speed Wins (<30s on correct answers):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    result.telemetry.speedWins.take(6).joinToString(", ") { "Q${it.first} (${it.second}s)" },
                                    fontSize = 12.sp,
                                    color = Color(0xFF14532D)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Breakdown Table
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Section-wise Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyDark)
                    Spacer(modifier = Modifier.height(10.dp))

                    result.sectionStats.forEach { stat ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stat.section, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NavyDark)
                                Text(
                                    "%.1f Marks".format(stat.score),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (stat.score >= 0) Color(0xFF15803D) else Color(0xFFB91C1C)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Att: ${stat.attempted}  •  Cor: ${stat.correct}  •  Wro: ${stat.wrong}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    "Acc: ${stat.accuracy}%  •  Time: ${stat.timeSpentSeconds / 60}m / ${stat.recommendedTimeMins}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Divider(modifier = Modifier.padding(top = 8.dp), color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }

        // Solutions Review Toggle Button
        item {
            Button(
                onClick = { showSolutionsTab = !showSolutionsTab },
                colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("toggle_solutions_button")
            ) {
                Icon(
                    if (showSolutionsTab) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (showSolutionsTab) "Hide Solutions & Explanations" else "Review Solutions & Categorize Mistakes",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Solutions & Explanations List
        if (showSolutionsTab) {
            itemsIndexed(result.questions) { index, q ->
                val userChoice = result.userAnswers[q.id]
                val isCorrect = userChoice != null && userChoice.trim().equals(q.correct.trim(), ignoreCase = true)
                val isUnattempted = userChoice == null

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isCorrect -> Color(0xFFF0FDF4)
                            isUnattempted -> Color.White
                            else -> Color(0xFFFEF2F2)
                        }
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            isCorrect -> Color(0xFF86EFAC)
                            isUnattempted -> Color(0xFFE2E8F0)
                            else -> Color(0xFFFCA5A5)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("solution_card_${q.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Q${index + 1} • ${q.section}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF64748B))
                            Surface(
                                color = when {
                                    isCorrect -> Color(0xFF15803D)
                                    isUnattempted -> Color(0xFF64748B)
                                    else -> Color(0xFFDC2626)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = when {
                                        isCorrect -> "CORRECT"
                                        isUnattempted -> "UNATTEMPTED"
                                        else -> "INCORRECT"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val stemText = if (isHindi && !q.stem_hi.isNullOrBlank()) q.stem_hi else q.stem
                        Text(stemText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF0F172A))

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Your Answer: ${userChoice ?: "None"}", fontSize = 12.sp, color = if (isCorrect) Color(0xFF15803D) else Color(0xFFDC2626))
                        Text("Correct Answer: ${q.correct}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))

                        if (!q.solution.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(color = Color.White, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Explanation:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = NavyDark)
                                    val solText = if (isHindi && !q.solution_hi.isNullOrBlank()) q.solution_hi else q.solution
                                    Text(solText ?: "", fontSize = 12.sp, color = Color(0xFF334155))
                                }
                            }
                        }

                        // If incorrect, show bucket classification buttons
                        if (!isCorrect && !isUnattempted) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Reclassify in Error Diary:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ErrorBucket.values().forEach { bucket ->
                                    OutlinedButton(
                                        onClick = { onLogCustomErrorBucket(q, bucket) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(bucket.title.split(" ").first(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Back to Dashboard button
        item {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("result_back_to_dashboard")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Return to Dashboard", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ResultMetric(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        Text(title, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
    }
}
