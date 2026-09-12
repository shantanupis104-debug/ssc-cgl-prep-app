package com.example.cglprep.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cglprep.ui.AppTab
import com.example.cglprep.ui.theme.AccentGold
import com.example.cglprep.ui.theme.NavyDark

@Composable
fun BottomNavBar(
    currentTab: AppTab,
    errorCount: Int,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = NavyDark,
        tonalElevation = 8.dp,
        modifier = modifier
    ) {
        NavigationBarItem(
            selected = currentTab == AppTab.DASHBOARD,
            onClick = { onTabSelected(AppTab.DASHBOARD) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard", modifier = Modifier.size(20.dp)) },
            label = { Text("Home", fontSize = 10.sp, fontWeight = if (currentTab == AppTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                indicatorColor = AccentGold,
                selectedTextColor = AccentGold,
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_dashboard")
        )

        NavigationBarItem(
            selected = currentTab == AppTab.CBT_EXAM,
            onClick = { onTabSelected(AppTab.CBT_EXAM) },
            icon = { Icon(Icons.Default.Quiz, contentDescription = "CBT", modifier = Modifier.size(20.dp)) },
            label = { Text("CBT", fontSize = 10.sp, fontWeight = if (currentTab == AppTab.CBT_EXAM) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                indicatorColor = AccentGold,
                selectedTextColor = AccentGold,
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_cbt")
        )

        NavigationBarItem(
            selected = currentTab == AppTab.PYQ_VAULT,
            onClick = { onTabSelected(AppTab.PYQ_VAULT) },
            icon = { Icon(Icons.Default.HistoryEdu, contentDescription = "PYQ", modifier = Modifier.size(20.dp)) },
            label = { Text("10Y PYQ", fontSize = 10.sp, fontWeight = if (currentTab == AppTab.PYQ_VAULT) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                indicatorColor = AccentGold,
                selectedTextColor = AccentGold,
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_pyq")
        )

        NavigationBarItem(
            selected = currentTab == AppTab.ERROR_DIARY,
            onClick = { onTabSelected(AppTab.ERROR_DIARY) },
            icon = {
                BadgedBox(
                    badge = {
                        if (errorCount > 0) {
                            Badge(containerColor = Color(0xFFEF4444)) {
                                Text(errorCount.toString(), fontSize = 9.sp, color = Color.White)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = "Error Diary", modifier = Modifier.size(20.dp))
                }
            },
            label = { Text("Errors", fontSize = 10.sp, fontWeight = if (currentTab == AppTab.ERROR_DIARY) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                indicatorColor = AccentGold,
                selectedTextColor = AccentGold,
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_error_diary")
        )

        NavigationBarItem(
            selected = currentTab == AppTab.ANALYTICS,
            onClick = { onTabSelected(AppTab.ANALYTICS) },
            icon = { Icon(Icons.Default.Assessment, contentDescription = "Analytics", modifier = Modifier.size(20.dp)) },
            label = { Text("Analytics", fontSize = 10.sp, fontWeight = if (currentTab == AppTab.ANALYTICS) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                indicatorColor = AccentGold,
                selectedTextColor = AccentGold,
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_analytics")
        )

        NavigationBarItem(
            selected = currentTab == AppTab.REVISION,
            onClick = { onTabSelected(AppTab.REVISION) },
            icon = { Icon(Icons.Default.School, contentDescription = "Revision", modifier = Modifier.size(20.dp)) },
            label = { Text("Boosters", fontSize = 10.sp, fontWeight = if (currentTab == AppTab.REVISION) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                indicatorColor = AccentGold,
                selectedTextColor = AccentGold,
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_revision")
        )
    }
}
