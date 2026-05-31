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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.components.CategoryHelpers
import com.example.ui.theme.*
import com.example.viewmodel.BudgetViewModel
import com.example.viewmodel.MonthBudgetSummary
import java.text.SimpleDateFormat
import java.util.*

data class GroupedLedger(
    val label: String,
    val spentSum: Double,
    val incomeSum: Double,
    val transactions: List<Transaction>,
    val rawKey: String = ""
)

data class CalendarDayInfo(
    val dayNumber: Int,
    val dateKey: String,
    val spentSum: Double,
    val incomeSum: Double = 0.0,
    val hasTransactions: Boolean
)

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.currentMonthBudgetState.collectAsState()
    val rawMonth by viewModel.selectedMonth.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    
    var isEditingBudget by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf("") }
    
    // Mode to list expenses: DAILY, MONTHLY, YEARLY
    var ledgerTab by remember { mutableStateOf("DAILY") }

    // Selected filters for drilling down/targeting
    var selectedDayFilter by remember(rawMonth) { mutableStateOf<Int?>(null) }
    var selectedYearFilterForMonthTab by remember { mutableStateOf<String?>(null) }

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

    // Parse year and month from rawMonth ("yyyy-MM")
    val calYearMonth = remember(rawMonth) {
        val parts = rawMonth.split("-")
        if (parts.size == 2) {
            val y = parts[0].toIntOrNull() ?: 2026
            val m = parts[1].toIntOrNull() ?: 5
            Pair(y, m)
        } else {
            Pair(2026, 5)
        }
    }
    val calYear = calYearMonth.first
    val calMonth = calYearMonth.second

    // Calculate calendar day info mapping
    val calendarDays = remember(calYear, calMonth, allTransactions) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, calYear)
        cal.set(Calendar.MONTH, calMonth - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, ..., 7 = Saturday
        
        val leadingEmptyDays = firstDayOfWeek - 1
        
        val sdfDayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val daysList = mutableListOf<CalendarDayInfo>()
        
        // Add empty cells for column alignment
        for (i in 0 until leadingEmptyDays) {
            daysList.add(CalendarDayInfo(dayNumber = 0, dateKey = "", spentSum = 0.0, hasTransactions = false))
        }
        
        // Add actual days
        for (d in 1..daysInMonth) {
            val targetCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, calYear)
                set(Calendar.MONTH, calMonth - 1)
                set(Calendar.DAY_OF_MONTH, d)
            }
            val dateKey = sdfDayKey.format(targetCal.time)
            
            val dayTrans = allTransactions.filter {
                try {
                    sdfDayKey.format(Date(it.dateMillis)) == dateKey
                } catch (e: Exception) {
                    false
                }
            }
            
            val spentSum = dayTrans.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val incomeSum = dayTrans.filter { it.type == "INCOME" }.sumOf { it.amount }
            
            daysList.add(
                CalendarDayInfo(
                    dayNumber = d,
                    dateKey = dateKey,
                    spentSum = spentSum,
                    incomeSum = incomeSum,
                    hasTransactions = dayTrans.isNotEmpty()
                )
            )
        }
        daysList
    }

    // Dynamic years found in all user transactions
    val availableYears = remember(allTransactions) {
        val sdfYear = SimpleDateFormat("yyyy", Locale.getDefault())
        val years = allTransactions.map {
            try {
                sdfYear.format(Date(it.dateMillis))
            } catch (e: Exception) {
                ""
            }
        }.filter { it.isNotBlank() }.toSet().sortedDescending()
        
        listOf("All Years") + years
    }

    // Prepare grouped ledger items with math summaries
    val groupedLedgerItems = remember(allTransactions, rawMonth, ledgerTab, selectedDayFilter, selectedYearFilterForMonthTab) {
        val sdfMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val sdfDayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfYearKey = SimpleDateFormat("yyyy", Locale.getDefault())
        
        val sortedTransactions = allTransactions.sortedByDescending { it.dateMillis }

        when (ledgerTab) {
            "DAILY" -> {
                // Filter only for selected month
                val monthlyTrans = sortedTransactions.filter {
                    try {
                        sdfMonth.format(Date(it.dateMillis)) == rawMonth
                    } catch (e: Exception) {
                        false
                    }
                }
                
                // Secondary filter: specific day of week/month selected from grid calendar
                val filteredTrans = if (selectedDayFilter == null) {
                    monthlyTrans
                } else {
                    monthlyTrans.filter {
                        val cal = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
                        cal.get(Calendar.DAY_OF_MONTH) == selectedDayFilter
                    }
                }

                val grouped = filteredTrans.groupBy {
                    try {
                        sdfDayKey.format(Date(it.dateMillis))
                    } catch (e: Exception) {
                        ""
                    }
                }
                
                grouped.map { (dateKey, list) ->
                    val displayLabel = try {
                        val dateObj = sdfDayKey.parse(dateKey)
                        val calNow = Calendar.getInstance()
                        val calTrx = Calendar.getInstance().apply { time = dateObj ?: Date() }
                        
                        if (calNow.get(Calendar.YEAR) == calTrx.get(Calendar.YEAR) &&
                            calNow.get(Calendar.DAY_OF_YEAR) == calTrx.get(Calendar.DAY_OF_YEAR)) {
                            "Today"
                        } else {
                            calNow.add(Calendar.DAY_OF_YEAR, -1)
                            if (calNow.get(Calendar.YEAR) == calTrx.get(Calendar.YEAR) &&
                                calNow.get(Calendar.DAY_OF_YEAR) == calTrx.get(Calendar.DAY_OF_YEAR)) {
                                "Yesterday"
                            } else {
                                SimpleDateFormat("dd MMMM, yyyy", Locale.US).format(dateObj ?: Date())
                            }
                        }
                    } catch (e: Exception) {
                        dateKey
                    }
                    
                    val spentSum = list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                    val incomeSum = list.filter { it.type == "INCOME" }.sumOf { it.amount }
                    
                    GroupedLedger(
                        label = displayLabel,
                        spentSum = spentSum,
                        incomeSum = incomeSum,
                        transactions = list,
                        rawKey = dateKey
                    )
                }
            }
            "MONTHLY" -> {
                val yearFilteredTrans = if (selectedYearFilterForMonthTab == null || selectedYearFilterForMonthTab == "All Years") {
                    sortedTransactions
                } else {
                    sortedTransactions.filter {
                        try {
                            sdfYearKey.format(Date(it.dateMillis)) == selectedYearFilterForMonthTab
                        } catch (e: Exception) {
                            false
                        }
                    }
                }

                val grouped = yearFilteredTrans.groupBy {
                    try {
                        sdfMonth.format(Date(it.dateMillis))
                    } catch (e: Exception) {
                        ""
                    }
                }
                
                grouped.map { (monthKey, list) ->
                    val displayLabel = try {
                        val dateObj = sdfMonth.parse(monthKey)
                        SimpleDateFormat("MMMM yyyy", Locale.US).format(dateObj ?: Date()).uppercase()
                    } catch (e: Exception) {
                        monthKey
                    }
                    
                    val spentSum = list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                    val incomeSum = list.filter { it.type == "INCOME" }.sumOf { it.amount }
                    
                    GroupedLedger(
                        label = displayLabel,
                        spentSum = spentSum,
                        incomeSum = incomeSum,
                        transactions = list,
                        rawKey = monthKey
                    )
                }
            }
            "YEARLY" -> {
                val grouped = sortedTransactions.groupBy {
                    try {
                        sdfYearKey.format(Date(it.dateMillis))
                    } catch (e: Exception) {
                        ""
                    }
                }
                
                grouped.map { (yearKey, list) ->
                    GroupedLedger(
                        label = "YEAR $yearKey",
                        spentSum = list.filter { it.type == "EXPENSE" }.sumOf { it.amount },
                        incomeSum = list.filter { it.type == "INCOME" }.sumOf { it.amount },
                        transactions = list,
                        rawKey = yearKey
                    )
                }
            }
            else -> emptyList()
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
                .padding(horizontal = 16.dp)
                .testTag("budget_lazy_column"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.offset(x = (-8).dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.prevMonth() },
                                modifier = Modifier.size(32.dp).testTag("budget_prev_month")
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
                                modifier = Modifier.size(32.dp).testTag("budget_next_month")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next Month",
                                    tint = AccentPurple
                                )
                            }
                        }
                        
                        Text(
                            text = "Budget & History",
                            color = TextLight,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }
            }

            // Budget adjustment action card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Monthly Outflow Allowance", color = TextMuted, fontSize = 12.sp)
                                Text(
                                    text = viewModel.formatMoney(summary.budgetLimit),
                                    color = AccentPurple,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    budgetInput = summary.budgetLimit.toInt().toString()
                                    isEditingBudget = !isEditingBudget
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isEditingBudget) DarkBorder else AccentPurpleContainer,
                                    contentColor = if (isEditingBudget) TextLight else AccentPurpleOnContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("edit_budget_btn")
                            ) {
                                Icon(
                                    imageVector = if (isEditingBudget) Icons.Default.Close else Icons.Default.Edit,
                                    contentDescription = "Edit Budget",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isEditingBudget) "Cancel" else "Configure", fontSize = 12.sp)
                            }
                        }

                        // Editing expansion
                        AnimatedVisibility(
                            visible = isEditingBudget,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Enter target spend limits:", color = TextLight, fontSize = 13.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = budgetInput,
                                        onValueChange = { budgetInput = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentPurple,
                                            unfocusedBorderColor = DarkBorder,
                                            focusedTextColor = TextLight,
                                            unfocusedTextColor = TextLight,
                                            focusedContainerColor = DarkCategory,
                                            unfocusedContainerColor = DarkCategory
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f).testTag("budget_value_input"),
                                        singleLine = true
                                    )

                                    Button(
                                        onClick = {
                                            val limitVal = budgetInput.toDoubleOrNull()
                                            if (limitVal != null && limitVal > 0) {
                                                viewModel.updateMonthlyBudgetLimit(limitVal)
                                                isEditingBudget = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AccentPurple,
                                            contentColor = AccentPurpleOnContainer
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("save_budget_btn")
                                    ) {
                                        Text("Save")
                                    }
                                }
                            }
                        }

                        // Daily warning indicator
                        HorizontalDivider(color = DarkBorder, thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF601410).copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFF2B8B5),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Daily Safety Limit Cap",
                                    color = TextLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "To stay balanced, limit expenditures to ${viewModel.formatMoney(summary.dailyBudgetLimit)} per day. (Recalculated daily relative to your active remaining balance).",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Heatmap calendar view for selected day/month expenses
            if (ledgerTab == "DAILY") {
                item {
                    MonthCalendarView(
                        days = calendarDays,
                        selectedDay = selectedDayFilter,
                        onDaySelect = { selectedDayFilter = it },
                        currencySymbol = currencySymbol,
                        modifier = Modifier.testTag("month_calendar_view")
                    )
                }
            }

            // Ledger title + View Mode
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Transaction Ledger",
                        color = TextLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    // Capsule options for daily, monthly, yearly
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkCategory)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tabs = listOf(
                            Triple("DAILY", "Daily", "📅"),
                            Triple("MONTHLY", "Monthly", "🗓️"),
                            Triple("YEARLY", "Yearly", "⏳")
                        )
                        tabs.forEach { (mode, label, icon) ->
                            val isSelected = ledgerTab == mode
                            val bThemeColor = if (isSelected) AccentPurple else Color.Transparent
                            val textThemeColor = if (isSelected) AccentPurpleOnContainer else TextMuted

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bThemeColor)
                                    .clickable { ledgerTab = mode }
                                    .padding(vertical = 10.dp)
                                    .testTag("ledger_tab_$mode"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(icon, fontSize = 14.sp)
                                    Text(
                                        text = label,
                                        color = textThemeColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Year selection filter in Monthly View
            if (ledgerTab == "MONTHLY") {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filter by Year:",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (selectedYearFilterForMonthTab != null) {
                                TextButton(
                                    onClick = { selectedYearFilterForMonthTab = null },
                                    colors = ButtonDefaults.textButtonColors(contentColor = AccentPurple),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Show All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(availableYears) { yr ->
                                val isSelected = (selectedYearFilterForMonthTab ?: "All Years") == yr
                                val chipBg = if (isSelected) AccentPurple else DarkCategory
                                val chipTextCol = if (isSelected) AccentPurpleOnContainer else TextLight
                                val chipBorderCol = if (isSelected) AccentPurple else DarkBorder
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(chipBg)
                                        .border(1.dp, chipBorderCol, RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedYearFilterForMonthTab = if (yr == "All Years") null else yr
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                        .testTag("filter_year_chip_$yr"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(yr, color = chipTextCol, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Dynamic filter badge/reminders
            if (ledgerTab == "DAILY" && selectedDayFilter != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF322330))
                            .border(1.dp, Color(0xFF6B3350), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🗓️", fontSize = 14.sp)
                            Text(
                                text = "Filtering Day $selectedDayFilter in $displayMonth",
                                color = TextLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "Clear Filter",
                            color = AccentPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { selectedDayFilter = null }
                                .testTag("clear_day_filter_btn")
                        )
                    }
                }
            }

            // Ledger Transactions mapping grouped by the interval
            if (groupedLedgerItems.isEmpty()) {
                item {
                    EmptyLedgerState()
                }
            } else {
                groupedLedgerItems.forEach { group ->
                    // Group header
                    val isTappable = ledgerTab == "MONTHLY" || ledgerTab == "YEARLY"
                    item(key = "group_${ledgerTab}_${group.label}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isTappable) {
                                        Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(DarkCategory)
                                            .clickable {
                                                if (ledgerTab == "MONTHLY") {
                                                    viewModel.selectMonth(group.rawKey)
                                                    ledgerTab = "DAILY"
                                                } else if (ledgerTab == "YEARLY") {
                                                    selectedYearFilterForMonthTab = group.rawKey
                                                    ledgerTab = "MONTHLY"
                                                }
                                            }
                                            .padding(14.dp)
                                    } else {
                                        Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp, end = 4.dp)
                                    }
                                )
                                .testTag("group_header_${group.label}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.label,
                                    color = AccentPurple,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                if (isTappable) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Drill Down",
                                        tint = AccentPurple.copy(alpha = 0.6f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (group.incomeSum > 0) {
                                    Text(
                                        text = "+${viewModel.formatMoney(group.incomeSum)}",
                                        color = Color(0xFF81C784),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (group.spentSum > 0) {
                                    Text(
                                        text = "-${viewModel.formatMoney(group.spentSum)}",
                                        color = TextLight.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Group items
                    items(
                        items = group.transactions,
                        key = { "${ledgerTab}_${group.label}_${it.id}" }
                    ) { transaction ->
                        LedgerItemRow(
                            transaction = transaction,
                            viewModel = viewModel,
                            onDelete = { viewModel.deleteTransaction(transaction) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun LedgerItemRow(
    transaction: Transaction,
    viewModel: BudgetViewModel,
    onDelete: () -> Unit
) {
    val categoryMetas by viewModel.categoryMetas.collectAsState()
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
            .padding(14.dp)
            .testTag("ledger_item_${transaction.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Colorized category avatar container
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(meta.bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = meta.icon,
                contentDescription = null,
                tint = meta.iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        // Title and timestamp
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                color = TextLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = "$dateStr • ${meta.displayName}",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Amount, style type, and hard delete triggers
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "$prefixSign${viewModel.formatMoney(transaction.amount)}",
                color = moneyColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkBG)
                    .testTag("delete_trx_${transaction.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete transaction",
                    tint = Color(0xFFE57373),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyLedgerState() {
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
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = "Ledger is Empty",
                color = TextLight,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "No recorded items were found in the current period. Use the navigation '+' trigger to add transactions.",
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun MonthCalendarView(
    days: List<CalendarDayInfo>,
    selectedDay: Int?,
    onDaySelect: (Int?) -> Unit,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row with Reset Filter option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅",
                        fontSize = 18.sp
                    )
                    Column {
                        Text(
                            text = "Monthly Expense Calendar",
                            color = TextLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap a day to filter ledger",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                
                if (selectedDay != null) {
                    TextButton(
                        onClick = { onDaySelect(null) },
                        colors = ButtonDefaults.textButtonColors(contentColor = AccentPurple),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("reset_calendar_filter_btn")
                    ) {
                        Text("Clear Filter", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)

            // Week Days Names
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val weekDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                weekDays.forEach { d ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = d,
                            color = AccentPurple.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Weeks grid
            val weeks = remember(days) { days.chunked(7) }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                weeks.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (cellIndex in 0 until 7) {
                            val dayInfo = week.getOrNull(cellIndex)
                            if (dayInfo == null || dayInfo.dayNumber == 0) {
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                val isSelected = selectedDay == dayInfo.dayNumber
                                
                                // Sleek dynamic category styling for custom design
                                val baseBgColor = when {
                                    isSelected -> AccentPurple
                                    dayInfo.spentSum > 0 -> Color(0xFF3F2731) // Heatmap highlight for spent
                                    dayInfo.incomeSum > 0 -> Color(0xFF1E3525) // soft green highlight for income
                                    else -> DarkCategory
                                }
                                
                                val textColor = if (isSelected) AccentPurpleOnContainer else TextLight
                                val borderColor = if (isSelected) AccentPurple else if (dayInfo.spentSum > 0) Color(0xFF6B3350) else DarkBorder
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(baseBgColor)
                                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                                        .clickable {
                                            if (isSelected) {
                                                onDaySelect(null)
                                            } else {
                                                onDaySelect(dayInfo.dayNumber)
                                            }
                                        }
                                        .testTag("calendar_day_${dayInfo.dayNumber}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(2.dp)
                                    ) {
                                        Text(
                                            text = dayInfo.dayNumber.toString(),
                                            color = textColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (dayInfo.spentSum > 0) {
                                            Text(
                                                text = "${currencySymbol}${dayInfo.spentSum.toInt()}",
                                                color = if (isSelected) AccentPurpleOnContainer else Color(0xFFF2B8B5),
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        } else if (dayInfo.incomeSum > 0) {
                                            Text(
                                                text = "+${currencySymbol}${dayInfo.incomeSum.toInt()}",
                                                color = if (isSelected) AccentPurpleOnContainer else Color(0xFF81C784),
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
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
}

