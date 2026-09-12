package com.example.cglprep.ui.screens

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
import com.example.cglprep.data.RevisionCard
import com.example.cglprep.ui.theme.AccentGold
import com.example.cglprep.ui.theme.NavyDark
import com.example.cglprep.ui.theme.NavyPrimary

@Composable
fun RevisionVaultScreen(
    revisionCards: List<RevisionCard>,
    onBookmark: (category: String, title: String, content: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("ALL") }
    val bookmarkedIds = remember { mutableStateMapOf<String, Boolean>() }

    val subjects = remember(revisionCards) {
        listOf("ALL") + revisionCards.map { it.subject }.distinct()
    }

    val filteredCards = remember(revisionCards, searchQuery, selectedSubject) {
        revisionCards.filter { card ->
            val matchesSubject = selectedSubject == "ALL" || card.subject == selectedSubject
            val matchesQuery = searchQuery.isBlank() ||
                    card.title.contains(searchQuery, ignoreCase = true) ||
                    card.points.any { it.contains(searchQuery, ignoreCase = true) } ||
                    card.formulaOrTrick?.contains(searchQuery, ignoreCase = true) == true
            matchesSubject && matchesQuery
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    "📚 Subject Revision & Boosters Vault",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NavyDark
                )
                Text(
                    "High-yield formulas, English vocabulary roots, and GK quick capsules",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Search bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search formulas, tricks, idioms, articles...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyDark,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                ),
                modifier = Modifier.fillMaxWidth().testTag("revision_search_field")
            )
        }

        // Category Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subjects) { subj ->
                    val isSelected = selectedSubject == subj
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSubject = subj },
                        label = {
                            Text(
                                if (subj == "ALL") "All Topics" else subj.lowercase().capitalize(),
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyDark,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Revision Cards List
        items(filteredCards) { card ->
            val isBookmarked = bookmarkedIds[card.id] == true

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("revision_card_${card.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = AccentGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = card.subject,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                bookmarkedIds[card.id] = !isBookmarked
                                onBookmark(card.subject, card.title, card.points.joinToString("\n"))
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) AccentGold else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        card.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NavyDark
                    )

                    if (!card.formulaOrTrick.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Functions, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                                Text(
                                    card.formulaOrTrick,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    card.points.forEach { pt ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("•", color = AccentGold, fontWeight = FontWeight.Black)
                            Text(pt, fontSize = 13.sp, color = Color(0xFF334155), lineHeight = 19.sp)
                        }
                    }
                }
            }
        }
    }
}
