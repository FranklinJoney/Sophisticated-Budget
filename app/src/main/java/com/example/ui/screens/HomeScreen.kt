package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.DatadogMonitor
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryHelpers
import com.example.ui.theme.*
import com.example.viewmodel.BudgetViewModel
import com.example.viewmodel.MonthBudgetSummary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: BudgetViewModel,
    onNavigateToReports: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.currentMonthBudgetState.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val rawMonth by viewModel.selectedMonth.collectAsState()

    var selectedCategoryForModal by remember { mutableStateOf<String?>(null) }
    var showAllTransactionsModal by remember { mutableStateOf(false) }

    // Format raw month e.g. "2026-05" into "May 2026"
    val displayMonth = remember(rawMonth) {
        try {
            val parser = SimpleDateFormat("yyyy-MM", Locale.US)
            val formatter = SimpleDateFormat("MMMM yyyy", Locale.US)
            val date = parser.parse(rawMonth)
            formatter.format(date ?: Date()).uppercase()
        } catch (e: Exception) {
            rawMonth.uppercase()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBG
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Content
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        // Month Toggle Container
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.offset(x = (-8).dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.prevMonth() },
                                modifier = Modifier.size(32.dp).testTag("prev_month_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Previous Month",
                                    tint = AccentPurple
                                )
                            }
                            Text(
                                text = displayMonth,
                                color = AccentPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            IconButton(
                                onClick = { viewModel.nextMonth() },
                                modifier = Modifier.size(32.dp).testTag("next_month_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next Month",
                                    tint = AccentPurple
                                )
                            }
                        }
                        
                        Text(
                            text = "Hello, $userName",
                            color = TextLight,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    // Profile Icon mockup
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DarkCard)
                            .border(1.dp, DarkBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = TextSub,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Remaining Budget Card
            item {
                RemainingBudgetCard(
                    viewModel = viewModel,
                    summary = summary
                )
            }

            // Clickable Inspect All Transactions Button/Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Track in Datadog monitor
                            DatadogMonitor.trackClick(
                                name = "view_all_transactions_card_clicked",
                                attributes = mapOf(
                                    "total_transactions" to summary.transactions.size,
                                    "selected_month" to rawMonth
                                )
                            )
                            showAllTransactionsModal = true
                        }
                        .testTag("show_all_transactions_card"),
                    colors = CardDefaults.cardColors(containerColor = DarkCategory),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("💸", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "Inspect All Transactions",
                                    color = TextLight,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "See all ${summary.transactions.size} ledger entries for this month",
                                    color = TextSub,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "View All Transactions",
                            tint = AccentPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Category Breakdown Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categories",
                        color = TextLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNavigateToReports() }
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "View Reports",
                            color = AccentPurple,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "View Trend Reports",
                            tint = AccentPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Dynamically Render Category Elements
            if (summary.categoryReports.isEmpty()) {
                item {
                    EmptyCategoryState()
                }
            } else {
                items(
                    items = summary.categoryReports,
                    key = { it.category }
                ) { report ->
                    CategoryRowItem(
                        report = report,
                        viewModel = viewModel,
                        onClick = {
                            // Track in Datadog monitor
                            DatadogMonitor.trackClick(
                                name = "view_category_transactions_clicked",
                                attributes = mapOf(
                                    "category" to report.category,
                                    "transaction_count" to report.transactionCount
                                )
                            )
                            selectedCategoryForModal = report.category
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Overlays for viewing transactions on click on home page
        if (showAllTransactionsModal) {
            TransactionsListDialog(
                title = "All Monthly Transactions",
                transactions = summary.transactions,
                viewModel = viewModel,
                onDismiss = { showAllTransactionsModal = false }
            )
        }

        selectedCategoryForModal?.let { categoryName ->
            val filteredTransactions = remember(categoryName, summary.transactions) {
                summary.transactions.filter { it.category == categoryName }
            }
            TransactionsListDialog(
                title = "Category: $categoryName",
                transactions = filteredTransactions,
                viewModel = viewModel,
                onDismiss = { selectedCategoryForModal = null }
            )
        }
    }
}

@Composable
fun RemainingBudgetCard(
    viewModel: BudgetViewModel,
    summary: MonthBudgetSummary
) {
    val remainingAmount = summary.budgetLimit - summary.totalSpent
    val spentFraction = if (summary.budgetLimit > 0) (summary.totalSpent / summary.budgetLimit).toFloat() else 0f
    val remainingPercent = if (summary.budgetLimit > 0) {
        ((summary.budgetLimit - summary.totalSpent) / summary.budgetLimit * 100).toInt()
    } else {
        0
    }

    val fractionLeft = (1f - spentFraction).coerceIn(0f, 1f)
    val pctLeftText = if (remainingAmount < 0) "OVER LIMIT" else "${(fractionLeft * 100).toInt()}% LEFT"

    val isOverBudget = remainingAmount < 0
    val badgeBg = if (isOverBudget) Color(0xFF601410) else AccentPurpleContainer
    val badgeText = if (isOverBudget) Color(0xFFF2B8B5) else AccentPurpleOnContainer

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(28.dp))
            .background(DarkCard, RoundedCornerShape(28.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(28.dp))
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // First Row: Header and Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Remaining Budget",
                        color = TextSub,
                        fontSize = 14.sp
                    )
                    
                    // Styled Decimals ($1,420.50)
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = viewModel.formatMoneyCompact(remainingAmount),
                            color = TextLight,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Light
                        )
                        Text(
                            text = viewModel.getMoneyDecimalString(remainingAmount),
                            color = AccentPurple,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                // % LEFT Pill badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(badgeBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = pctLeftText,
                        color = badgeText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Spent Indicator Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(DarkBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fractionLeft)
                        .clip(CircleShape)
                        .background(AccentPurple)
                )
            }

            // Spent vs Total limits labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spent: ${viewModel.formatMoney(summary.totalSpent)}",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Total: ${viewModel.formatMoney(summary.budgetLimit)}",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CategoryRowItem(
    report: com.example.viewmodel.CategoryReport,
    viewModel: BudgetViewModel,
    onClick: () -> Unit = {}
) {
    val categoryMetas by viewModel.categoryMetas.collectAsState()
    val meta = remember(report.category, categoryMetas) {
        viewModel.getCategoryMeta(report.category)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCategory)
            .clickable { onClick() }
            .padding(16.dp)
            .testTag("category_row_${meta.id.replace(" ", "_")}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon Container
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(meta.bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = meta.icon,
                contentDescription = meta.displayName,
                tint = meta.iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        // Details Column
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meta.displayName,
                    color = TextLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = viewModel.formatMoney(report.totalSpent),
                    color = TextLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${report.transactionCount} Transactions",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = String.format(Locale.US, "%.1f%% spent", report.percentage),
                    color = AccentPurple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EmptyCategoryState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No Expenses Yet",
                color = TextLight,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Tap the '+' button in the navigation bar to add your first transaction.",
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun TransactionsListDialog(
    title: String,
    transactions: List<com.example.data.Transaction>,
    viewModel: com.example.viewmodel.BudgetViewModel,
    onDismiss: () -> Unit
) {
    val categoryMetas by viewModel.categoryMetas.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.75f)
                .border(1.dp, DarkBorder, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkBG),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with Close option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            color = TextLight,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val spent = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                        val income = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                        Text(
                            text = "Outflow: ${viewModel.formatMoney(spent)} • Inflow: ${viewModel.formatMoney(income)}",
                            color = TextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkCategory)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss transactions list filter",
                            tint = TextLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = DarkBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                // Scrollable List of Custom Transactions
                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recorded transactions for this period.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(transactions) { transaction ->
                            val meta = remember(transaction.category, categoryMetas) {
                                viewModel.getCategoryMeta(transaction.category)
                            }
                            val dateStr = remember(transaction.dateMillis) {
                                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.US)
                                sdf.format(Date(transaction.dateMillis))
                            }
                            val isExpense = transaction.type == "EXPENSE"
                            val moneyColor = if (isExpense) TextLight else Color(0xFF81C784)
                            val prefixSign = if (isExpense) "-" else "+"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DarkCategory)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Category Avatar Container
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(meta.bgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = meta.icon,
                                        contentDescription = null,
                                        tint = meta.iconColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Details Column
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = transaction.title,
                                        color = TextLight,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$dateStr • ${meta.displayName}",
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                }

                                // Money Label and deletion trigger
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "$prefixSign${viewModel.formatMoney(transaction.amount)}",
                                        color = moneyColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteTransaction(transaction)
                                            DatadogMonitor.trackClick(
                                                name = "delete_transaction_from_home_dialog",
                                                attributes = mapOf("id" to transaction.id, "category" to transaction.category)
                                            )
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(DarkBG)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete custom transaction",
                                            tint = Color(0xFFE57373),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
