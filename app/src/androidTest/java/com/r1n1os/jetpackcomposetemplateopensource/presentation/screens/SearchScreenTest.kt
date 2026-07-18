package com.r1n1os.jetpackcomposetemplateopensource.presentation.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.r1n1os.jetpackcomposetemplateopensource.presentation.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for [SearchScreen].
 *
 * Launches [MainActivity] via [createAndroidComposeRule] — the Activity
 * already calls setContent with the full app navigation. Tests navigate
 * to the search screen via the bottom navigation bar and interact using
 * the real Hilt-managed [SearchViewModel].
 */
@RunWith(AndroidJUnit4::class)
class SearchScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        composeTestRule.waitForIdle()
    }

    /** Navigates from home to the search screen via the bottom nav bar. */
    private fun navigateToSearch() {
        composeTestRule.onNodeWithText("Buscar").performClick()
        composeTestRule.waitForIdle()
    }

    // ── Navigation ──────────────────────────────────────────────────────

    @Test
    fun navigateToSearchScreen_searchFieldIsDisplayed() {
        navigateToSearch()

        composeTestRule.onNodeWithText("Buscar productos, categorías...")
            .assertIsDisplayed()
    }

    // ── Search field ────────────────────────────────────────────────────

    @Test
    fun searchField_typing_showsClearButton() {
        navigateToSearch()

        val searchField = composeTestRule.onNodeWithText("Buscar productos, categorías...")
        searchField.performTextClearance()
        searchField.performTextInput("leche")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Limpiar")
            .assertIsDisplayed()
    }

    @Test
    fun searchField_clearButton_clearsQuery() {
        navigateToSearch()

        val searchField = composeTestRule.onNodeWithText("Buscar productos, categorías...")
        searchField.performTextClearance()
        searchField.performTextInput("leche")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Limpiar").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Limpiar")
            .assertIsNotDisplayed()
    }

    @Test
    fun searchField_clearButton_restoresAllProducts() {
        navigateToSearch()

        val searchField = composeTestRule.onNodeWithText("Buscar productos, categorías...")
        searchField.performTextClearance()
        searchField.performTextInput("leche")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Limpiar").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("12 productos")
            .assertIsDisplayed()
    }

    // ── Results ─────────────────────────────────────────────────────────

    @Test
    fun searchField_emptyQuery_showsAllProducts() {
        navigateToSearch()

        composeTestRule.onNodeWithText("12 productos")
            .assertIsDisplayed()
    }

    @Test
    fun searchField_typing_filtersResults() {
        navigateToSearch()

        val searchField = composeTestRule.onNodeWithText("Buscar productos, categorías...")
        searchField.performTextClearance()
        searchField.performTextInput("leche")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("1 resultados para \"leche\"")
            .assertIsDisplayed()
    }

    @Test
    fun searchField_emptySearch_showsNoResultsMessage() {
        navigateToSearch()

        val searchField = composeTestRule.onNodeWithText("Buscar productos, categorías...")
        searchField.performTextClearance()
        searchField.performTextInput("xyznoexiste")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("No se encontraron productos")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Intenta con otro término de búsqueda")
            .assertIsDisplayed()
    }

    // ── Category filtering via search ────────────────────────────────────

    @Test
    fun search_byCategory_filtersResults() {
        navigateToSearch()

        val searchField = composeTestRule.onNodeWithText("Buscar productos, categorías...")
        searchField.performTextClearance()
        searchField.performTextInput("Carnes")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Pechuga de Pollo 1kg")
            .assertIsDisplayed()
    }

    // ── Product result card ─────────────────────────────────────────────

    @Test
    fun productResultCard_showsProductName() {
        navigateToSearch()

        composeTestRule.onNodeWithText("Leche Entera 1L")
            .assertIsDisplayed()
    }
}
