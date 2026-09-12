package com.example.cglprep.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Translate
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
import com.example.cglprep.data.ExamPreset
import com.example.cglprep.data.StudentProfileEntity
import com.example.cglprep.ui.theme.AccentGold
import com.example.cglprep.ui.theme.NavyDark
import com.example.cglprep.ui.theme.NavyPrimary

@Composable
fun TopHeader(
    activeStudent: StudentProfileEntity?,
    activePreset: ExamPreset,
    isHindi: Boolean,
    onPresetSelected: (ExamPreset) -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenProfileModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPresetMenu by remember { mutableStateOf(false) }

    Surface(
        color = NavyDark,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            // Row 1: Brand Logo + Title + Student Profile Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(AccentGold, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "CGL",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Column {
                        Text(
                            text = "SSC CGL 2026 Prep Pro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Official TCS iON CBT Simulator",
                            fontSize = 11.sp,
                            color = Color(0xFF93C5FD)
                        )
                    }
                }

                // Student Profile Chip
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .clickable { onOpenProfileModal() }
                        .testTag("student_profile_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(activeStudent?.avatarColor ?: 0xFFF59E0BL)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeStudent?.name?.firstOrNull()?.toString() ?: "A",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = activeStudent?.name?.split(" ")?.firstOrNull() ?: "Aspirant",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                            Text(
                                text = activeStudent?.rollNumber ?: "CGL-2026",
                                fontSize = 9.sp,
                                color = Color(0xFF93C5FD)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Exam Preset Selector + Language Switcher Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Preset Dropdown Button
                Box {
                    Surface(
                        color = NavyPrimary,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clickable { showPresetMenu = true }
                            .testTag("preset_dropdown_trigger")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = activePreset.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Preset",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showPresetMenu,
                        onDismissRequest = { showPresetMenu = false }
                    ) {
                        ExamPreset.values().forEach { preset ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(preset.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(
                                            "${preset.totalQuestions} Qs • ${preset.durationMinutes}m • +${preset.markCorrect} / -${preset.markWrong}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                },
                                onClick = {
                                    onPresetSelected(preset)
                                    showPresetMenu = false
                                }
                            )
                        }
                    }
                }

                // Bilingual toggle
                Surface(
                    color = if (isHindi) AccentGold else Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clickable { onToggleLanguage() }
                        .testTag("language_toggle_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Translate",
                            tint = if (isHindi) Color(0xFF0F172A) else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isHindi) "हिन्दी (HI)" else "English (EN)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHindi) Color(0xFF0F172A) else Color.White
                        )
                    }
                }
            }
        }
    }
}
