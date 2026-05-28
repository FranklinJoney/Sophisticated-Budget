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
}
