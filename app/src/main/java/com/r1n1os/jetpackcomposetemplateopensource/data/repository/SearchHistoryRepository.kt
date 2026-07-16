package com.r1n1os.jetpackcomposetemplateopensource.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing search history.
 * Uses an in-memory store. When a Room DAO is available,
 * this can be swapped to persist history in the database.
 */
@Singleton
class SearchHistoryRepository @Inject constructor() {

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: Flow<List<String>> = _searchHistory.asStateFlow()

    /**
     * Saves a product name to the search history.
     * Avoids duplicates and keeps the most recent at the top.
     */
    suspend fun saveSearch(productName: String) {
        val current = _searchHistory.value.toMutableList()
        // Remove if already exists (to move it to top)
        current.remove(productName)
        // Add to the beginning
        current.add(0, productName)
        // Keep only last 20 searches
        if (current.size > 20) {
            current.removeAt(current.lastIndex)
        }
        _searchHistory.value = current
    }

    /**
     * Returns the list of recent search terms.
     */
    fun getRecentSearches(): List<String> = _searchHistory.value

    /**
     * Clears all search history.
     */
    suspend fun clearHistory() {
        _searchHistory.value = emptyList()
    }
}
