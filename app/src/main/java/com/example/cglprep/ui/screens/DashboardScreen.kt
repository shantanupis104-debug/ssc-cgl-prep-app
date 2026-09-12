package com.example.cglprep.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cglprep.data.*
import com.example.cglprep.ui.theme.AccentGold
import com.example.cglprep.ui.theme.NavyDark
import com.example.cglprep.ui.theme.NavyPrimary

@Composable
fun DashboardScreen(
    activePillar: ExamPillar,
    dailyMocks: List<MockExam>,
    bankingMocks: List<MockExam>,
    railwayMocks: List<MockExam>,
    spacedDueErrors: List<ErrorDiaryEntity>,
    totalErrors: Int,
    attempts: List<ExamAttemptEntity>,
    onSelectPillar: (ExamPillar) -> Unit,
    onStartMock: (MockExam) -> Unit,
    onLaunchSpacedRevision: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayedMocks = when (activePillar) {
        ExamPillar.SSC -> dailyMocks
        ExamPillar.BANKING -> bankingMocks.ifEmpty { dailyMocks }
        ExamPillar.RAILWAY -> railwayMocks.ifEmpty { dailyMocks }
    }

    val totalAttempts = attempts.size
    val avgScore = if (attempts.isNotEmpty()) {
        "%.1f".format(attempts.map { it.score }.average())
    } else "0.0"
    val avgAccuracy = if (attempts.isNotEmpty()) {
        "${attempts.map { it.accuracy }.average().toInt()}%"
    } else "0%"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Spaced Repetition Due Banner
        if (spacedDueErrors.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF4F46E5), Color(0xFF3730A3))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .testTag("spaced_rep_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                                Text(
                                    "Spaced Repetition Due!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${spacedDueErrors.size} mistake(s) scheduled for spaced revision today.",
                                fontSize = 12.sp,
                                color = Color(0xFFC7D2FE)
                            )
                        }

                        Button(
                            onClick = onLaunchSpacedRevision,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("start_spaced_revision_button")
                        ) {
                            Text("Revise Now", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Exam Pillar Switcher
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Target Exam Pillar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NavyDark
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExamPillar.values().forEach { pillar ->
                        val isSelected = pillar == activePillar
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) NavyDark else Color(0xFFF1F5F9),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AccentGold) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectPillar(pillar) }
                                .testTag("pillar_${pillar.key}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    pillar.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else NavyDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    pillar.badge,
                                    fontSize = 10.sp,
                                    color = if (isSelected) AccentGold else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Stats Overview Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Performance Snapshot", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        title = "Mocks Attempted",
                        value = totalAttempts.toString(),
                        subtitle = "Target: 50+",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Avg Score",
                        value = "$avgScore / 200",
                        subtitle = "Cutoff: ~145",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        title = "Overall Accuracy",
                        value = avgAccuracy,
                        subtitle = "Ideal: >85%",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "3-Bucket Errors",
                        value = totalErrors.toString(),
                        subtitle = "Review scheduled",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // TCS iON Daily Mocks Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "${activePillar.label} Live Mocks",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NavyDark
                    )
                    Text(
                        "Official TCS iON CBT interface & marking scheme",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        items(displayedMocks) { mock ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("mock_card_${mock.mock_id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Mock #${mock.mock_number}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            mock.date ?: "Live",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        mock.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NavyDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DetailChip(icon = Icons.Default.Quiz, text = "${mock.questions.size.ifZero(mock.total_questions)} Qs")
                        DetailChip(icon = Icons.Default.Timer, text = "${mock.duration_minutes} Mins")
                        DetailChip(icon = Icons.Default.Grade, text = "${mock.max_marks.toInt()} Marks")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onStartMock(mock) },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("start_mock_${mock.mock_id}")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp), tint = AccentGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start TCS iON Simulator", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyDark)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = AccentGold, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DetailChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFF64748B))
        Text(text, fontSize = 11.sp, color = Color(0xFF475569))
    }
}

private fun Int.ifZero(defaultVal: Int): Int = if (this == 0) defaultVal else this
