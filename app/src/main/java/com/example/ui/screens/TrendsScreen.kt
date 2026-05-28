package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun TrendsScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.currentMonthBudgetState.collectAsState()
    val rawMonth by viewModel.selectedMonth.collectAsState()

    var activeVisualizerTab by remember { mutableStateOf(0) } // 0 = Category Pie, 1 = Daily Bar

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
                                modifier = Modifier.size(32.dp).testTag("trends_prev_month")
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
                                modifier = Modifier.size(32.dp).testTag("trends_next_month")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next Month",
                                    tint = AccentPurple
                                )
                            }
                        }
                        
                        Text(
                            text = "Financial Reports",
                            color = TextLight,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }
            }

            // Quick Metrics Row (Income vs Expense breakdown)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Month Inflow",
                        amount = viewModel.formatMoney(summary.totalIncome),
                        accentColor = Color(0xFFC2E7FF),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Month Outflow",
                        amount = viewModel.formatMoney(summary.totalSpent),
                        accentColor = AccentPurple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Selector tabs: Breakdown / Daily Trend
            item {
                TabRow(
                    selectedTabIndex = activeVisualizerTab,
                    containerColor = DarkCategory,
                    contentColor = TextLight,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeVisualizerTab]),
                            color = AccentPurple
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = activeVisualizerTab == 0,
                        onClick = { activeVisualizerTab = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Category")
                            }
                        },
                        unselectedContentColor = TextMuted,
                        selectedContentColor = AccentPurple
                    )
                    Tab(
                        selected = activeVisualizerTab == 1,
                        onClick = { activeVisualizerTab = 1 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Daily Trend")
                            }
                        },
                        unselectedContentColor = TextMuted,
                        selectedContentColor = AccentPurple
                    )
                }
            }

            // Visualizer Container Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkCard, RoundedCornerShape(24.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    if (summary.transactions.isEmpty()) {
                        EmptyVisualizerState()
                    } else {
                        when (activeVisualizerTab) {
                            0 -> RingChartVisualizer(summary = summary, viewModel = viewModel)
                            1 -> DailyBarChartVisualizer(summary = summary, viewModel = viewModel)
                        }
                    }
                }
            }

            // Explaining breakdown items list for categories
            if (activeVisualizerTab == 0 && summary.categoryReports.isNotEmpty()) {
                item {
                    Text(
                        text = "Distribution",
                        color = TextLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                    )
                }
                
                itemsIndexed(summary.categoryReports) { index, report ->
                    CategoryPercentItem(report = report, index = index, viewModel = viewModel)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    amount: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, color = TextMuted, fontSize = 12.sp)
            Text(amount, color = accentColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RingChartVisualizer(
    summary: MonthBudgetSummary,
    viewModel: BudgetViewModel
) {
    val reports = summary.categoryReports
    val totalExpense = summary.totalSpent

    if (totalExpense <= 0) {
        EmptyVisualizerState()
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Expense Distribution",
            color = TextSub,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .size(170.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                for (report in reports) {
                    val sweepAngle = ((report.totalSpent / totalExpense) * 360f).toFloat()
                    val meta = viewModel.getCategoryMeta(report.category)
                    
                    drawArc(
                        color = meta.bgColor,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        size = Size(size.width, size.height),
                        style = Stroke(width = 24.dp.toPx())
                    )
                    startAngle += sweepAngle
                }
            }

            // Centered metrics hole
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total Spent",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = viewModel.formatMoneyCompact(totalExpense),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = viewModel.getMoneyDecimalString(totalExpense),
                    color = AccentPurple,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun DailyBarChartVisualizer(
    summary: MonthBudgetSummary,
    viewModel: BudgetViewModel
) {
    val calendar = Calendar.getInstance()
    // Find the max number of days in selectedMonth
    val maxDays = remember(summary.yearMonth) {
        try {
            val parser = SimpleDateFormat("yyyy-MM", Locale.US)
            val date = parser.parse(summary.yearMonth) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        } catch (e: Exception) {
            30
        }
    }

    // Set sample values for daily expenses
    val dailySpends = summary.dailySpendList
    val maxSpent = remember(dailySpends) {
        (dailySpends.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily Outflow Wave",
                color = TextSub,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2B2930))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Safety Max: ${viewModel.formatMoney(summary.dailyBudgetLimit)} / Day",
                    color = Color(0xFFF2B8B5),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Horizontal Graph Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(vertical = 8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val bottomLabelHeight = 20.dp.toPx()
                val graphHeight = canvasHeight - bottomLabelHeight

                // Draw background horizontal alignment helpers (3 helper lines)
                val helperLines = 4
                for (i in 0 until helperLines) {
                    val y = (graphHeight / (helperLines - 1)) * i
                    drawLine(
                        color = DarkBorder.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw Daily Budget Safety Margin Threshold line
                val budgetLimitFraction = (summary.dailyBudgetLimit / maxSpent).coerceIn(0.0, 1.0)
                val lineY = (graphHeight - (graphHeight * budgetLimitFraction)).toFloat()
                drawLine(
                    color = Color(0xFFF2B8B5),
                    start = Offset(0f, lineY),
                    end = Offset(canvasWidth, lineY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Render dynamic vertical bar capsules
                val barSpacing = canvasWidth / maxDays
                val barWidth = (barSpacing * 0.7f).coerceIn(4f, 20f)

                for (day in 1..maxDays) {
                    val spentOnDay = dailySpends[day] ?: 0.0
                    val fractionValue = (spentOnDay / maxSpent).coerceIn(0.0, 1.0)
                    val barHeight = (graphHeight * fractionValue).toFloat()

                    val barX = ((day - 1) * barSpacing) + ((barSpacing - barWidth) / 2)
                    val barY = graphHeight - barHeight

                    // Colors vary depending on if they exceed daily budget safety limit
                    val barColor = if (spentOnDay > summary.dailyBudgetLimit) {
                        Color(0xFFF2B8B5) // Warn Red style
                    } else if (spentOnDay > 0) {
                        AccentPurple
                    } else {
                        DarkBorder.copy(alpha = 0.3f)
                    }

                    // Draw capsule
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(barX, if (spentOnDay > 0) barY else graphHeight - 4.dp.toPx()),
                        size = Size(barWidth, if (spentOnDay > 0) barHeight.coerceAtLeast(4.dp.toPx()) else 4.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )
                }
            }
        }

        // Bottom horizontal label guide
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Day 1", color = TextMuted, fontSize = 10.sp)
            Text("Day ${maxDays / 2}", color = TextMuted, fontSize = 10.sp)
            Text("Day $maxDays", color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
fun CategoryPercentItem(
    report: com.example.viewmodel.CategoryReport,
    index: Int,
    viewModel: BudgetViewModel
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
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Simple Index badge
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(DarkBG),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${index + 1}",
                color = AccentPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Visual attributes
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

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meta.displayName,
                    color = TextLight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = viewModel.formatMoney(report.totalSpent),
                    color = TextLight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Small horizontal bar displaying relative category weight
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(DarkBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((report.percentage / 100.0).toFloat().coerceIn(0f, 1f))
                            .clip(CircleShape)
                            .background(meta.bgColor)
                    )
                }
                Text(
                    text = String.format(Locale.US, "%.1f%%", report.percentage),
                    color = TextSub,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyVisualizerState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(36.dp)
        )
        Text(
            text = "Insufficient Data",
            color = TextLight,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "No recorded expenses matched this month to generate analytical visualizations.",
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
