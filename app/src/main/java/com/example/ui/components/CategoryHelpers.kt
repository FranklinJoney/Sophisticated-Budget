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

object CategoryHelpers {
    val CATEGORIES = listOf(
        CategoryMeta(
            id = "Dining & Food",
            displayName = "Dining & Food",
            icon = Icons.Default.Restaurant,
            bgColor = CatDiningBg,
            iconColor = CatDiningText
        ),
        CategoryMeta(
            id = "Transport",
            displayName = "Transport",
            icon = Icons.Default.DirectionsCar,
            bgColor = CatTransportBg,
            iconColor = CatTransportText
        ),
        CategoryMeta(
            id = "Shopping",
            displayName = "Shopping",
            icon = Icons.Default.ShoppingBag,
            bgColor = CatShoppingBg,
            iconColor = CatShoppingOnBg
        ),
        CategoryMeta(
            id = "Utilities",
            displayName = "Utilities",
            icon = Icons.Default.Bolt,
            bgColor = CatUtilitiesBg,
            iconColor = CatUtilitiesOnBg
        ),
        CategoryMeta(
            id = "Entertainment",
            displayName = "Entertainment",
            icon = Icons.Default.LocalPlay,
            bgColor = CatEntertainmentBg,
            iconColor = CatEntertainmentOnBg
        ),
        CategoryMeta(
            id = "Other",
            displayName = "Other",
            icon = Icons.Default.Category,
            bgColor = CatOtherBg,
            iconColor = CatOtherOnBg
        )
    )

    fun getMeta(categoryName: String): CategoryMeta {
        return CATEGORIES.find { it.id.equals(categoryName, ignoreCase = true) }
            ?: CategoryMeta(
                id = "Other",
                displayName = "Other",
                icon = Icons.Default.Category,
                bgColor = CatOtherBg,
                iconColor = CatOtherOnBg
            )
    }
}
