package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.viewmodel.BudgetViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    viewModel: BudgetViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current

    val categories by viewModel.categoryMetas.collectAsState()

    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"
    var selectedCategory by remember(categories) {
        mutableStateOf(if (categories.isNotEmpty()) categories[0].id else "Other")
    }
    var notes by remember { mutableStateOf("") }

    // Date State
    var calendar = remember { Calendar.getInstance() }
    var selectedDateMillis by remember { mutableStateOf(calendar.timeInMillis) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDateMillis = it
                        }
                        showDatePicker = false
                    },
                    modifier = Modifier.testTag("date_picker_confirm_btn")
                ) {
                    Text("OK", color = AccentPurple)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    modifier = Modifier.testTag("date_picker_cancel_btn")
                ) {
                    Text("Cancel", color = AccentPurple)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = DarkCard
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = DarkCard,
                    titleContentColor = TextLight,
                    headlineContentColor = TextLight,
                    weekdayContentColor = TextMuted,
                    subheadContentColor = TextMuted,
                    navigationContentColor = AccentPurple,
                    yearContentColor = TextSub,
                    selectedYearContentColor = AccentPurpleOnContainer,
                    selectedYearContainerColor = AccentPurple,
                    dayContentColor = TextLight,
                    selectedDayContentColor = AccentPurpleOnContainer,
                    selectedDayContainerColor = AccentPurple,
                    todayContentColor = AccentPurple,
                    todayDateBorderColor = AccentPurple
                )
            )
        }
    }

    val dateDisplayStr = remember(selectedDateMillis) {
        val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.US)
        sdf.format(Date(selectedDateMillis))
    }

    var showError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(DarkCard)
                .border(1.dp, DarkBorder, RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header Segment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Transaction",
                        color = TextLight,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_trx_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                // Expense / Income Segment Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkBG)
                        .padding(4.dp)
                ) {
                    val types = listOf("EXPENSE", "INCOME")
                    for (tp in types) {
                        val isSelected = tp == selectedType
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AccentPurple else Color.Transparent)
                                .clickable { selectedType = tp }
                                .padding(vertical = 10.dp)
                                .testTag("type_toggle_$tp"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (tp == "EXPENSE") "Outflow (Expense)" else "Inflow (Income)",
                                color = if (isSelected) AccentPurpleOnContainer else TextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What did you spend on?") },
                    placeholder = { Text("e.g. Weekly groceries") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight,
                        focusedContainerColor = DarkCategory,
                        unfocusedContainerColor = DarkCategory,
                        focusedLabelColor = AccentPurple,
                        unfocusedLabelColor = TextMuted,
                        focusedPlaceholderColor = TextMuted,
                        unfocusedPlaceholderColor = TextMuted
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_trx_title"),
                    singleLine = true
                )

                // Amount Input
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("How much?") },
                    placeholder = { Text("e.g. 15.50") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight,
                        focusedContainerColor = DarkCategory,
                        unfocusedContainerColor = DarkCategory,
                        focusedLabelColor = AccentPurple,
                        unfocusedLabelColor = TextMuted,
                        focusedPlaceholderColor = TextMuted,
                        unfocusedPlaceholderColor = TextMuted
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_trx_amount"),
                    singleLine = true
                )

                // Interactive Calendar Trigger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCategory)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            showDatePicker = true
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("add_trx_date_trigger"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Date Selector",
                        tint = AccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text("Transaction Date", color = TextMuted, fontSize = 11.sp)
                        Text(dateDisplayStr, color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Quick Past-Date Presets for Relative/Store Past Expenses
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Store Past Expense Presets:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val presets = listOf(
                            Triple("Today", 0, "📅"),
                            Triple("Yesterday", -1, "🕒"),
                            Triple("Last Month", -30, "🗓️"),
                            Triple("Last Year", -365, "⏳")
                        )
                        presets.forEach { (label, daysOffset, icon) ->
                            val isSelected = remember(selectedDateMillis, daysOffset) {
                                val targetCal = Calendar.getInstance().apply {
                                    if (daysOffset != 0) {
                                        add(Calendar.DAY_OF_YEAR, daysOffset)
                                    }
                                }
                                val curCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                                targetCal.get(Calendar.YEAR) == curCal.get(Calendar.YEAR) &&
                                        targetCal.get(Calendar.DAY_OF_YEAR) == curCal.get(Calendar.DAY_OF_YEAR)
                            }

                            val chipBg = if (isSelected) AccentPurple else DarkCategory
                            val chipText = if (isSelected) AccentPurpleOnContainer else TextLight
                            val borderCol = if (isSelected) AccentPurple else DarkBorder

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                  .clip(RoundedCornerShape(10.dp))
                                    .background(chipBg)
                                    .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                                    .clickable {
                                        val newCal = Calendar.getInstance()
                                        if (daysOffset != 0) {
                                            newCal.add(Calendar.DAY_OF_YEAR, daysOffset)
                                        }
                                        selectedDateMillis = newCal.timeInMillis
                                    }
                                    .padding(vertical = 8.dp)
                                    .testTag("date_preset_$label"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(icon, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = label,
                                        color = chipText,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "💡 Tip: Tap 'Transaction Date' above to pick any exact past date, month or year manually.",
                        color = TextMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Category Selector Title
                Text(
                    text = "Select Category",
                    color = TextSub,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                )

                // Category Selection Grid layout
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = cat.id == selectedCategory
                        val borderCol = if (isSelected) AccentPurple else Color.Transparent
                        val bgCol = if (isSelected) DarkBG else DarkCategory

                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgCol)
                                .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                                .clickable { selectedCategory = cat.id }
                                .padding(8.dp)
                                .testTag("select_cat_${cat.id.replace(" ", "_")}"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(cat.bgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    tint = cat.iconColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = cat.displayName,
                                color = if (isSelected) AccentPurple else TextLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Error Warning Block
                if (showError) {
                    val errorColor = if (AppColors.isDark) Color(0xFFF2B8B5) else Color(0xFFB3261E)
                    Text(
                        text = errorMsg,
                        color = errorColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().testTag("add_trx_error_msg")
                    )
                }

                // Action Bottom Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val parsedAmount = amountStr.toDoubleOrNull()
                            if (title.isBlank()) {
                                showError = true
                                errorMsg = "Please enter a transaction title."
                            } else if (parsedAmount == null || parsedAmount <= 0) {
                                showError = true
                                errorMsg = "Please enter a valid amount greater than zero."
                            } else {
                                viewModel.addTransaction(
                                    title = title,
                                    amount = parsedAmount,
                                    type = selectedType,
                                    category = selectedCategory,
                                    dateMillis = selectedDateMillis,
                                    notes = notes
                                )
                                onConfirm()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentPurple,
                            contentColor = AccentPurpleOnContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("save_trx_btn")
                    ) {
                        Text("Record")
                    }
                }
            }
        }
    }
}
