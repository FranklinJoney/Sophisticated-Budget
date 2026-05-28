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

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.currentMonthBudgetState.collectAsState()
    val rawMonth by viewModel.selectedMonth.collectAsState()
    
    var isEditingBudget by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf("") }

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

            // Ledger title
            item {
                Text(
                    text = "Transaction Ledger",
                    color = TextLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                )
            }

            // Ledger Transactions mapping
            if (summary.transactions.isEmpty()) {
                item {
                    EmptyLedgerState()
                }
            } else {
                items(
                    items = summary.transactions,
                    key = { it.id }
                ) { transaction ->
                    LedgerItemRow(
                        transaction = transaction,
                        viewModel = viewModel,
                        onDelete = { viewModel.deleteTransaction(transaction) }
                    )
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
    val meta = remember(transaction.category) {
        CategoryHelpers.getMeta(transaction.category)
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
