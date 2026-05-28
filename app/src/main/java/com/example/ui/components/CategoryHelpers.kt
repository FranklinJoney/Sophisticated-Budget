package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.*

data class CategoryMeta(
    val id: String,
    val displayName: String,
    val icon: ImageVector,
    val bgColor: Color,
    val iconColor: Color
)

data class ColorOption(
    val name: String,
    val bgHex: String,
    val textHex: String
)

data class IconOption(
    val id: String,
    val label: String,
    val icon: ImageVector
)

object CategoryHelpers {

    val COLORS = listOf(
        ColorOption("Lavender", "#FFEADDFF", "#FF21005D"),
        ColorOption("Rose", "#FFFFD8E4", "#FF31111D"),
        ColorOption("Coral", "#FFF2B8B5", "#FF601410"),
        ColorOption("Gold", "#FFFFDF9E", "#FF4C3000"),
        ColorOption("Mint", "#FFC2F0C2", "#FF003D00"),
        ColorOption("Sky Blue", "#FFC2E7FF", "#FF001D35"),
        ColorOption("Indigo", "#FFBAC2FF", "#FF000F5D"),
        ColorOption("Ice", "#FFE6E1E5", "#FF1D1B20")
    )

    val ICONS = listOf(
        IconOption("Restaurant", "Dining", Icons.Default.Restaurant),
        IconOption("DirectionsCar", "Transport", Icons.Default.DirectionsCar),
        IconOption("ShoppingBag", "Shopping", Icons.Default.ShoppingBag),
        IconOption("Bolt", "Utilities", Icons.Default.Bolt),
        IconOption("LocalPlay", "Entertainment", Icons.Default.LocalPlay),
        IconOption("Savings", "Savings", Icons.Default.Savings),
        IconOption("MedicalServices", "Health", Icons.Default.MedicalServices),
        IconOption("School", "Education", Icons.Default.School),
        IconOption("Home", "Housing", Icons.Default.Home),
        IconOption("Flight", "Travel", Icons.Default.Flight),
        IconOption("FitnessCenter", "Fitness", Icons.Default.FitnessCenter),
        IconOption("Work", "Business", Icons.Default.Work),
        IconOption("Spa", "Self-Care", Icons.Default.Spa),
        IconOption("CardMembership", "Subscriptions", Icons.Default.CardMembership),
        IconOption("WaterDrop", "Water Bill", Icons.Default.WaterDrop),
        IconOption("Pets", "Pets", Icons.Default.Pets),
        IconOption("LocalCafe", "Coffee Shop", Icons.Default.LocalCafe),
        IconOption("Fastfood", "Fast Food", Icons.Default.Fastfood),
        IconOption("CardGiftcard", "Gifts", Icons.Default.CardGiftcard),
        IconOption("Celebration", "Party/Fun", Icons.Default.Celebration),
        IconOption("Category", "Other", Icons.Default.Category)
    )

    val DEFAULT_CATEGORIES = listOf(
        com.example.data.Category("Dining & Food", "Dining & Food", "Restaurant", "#FFE8DEF8", "#FF21005D"),
        com.example.data.Category("Transport", "Transport", "DirectionsCar", "#FFD0BCFF", "#FF381E72"),
        com.example.data.Category("Shopping", "Shopping", "ShoppingBag", "#FFBAC2FF", "#FF000F5D"),
        com.example.data.Category("Utilities", "Utilities", "Bolt", "#FFF2B8B5", "#FF601410"),
        com.example.data.Category("Entertainment", "Entertainment", "LocalPlay", "#FFFFD8E4", "#FF31111D"),
        com.example.data.Category("Other", "Other", "Category", "#FFC2E7FF", "#FF001D35")
    )

    val DEFAULT_METAS = DEFAULT_CATEGORIES.map { fromDb(it) }

    // Backup standard getter for safety during initialization/re-seeding
    fun getMeta(categoryName: String, fallbackList: List<CategoryMeta> = DEFAULT_METAS): CategoryMeta {
        return fallbackList.find { it.id.equals(categoryName, ignoreCase = true) }
            ?: fallbackList.find { it.id.equals("Other", ignoreCase = true) }
            ?: CategoryMeta(
                id = "Other",
                displayName = "Other",
                icon = Icons.Default.Category,
                bgColor = CatOtherBg,
                iconColor = CatOtherOnBg
            )
    }

    fun getIconByName(name: String): ImageVector {
        return ICONS.find { it.id == name }?.icon ?: Icons.Default.Category
    }

    fun fromDb(category: com.example.data.Category): CategoryMeta {
        val bgCol = try { Color(android.graphics.Color.parseColor(category.bgColorHex)) } catch (e: Exception) { CatOtherBg }
        val iconCol = try { Color(android.graphics.Color.parseColor(category.iconColorHex)) } catch (e: Exception) { CatOtherOnBg }
        return CategoryMeta(
            id = category.id,
            displayName = category.displayName,
            icon = getIconByName(category.iconName),
            bgColor = bgCol,
            iconColor = iconCol
        )
    }
}
