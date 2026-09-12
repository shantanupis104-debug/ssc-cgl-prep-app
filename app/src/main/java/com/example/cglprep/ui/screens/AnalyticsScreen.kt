package com.example.cglprep.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cglprep.data.ExamAttemptEntity
import com.example.cglprep.ui.theme.AccentGold
import com.example.cglprep.ui.theme.NavyDark
import com.example.cglprep.ui.theme.NavyPrimary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(
    attempts: List<ExamAttemptEntity>,
    modifier: Modifier = Modifier
) {
    val totalAttempts = attempts.size
    val avgScore = if (attempts.isNotEmpty()) attempts.map { it.score }.average() else 0.0
    val avgAccuracy = if (attempts.isNotEmpty()) attempts.map { it.accuracy }.average().toInt() else 0
    val highestScore = if (attempts.isNotEmpty()) attempts.maxOf { it.score } else 0.0

    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    "📊 Performance Analytics & Readiness",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NavyDark
                )
                Text(
                    "Real-time score trends, cutoff benchmark & speed metrics",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Exam Readiness Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                modifier = Modifier.fillMaxWidth().testTag("readiness_meter_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("SSC CGL Readiness Index", color = Color(0xFF93C5FD), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            val readiness = ((avgScore / 200.0) * 100).toInt().coerceIn(10, 100)
                            Text("$readiness%", fontSize = 32.sp, fontWeight = FontWeight.Black, color = AccentGold)
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (avgScore >= 145) "🟢 Cutoff Qualified" else "🟠 Cutoff In Range (145+)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { (avgScore / 200.0).toFloat().coerceIn(0.05f, 1f) },
                        color = AccentGold,
                        trackColor = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricItem(title = "Avg Score", value = "%.1f".format(avgScore))
                        MetricItem(title = "Max Score", value = "%.1f".format(highestScore))
                        MetricItem(title = "Avg Accuracy", value = "$avgAccuracy%")
                        MetricItem(title = "Tests Taken", value = "$totalAttempts")
                    }
                }
            }
        }

        // Subject Benchmark Targets
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pillar Targets (Tier 1)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
                    Spacer(modifier = Modifier.height(10.dp))

                    BenchmarkRow("Quantitative Aptitude", "45+ / 50 M", 0.90f, Color(0xFF3B82F6))
                    BenchmarkRow("Reasoning & Intelligence", "46+ / 50 M", 0.92f, Color(0xFF10B981))
                    BenchmarkRow("English Comprehension", "44+ / 50 M", 0.88f, Color(0xFF8B5CF6))
                    BenchmarkRow("General Awareness", "32+ / 50 M", 0.64f, Color(0xFFF59E0B))
                }
            }
        }

        // Recent Mock History
        item {
            Text("Recent CBT Attempt History", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyDark)
        }

        if (attempts.isEmpty()) {
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No mock exams recorded yet. Attempt a test to see detailed analytics!", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
        }

        items(attempts) { attempt ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().testTag("attempt_item_${attempt.attemptId}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(attempt.mockTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            dateFormat.format(Date(attempt.timestamp)),
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Att: ${attempt.attemptedCount}  •  Cor: ${attempt.correctCount}  •  Wro: ${attempt.wrongCount}  •  ${attempt.timeSpentSeconds / 60}m",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "%.1f".format(attempt.score),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = NavyPrimary
                        )
                        Text(
                            "${attempt.accuracy}% Acc",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (attempt.accuracy >= 80) Color(0xFF15803D) else Color(0xFFD97706)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
        Text(title, fontSize = 10.sp, color = Color(0xFF93C5FD))
    }
}

@Composable
private fun BenchmarkRow(subject: String, target: String, progress: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(subject, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
            Text(target, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            color = color,
            trackColor = Color(0xFFF1F5F9),
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
        )
    }
}
