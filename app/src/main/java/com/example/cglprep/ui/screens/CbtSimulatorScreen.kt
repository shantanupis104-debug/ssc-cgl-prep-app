package com.example.cglprep.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cglprep.data.ExamPreset
import com.example.cglprep.data.Question
import com.example.cglprep.data.QuestionStatus
import com.example.cglprep.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CbtSimulatorScreen(
    questions: List<Question>,
    currentIndex: Int,
    userAnswers: Map<Int, String>,
    questionStatuses: Map<Int, QuestionStatus>,
    timeSpentPerQuestion: Map<Int, Long>,
    remainingSeconds: Long,
    activePreset: ExamPreset,
    isHindi: Boolean,
    onSelectOption: (String) -> Unit,
    onClearResponse: () -> Unit,
    onSaveAndNext: () -> Unit,
    onMarkForReviewAndNext: () -> Unit,
    onJumpToQuestion: (Int) -> Unit,
    onSubmitExam: () -> Unit,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (questions.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active exam. Select a mock test from the Dashboard.", color = Color.Gray)
        }
        return
    }

    var showPaletteSheet by remember { mutableStateOf(false) }
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }

    val currentQ = questions.getOrNull(currentIndex) ?: questions[0]
    val selectedOption = userAnswers[currentQ.id]
    val timeSpent = timeSpentPerQuestion[currentQ.id] ?: 0L

    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val timerText = "%02d:%02d".format(mins, secs)
    val isTimerLow = remainingSeconds < 300 // < 5 mins

    // Section list
    val sections = listOf(
        "General Intelligence & Reasoning",
        "General Awareness",
        "Quantitative Aptitude",
        "English Comprehension"
    )

    // Palette Counts
    val answeredCount = questionStatuses.values.count { it == QuestionStatus.ANSWERED }
    val notAnsweredCount = questionStatuses.values.count { it == QuestionStatus.NOT_ANSWERED }
    val notVisitedCount = questionStatuses.values.count { it == QuestionStatus.NOT_VISITED }
    val markedReviewCount = questionStatuses.values.count { it == QuestionStatus.MARKED_FOR_REVIEW }
    val answeredMarkedCount = questionStatuses.values.count { it == QuestionStatus.ANSWERED_AND_MARKED }

    Scaffold(
        topBar = {
            Surface(
                color = NavyDark,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
                    // Row 1: Section name & Timer & Palette toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Section Badge
                        Surface(
                            color = NavyLight,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = currentQ.section,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Countdown Timer
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .background(
                                    if (isTimerLow) Color(0xFFEF4444) else Color(0xFF1E293B),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = if (isTimerLow) Color.White else AccentGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = timerText,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }

                        // Question Palette Trigger Button
                        IconButton(
                            onClick = { showPaletteSheet = true },
                            modifier = Modifier.testTag("cbt_palette_button")
                        ) {
                            Icon(Icons.Default.GridView, contentDescription = "Question Palette", tint = AccentGold)
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Action controls (Clear, Mark for review, Save & Next)
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onClearResponse,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f).testTag("cbt_clear_response_button")
                        ) {
                            Text("Clear", fontSize = 12.sp, color = Color(0xFF64748B))
                        }

                        Button(
                            onClick = onMarkForReviewAndNext,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusMarkedReview),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1.3f).testTag("cbt_mark_review_button")
                        ) {
                            Text("Mark & Next", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = onSaveAndNext,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusAnswered),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1.4f).testTag("cbt_save_next_button")
                        ) {
                            Text("Save & Next", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { showSubmitConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("cbt_submit_exam_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit Exam", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Meta info row (Q Num, Marks, Per-Q Time)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${currentIndex + 1} / ${questions.size}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NavyDark
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "+${activePreset.markCorrect} / -${activePreset.markWrong}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF16A34A)
                    )
                    Text(
                        "•  ${timeSpent}s spent",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE2E8F0))

            // Question Stem (EN or HI)
            val stemText = if (isHindi && !currentQ.stem_hi.isNullOrBlank()) currentQ.stem_hi else currentQ.stem
            Text(
                text = stemText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 24.sp,
                color = Color(0xFF0F172A),
                modifier = Modifier.testTag("question_stem_text")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Options List
            val optionsList = if (isHindi && !currentQ.options_hi.isNullOrEmpty()) currentQ.options_hi else currentQ.options
            val optionLabels = listOf("A", "B", "C", "D", "E")

            optionsList.forEachIndexed { optIdx, optionText ->
                val label = optionLabels.getOrElse(optIdx) { "${optIdx + 1}" }
                // Option matching: compare either with optionText or with option label
                val isSelected = selectedOption == optionText || selectedOption == label

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) AccentGold.copy(alpha = 0.15f) else Color.White,
                    border = androidx.compose.foundation.BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) AccentGold else Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            onSelectOption(optionText)
                        }
                        .testTag("option_${optIdx}_button")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) AccentGold else Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) Color(0xFF0F172A) else Color(0xFF475569)
                            )
                        }

                        Text(
                            text = optionText,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    // Question Palette BottomSheet / Modal
    if (showPaletteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaletteSheet = false },
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "TCS iON Question Palette",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NavyDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status summary chips
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    StatusLegendBadge(color = StatusAnswered, label = "Answered", count = answeredCount)
                    StatusLegendBadge(color = StatusNotAnswered, label = "Not Answered", count = notAnsweredCount)
                    StatusLegendBadge(color = StatusNotVisited, label = "Not Visited", count = notVisitedCount)
                    StatusLegendBadge(color = StatusMarkedReview, label = "Review", count = markedReviewCount)
                    StatusLegendBadge(color = StatusAnsweredMarked, label = "Ans & Rev", count = answeredMarkedCount)
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))

                Text("Jump to Question:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(280.dp)
                ) {
                    itemsIndexed(questions) { index, q ->
                        val status = questionStatuses[q.id] ?: QuestionStatus.NOT_VISITED
                        val isCurrent = index == currentIndex

                        val bg = when (status) {
                            QuestionStatus.ANSWERED -> StatusAnswered
                            QuestionStatus.NOT_ANSWERED -> StatusNotAnswered
                            QuestionStatus.MARKED_FOR_REVIEW -> StatusMarkedReview
                            QuestionStatus.ANSWERED_AND_MARKED -> StatusAnsweredMarked
                            QuestionStatus.NOT_VISITED -> Color(0xFFF1F5F9)
                        }
                        val textColor = if (status == QuestionStatus.NOT_VISITED) Color(0xFF475569) else Color.White

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bg)
                                .border(
                                    if (isCurrent) 2.dp else 0.dp,
                                    if (isCurrent) AccentGold else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onJumpToQuestion(index)
                                    showPaletteSheet = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }

    // Submit confirmation dialog
    if (showSubmitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirmDialog = false },
            title = { Text("Submit Exam?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Are you sure you want to finish and evaluate this exam?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Answered: $answeredCount", color = StatusAnswered, fontWeight = FontWeight.Bold)
                    Text("• Not Answered: $notAnsweredCount", color = StatusNotAnswered)
                    Text("• Marked for Review: $markedReviewCount", color = StatusMarkedReview)
                    Text("• Not Visited: $notVisitedCount", color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmDialog = false
                        onSubmitExam()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C))
                ) {
                    Text("Yes, Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatusLegendBadge(color: Color, label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(count.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 9.sp, color = Color(0xFF64748B))
    }
}
