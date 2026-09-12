package com.example.cglprep.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cglprep.data.ErrorBucket
import com.example.cglprep.data.ErrorDiaryEntity
import com.example.cglprep.ui.theme.AccentGold
import com.example.cglprep.ui.theme.NavyDark
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ErrorDiaryScreen(
    errors: List<ErrorDiaryEntity>,
    selectedBucket: String,
    isHindi: Boolean,
    onFilterBucket: (String) -> Unit,
    onChangeBucket: (qid: Int, bucket: ErrorBucket) -> Unit,
    onMarkMastered: (qid: Int, mastered: Boolean) -> Unit,
    onDeleteError: (qid: Int) -> Unit,
    onLaunchRetest: (List<ErrorDiaryEntity>) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredErrors = remember(errors, selectedBucket) {
        if (selectedBucket == "ALL") errors else errors.filter { it.bucket == selectedBucket }
    }

    val dueToday = remember(errors) {
        val now = System.currentTimeMillis()
        errors.filter { !it.isMastered && it.nextReviewDate <= now }
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    "📕 3-Bucket Error Diary & Spaced Repetition",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NavyDark
                )
                Text(
                    "Transform mistakes into 100% accuracy using intelligent spaced recall intervals",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Spaced Repetition Retest action card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                modifier = Modifier.fillMaxWidth().testTag("error_retest_banner")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Adaptive Spaced Retest",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            if (dueToday.isNotEmpty()) "${dueToday.size} questions due for spaced recall today!"
                            else "All ${errors.size} logged errors ready for review retest.",
                            fontSize = 12.sp,
                            color = Color(0xFF93C5FD)
                        )
                    }

                    Button(
                        onClick = {
                            val targetList = if (dueToday.isNotEmpty()) dueToday else errors
                            if (targetList.isNotEmpty()) {
                                onLaunchRetest(targetList)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                        shape = RoundedCornerShape(8.dp),
                        enabled = errors.isNotEmpty(),
                        modifier = Modifier.testTag("launch_error_retest_button")
                    ) {
                        Text("Retest Now", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Bucket Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedBucket == "ALL",
                        onClick = { onFilterBucket("ALL") },
                        label = { Text("All Errors (${errors.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyDark,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                items(ErrorBucket.values()) { b ->
                    val count = errors.count { it.bucket == b.code }
                    FilterChip(
                        selected = selectedBucket == b.code,
                        onClick = { onFilterBucket(b.code) },
                        label = { Text("${b.title} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyDark,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (filteredErrors.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No mistakes in this bucket!", fontWeight = FontWeight.Bold, color = NavyDark)
                        Text("Keep practicing CBT mocks to maintain 100% accuracy.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Error Cards
        items(filteredErrors) { err ->
            val bucketObj = ErrorBucket.values().find { it.code == err.bucket } ?: ErrorBucket.BUCKET_A
            val isDue = !err.isMastered && err.nextReviewDate <= System.currentTimeMillis()

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (err.isMastered) Color(0xFFF8FAFC) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("error_card_${err.qid}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when (bucketObj) {
                                ErrorBucket.BUCKET_A -> Color(0xFFEFF6FF)
                                ErrorBucket.BUCKET_B -> Color(0xFFFEF3C7)
                                ErrorBucket.BUCKET_C -> Color(0xFFFEE2E2)
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = bucketObj.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (bucketObj) {
                                    ErrorBucket.BUCKET_A -> Color(0xFF1D4ED8)
                                    ErrorBucket.BUCKET_B -> Color(0xFFB45309)
                                    ErrorBucket.BUCKET_C -> Color(0xFFB91C1C)
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (isDue) {
                                Surface(color = Color(0xFFDC2626), shape = RoundedCornerShape(4.dp)) {
                                    Text("DUE TODAY", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            } else {
                                Text(
                                    "Next: ${dateFormat.format(Date(err.nextReviewDate))}",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }

                            IconButton(onClick = { onDeleteError(err.qid) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val stemText = if (isHindi && !err.stemHi.isNullOrBlank()) err.stemHi else err.stem
                    Text(stemText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF0F172A))

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Selected: ${err.userChoice}", fontSize = 11.sp, color = Color(0xFFDC2626))
                        Text("Correct: ${err.correct}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                    }

                    if (!err.solution.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val solText = if (isHindi && !err.solutionHi.isNullOrBlank()) err.solutionHi else err.solution
                        Text(solText ?: "", fontSize = 11.sp, color = Color(0xFF475569))
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    // Action row: Change bucket & Mastered checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            ErrorBucket.values().forEach { b ->
                                val isCur = b.code == err.bucket
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isCur) NavyDark else Color(0xFFF1F5F9),
                                    modifier = Modifier.clickable { onChangeBucket(err.qid, b) }
                                ) {
                                    Text(
                                        b.title.split(" ").first(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCur) Color.White else Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = err.isMastered,
                                onCheckedChange = { onMarkMastered(err.qid, it) }
                            )
                            Text(
                                "Mastered",
                                fontSize = 11.sp,
                                fontWeight = if (err.isMastered) FontWeight.Bold else FontWeight.Normal,
                                color = if (err.isMastered) Color(0xFF15803D) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }
    }
}
