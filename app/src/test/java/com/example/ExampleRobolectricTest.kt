package com.example

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Sophisticated Budget", appName)
  }

  @Test
  fun `test UI navigation and add transaction dialog`() {
    // Check main tab elements are visible on startup
    composeTestRule.onNodeWithTag("nav_home").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_trends").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_budget").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_settings").assertIsDisplayed()

    // Switch to Preferences / Settings
    composeTestRule.onNodeWithTag("nav_settings").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("edit_username_btn").assertIsDisplayed()

    // Switch to Budget screen
    composeTestRule.onNodeWithTag("nav_budget").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("edit_budget_btn").assertIsDisplayed()

    // Click on '+' add transaction button in CustomBottomBar
    composeTestRule.onNodeWithTag("nav_add_transaction").performClick()
    composeTestRule.waitForIdle()

    // Verify dialog elements exist and inputs can accept text
    composeTestRule.onNodeWithTag("add_trx_title").performTextInput("Latte Coffee")
    composeTestRule.onNodeWithTag("add_trx_amount").performTextInput("5.25")

    // Direct click to choose Dining & Food category item row
    composeTestRule.onNodeWithTag("select_cat_Dining_&_Food").performClick()

    // Click Record button to submit that transaction
    composeTestRule.onNodeWithTag("save_trx_btn").performClick()
    composeTestRule.waitForIdle()

    // Navigate back to Home and ensure UI is fine
    composeTestRule.onNodeWithTag("nav_home").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun `test custom category creation and modification`() {
    // Navigate to Settings Screen
    composeTestRule.onNodeWithTag("nav_settings").performClick()
    composeTestRule.waitForIdle()

    // Scroll settings LazyColumn to make add category button viewable/composed
    composeTestRule.onNodeWithTag("settings_lazy_column")
        .performScrollToNode(hasTestTag("add_custom_category_btn"))
    composeTestRule.waitForIdle()

    // Click the create custom category button
    composeTestRule.onNodeWithTag("add_custom_category_btn").performClick()
    composeTestRule.waitForIdle()

    // Type a custom category name
    composeTestRule.onNodeWithTag("cat_edit_name_input").performTextInput("Subscriptions")
    
    // Select an icon and a color option from the first-row (fully visible/not virtualized)
    composeTestRule.onNodeWithTag("select_icon_LocalPlay").performClick()
    composeTestRule.onNodeWithTag("select_color_Rose").performClick()

    // Click Save
    composeTestRule.onNodeWithTag("cat_edit_save_btn").performClick()
    composeTestRule.waitForIdle()

    // Wait and scroll to the newly created category in the list
    composeTestRule.waitUntil(5000) {
        composeTestRule.onAllNodesWithTag("manage_cat_Subscriptions").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithTag("settings_lazy_column")
        .performScrollToNode(hasTestTag("manage_cat_Subscriptions"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("manage_cat_Subscriptions").assertIsDisplayed()

    // Edit it
    composeTestRule.onNodeWithTag("manage_cat_Subscriptions").performClick()
    composeTestRule.waitForIdle()

    // Clear input and change name
    composeTestRule.onNodeWithTag("cat_edit_name_input").performTextClearance()
    composeTestRule.onNodeWithTag("cat_edit_name_input").performTextInput("Netflix")

    // Save
    composeTestRule.onNodeWithTag("cat_edit_save_btn").performClick()
    composeTestRule.waitForIdle()

    // Wait and scroll to the updated category
    composeTestRule.waitUntil(5000) {
        composeTestRule.onAllNodesWithTag("manage_cat_Netflix").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithTag("settings_lazy_column")
        .performScrollToNode(hasTestTag("manage_cat_Netflix"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("manage_cat_Netflix").assertIsDisplayed()
  }

  @Test
  fun `test relative date presets and ledger mode switching`() {
    // Switch to Budget screen
    composeTestRule.onNodeWithTag("nav_budget").performClick()
    composeTestRule.waitForIdle()

    // Scroll container to make ledger tabs visible/composed
    composeTestRule.onNodeWithTag("budget_lazy_column")
        .performScrollToNode(hasTestTag("ledger_tab_DAILY"))
    composeTestRule.waitForIdle()

    // Ensure the tab selectors exist and are displayed
    composeTestRule.onNodeWithTag("ledger_tab_DAILY").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ledger_tab_MONTHLY").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ledger_tab_YEARLY").assertIsDisplayed()

    // Click on '+' add transaction button in CustomBottomBar
    composeTestRule.onNodeWithTag("nav_add_transaction").performClick()
    composeTestRule.waitForIdle()

    // Assert date presets are displayed
    composeTestRule.onNodeWithTag("date_preset_Today").assertIsDisplayed()
    composeTestRule.onNodeWithTag("date_preset_Yesterday").assertIsDisplayed()
    composeTestRule.onNodeWithTag("date_preset_Last Month").assertIsDisplayed()
    composeTestRule.onNodeWithTag("date_preset_Last Year").assertIsDisplayed()

    // Fill in a yesterday expense
    composeTestRule.onNodeWithTag("add_trx_title").performTextInput("Yesterday Dinner")
    composeTestRule.onNodeWithTag("add_trx_amount").performTextInput("150.00")
    composeTestRule.onNodeWithTag("date_preset_Yesterday").performClick()
    composeTestRule.onNodeWithTag("save_trx_btn").performClick()
    composeTestRule.waitForIdle()

    // Scroll to Monthly tab and click it
    composeTestRule.onNodeWithTag("budget_lazy_column")
        .performScrollToNode(hasTestTag("ledger_tab_MONTHLY"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ledger_tab_MONTHLY").performClick()
    composeTestRule.waitForIdle()

    // Scroll to Yearly tab and click it
    composeTestRule.onNodeWithTag("budget_lazy_column")
        .performScrollToNode(hasTestTag("ledger_tab_YEARLY"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ledger_tab_YEARLY").performClick()
    composeTestRule.waitForIdle()
  }
}
