package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryHelpers
import com.example.ui.components.CategoryMeta
import com.example.ui.theme.*
import com.example.viewmodel.BudgetViewModel

@Composable
fun SettingsScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val themeSetting by viewModel.themeSetting.collectAsState()

    var editingName by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }

    val currencyOptions = listOf("$", "€", "£", "₹", "¥", "₩")

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Database?", color = TextLight) },
            text = { Text("This will clear all transactions and re-seed the default baseline metrics for the current month. Are you sure?", color = TextSub) },
            confirmButton = {
                val warningConfirmColor = if (AppColors.isDark) Color(0xFFF2B8B5) else Color(0xFFB3261E)
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = warningConfirmColor)
                ) {
                    Text("Decline & Clear")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = AccentPurple)
                ) {
                    Text("Keep Safe")
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(24.dp)
        )
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
                .testTag("settings_lazy_column"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Column {
                    Text(
                        text = "PREFERENCES",
                        color = AccentPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Text(
                        text = "Settings Panel",
                        color = TextLight,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            // Profile card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(AccentPurpleContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = AccentPurpleOnContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userName,
                                    color = TextLight,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Active Budget Controller",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Username edit box
                        if (editingName) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    placeholder = { Text("Enter name...") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentPurple,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = TextLight,
                                        unfocusedTextColor = TextLight,
                                        focusedContainerColor = DarkCategory,
                                        unfocusedContainerColor = DarkCategory
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("settings_username_input"),
                                    singleLine = true
                                )
                                Button(
                                    onClick = {
                                        viewModel.updateUserName(nameInput)
                                        editingName = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentPurple,
                                        contentColor = AccentPurpleOnContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("save_username_btn")
                                ) {
                                    Text("Save")
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    nameInput = userName
                                    editingName = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurple),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                modifier = Modifier.fillMaxWidth().testTag("edit_username_btn")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit Custom Username", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Currency Customization Row
            item {
                SettingsGroup(title = "Currency Configuration") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Choose active standard currency format:",
                            color = TextSub,
                            fontSize = 13.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (sym in currencyOptions) {
                                val isSelected = sym == currencySymbol
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) AccentPurpleContainer else DarkCategory)
                                        .border(
                                            1.dp,
                                            if (isSelected) AccentPurple else DarkBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.updateCurrencySymbol(sym) }
                                        .testTag("currency_sym_$sym"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sym,
                                        color = if (isSelected) AccentPurpleOnContainer else TextLight,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Display Theme Configuration Row
            item {
                SettingsGroup(title = "Display Theme Settings") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Choose active aesthetic design theme:",
                            color = TextSub,
                            fontSize = 13.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val options = listOf(
                                Triple("LIGHT", "☀️ Light", "theme_light_btn"),
                                Triple("DARK", "🌙 Dark", "theme_dark_btn"),
                                Triple("SYSTEM", "🌓 System", "theme_system_btn")
                            )
                            for ((setting, label, tag) in options) {
                                val isSelected = setting == themeSetting
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) AccentPurpleContainer else DarkCategory)
                                        .border(
                                            1.dp,
                                            if (isSelected) AccentPurple else DarkBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.updateThemeSetting(setting) }
                                        .testTag(tag),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) AccentPurpleOnContainer else TextLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Custom Categories list section & Dialogs
            item {
                var showCategoryDialog by remember { mutableStateOf(false) }
                var editingCategory by remember { mutableStateOf<CategoryMeta?>(null) }
                
                val categories by viewModel.categoryMetas.collectAsState()

                if (showCategoryDialog || editingCategory != null) {
                    CategoryEditDialog(
                        existingCategory = editingCategory,
                        onDismiss = {
                            showCategoryDialog = false
                            editingCategory = null
                        },
                        onSave = { oldId, displayName, iconName, bgHex, textHex ->
                            if (oldId != null) {
                                viewModel.updateCategory(oldId, displayName, iconName, bgHex, textHex)
                            } else {
                                viewModel.addCategory(displayName, iconName, bgHex, textHex)
                            }
                            showCategoryDialog = false
                            editingCategory = null
                        },
                        onDelete = { categoryId ->
                            viewModel.deleteCategory(categoryId)
                            showCategoryDialog = false
                            editingCategory = null
                        }
                    )
                }

                SettingsGroup(title = "Custom Categories") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Add, edit, or customize display styles (icons & colors) for transaction categories:",
                            color = TextSub,
                            fontSize = 13.sp
                        )

                        val categoryChunks = categories.chunked(2)
                        for (chunk in categoryChunks) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (cat in chunk) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(cat.bgColor)
                                            .clickable { editingCategory = cat }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                            .testTag("manage_cat_${cat.id.replace(" ", "_")}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = cat.icon,
                                                contentDescription = null,
                                                tint = cat.iconColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = cat.displayName,
                                                color = cat.iconColor,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = cat.iconColor.copy(alpha = 0.6f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                                if (chunk.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { showCategoryDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPurple,
                                contentColor = AccentPurpleOnContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("add_custom_category_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Custom Category", fontSize = 13.sp)
                        }
                    }
                }
            }

            // System administration setting triggers
            item {
                SettingsGroup(title = "Core Administration") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminRow(
                            icon = Icons.Default.Refresh,
                            title = "Restore Default Database",
                            description = "Wipes custom ledgers and populates standard seed metrics",
                            onClick = { showResetDialog = true },
                            tag = "reset_database_row"
                        )
                    }
                }
            }

            // Architectural technical credits
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Sophisticated Dark Architecture",
                            color = TextLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Built natively utilizing Android Jetpack Compose, reactive Room SQL persistence, MVVM flows, KSP compilers, and Material 3 design directives.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
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
fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 2.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCard, RoundedCornerShape(20.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun AdminRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val warningIconBg = if (AppColors.isDark) Color(0xFF601410).copy(alpha = 0.5f) else Color(0xFFFFEBEE)
        val warningIconTint = if (AppColors.isDark) Color(0xFFF2B8B5) else Color(0xFFD32F2F)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(warningIconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = warningIconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditDialog(
    existingCategory: CategoryMeta?,
    onDismiss: () -> Unit,
    onSave: (oldId: String?, displayName: String, iconName: String, bgHex: String, textHex: String) -> Unit,
    onDelete: (categoryId: String) -> Unit
) {
    var name by remember { mutableStateOf(existingCategory?.displayName ?: "") }
    var selectedIconName by remember { mutableStateOf(
        CategoryHelpers.ICONS.find { it.icon == existingCategory?.icon }?.id ?: "Category"
    ) }
    
    var selectedColor by remember { mutableStateOf(
        CategoryHelpers.COLORS.find { 
            existingCategory != null && 
            try { Color(android.graphics.Color.parseColor(it.bgHex)) == existingCategory.bgColor } catch (e: Exception) { false }
        } ?: CategoryHelpers.COLORS[0]
    ) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                if (existingCategory != null) "Edit Category" else "Add Custom Category", 
                color = TextLight,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight,
                        focusedContainerColor = DarkCategory,
                        unfocusedContainerColor = DarkCategory,
                        focusedLabelColor = AccentPurple,
                        unfocusedLabelColor = TextMuted
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("cat_edit_name_input"),
                    singleLine = true,
                    placeholder = { Text("e.g., Subscriptions") }
                )

                // Visual live preview container
                Text(
                    text = "Live Preview:",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                val previewBg = try { Color(android.graphics.Color.parseColor(selectedColor.bgHex)) } catch (e: Exception) { CatOtherBg }
                val previewText = try { Color(android.graphics.Color.parseColor(selectedColor.textHex)) } catch (e: Exception) { CatOtherOnBg }
                val previewIcon = CategoryHelpers.getIconByName(selectedIconName)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(previewBg)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(previewText.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = previewIcon,
                                contentDescription = null,
                                tint = previewText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = if (name.isNotBlank()) name else "Category Name",
                            color = previewText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Choose Icon Section
                Text(
                    text = "Choose Visual Icon:",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(CategoryHelpers.ICONS) { iconOpt ->
                        val isSelected = iconOpt.id == selectedIconName
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AccentPurpleContainer else DarkCategory)
                                .border(1.dp, if (isSelected) AccentPurple else DarkBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedIconName = iconOpt.id }
                                .testTag("select_icon_${iconOpt.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconOpt.icon,
                                contentDescription = iconOpt.label,
                                tint = if (isSelected) AccentPurpleOnContainer else TextLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Choose Color Section
                Text(
                    text = "Choose Color Style:",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(CategoryHelpers.COLORS) { colOpt ->
                        val isSelected = colOpt.name == selectedColor.name
                        val bg = try { Color(android.graphics.Color.parseColor(colOpt.bgHex)) } catch (e: Exception) { CatOtherBg }
                        val txt = try { Color(android.graphics.Color.parseColor(colOpt.textHex)) } catch (e: Exception) { CatOtherOnBg }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(bg)
                                .border(1.dp, if (isSelected) AccentPurple else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { selectedColor = colOpt }
                                .testTag("select_color_${colOpt.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = colOpt.name,
                                color = txt,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (existingCategory != null && existingCategory.id != "Other") {
                    val deleteBtnColor = if (AppColors.isDark) Color(0xFFF2B8B5) else Color(0xFFB3261E)
                    TextButton(
                        onClick = { onDelete(existingCategory.id) },
                        colors = ButtonDefaults.textButtonColors(contentColor = deleteBtnColor),
                        modifier = Modifier.testTag("cat_edit_delete_btn")
                    ) {
                        Text("Delete")
                    }
                }
                
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(existingCategory?.id, name, selectedIconName, selectedColor.bgHex, selectedColor.textHex)
                        }
                    },
                    modifier = Modifier.testTag("cat_edit_save_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPurple,
                        contentColor = AccentPurpleOnContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextSub),
                modifier = Modifier.testTag("cat_edit_dismiss_btn")
            ) {
                Text("Cancel")
            }
        },
        containerColor = DarkCard,
        shape = RoundedCornerShape(24.dp)
    )
}
