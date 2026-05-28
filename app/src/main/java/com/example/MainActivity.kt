package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddTransactionDialog
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrendsScreen
import com.example.ui.theme.*
import com.example.viewmodel.BudgetViewModel

enum class AppTab {
    HOME, TRENDS, BUDGET, SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BudgetAppMain()
            }
        }
    }
}

@Composable
fun BudgetAppMain(
    viewModel: BudgetViewModel = viewModel()
) {
    var currentTab by remember { mutableStateOf(AppTab.HOME) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBG,
        bottomBar = {
            CustomBottomBar(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it },
                onAddClicked = { showAddDialog = true }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()) // Account for bottom bar
        ) {
            // Animated transitions between screen tabs
            when (currentTab) {
                AppTab.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToReports = { currentTab = AppTab.TRENDS },
                    modifier = Modifier.fillMaxSize()
                )
                AppTab.TRENDS -> TrendsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
                AppTab.BUDGET -> BudgetScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
                AppTab.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Add Transaction Trigger Dialog overlaid
        if (showAddDialog) {
            AddTransactionDialog(
                viewModel = viewModel,
                onDismiss = { showAddDialog = false },
                onConfirm = { showAddDialog = false }
            )
        }
    }
}

@Composable
fun CustomBottomBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onAddClicked: () -> Unit
) {
    // Mimics: <nav class="bg-[#2B2930] px-6 py-4 flex items-center justify-between rounded-t-[24px]">
    // We add navigation bars padding to automatically respect safe interactive gesture pills
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBG)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(DarkCategory)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTabItem(
                tab = AppTab.HOME,
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = selectedTab == AppTab.HOME,
                onSelected = onTabSelected,
                tag = "nav_home"
            )

            BottomTabItem(
                tab = AppTab.TRENDS,
                icon = Icons.Default.BarChart,
                label = "Trends",
                isSelected = selectedTab == AppTab.TRENDS,
                onSelected = onTabSelected,
                tag = "nav_trends"
            )

            // Center ADD Floating Button overlapping slightly upwards (-mt-10 in design HTML)
            Box(
                modifier = Modifier
                    .offset(y = (-14).dp)
                    .testTag("nav_add_transaction")
            ) {
                Button(
                    onClick = onAddClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPurple,
                        contentColor = AccentPurpleOnContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 2.dp
                    ),
                    modifier = Modifier.size(56.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Record transaction",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            BottomTabItem(
                tab = AppTab.BUDGET,
                icon = Icons.Default.AccountBalanceWallet,
                label = "Budget",
                isSelected = selectedTab == AppTab.BUDGET,
                onSelected = onTabSelected,
                tag = "nav_budget"
            )

            BottomTabItem(
                tab = AppTab.SETTINGS,
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = selectedTab == AppTab.SETTINGS,
                onSelected = onTabSelected,
                tag = "nav_settings"
            )
        }
    }
}

@Composable
fun BottomTabItem(
    tab: AppTab,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onSelected: (AppTab) -> Unit,
    tag: String
) {
    val contentColor = if (isSelected) AccentPurple else TextSub

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelected(tab) }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", color = Color.White, modifier = modifier)
}
