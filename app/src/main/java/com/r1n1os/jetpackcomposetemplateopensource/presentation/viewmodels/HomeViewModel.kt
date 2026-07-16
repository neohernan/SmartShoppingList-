package com.r1n1os.jetpackcomposetemplateopensource.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r1n1os.jetpackcomposetemplateopensource.data.local.dao.SearchHistoryDao
import com.r1n1os.jetpackcomposetemplateopensource.data.mock.MockPriceDataSource
import com.r1n1os.jetpackcomposetemplateopensource.data.remote.GeminiAssistant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "Hernán",
    val featuredProducts: List<MockPriceDataSource.Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val recentProducts: List<MockPriceDataSource.Product> = emptyList(),
    val suggestionQuery: String = "",
    val suggestions: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val suggestedProductName: String? = null,
    val showGeminiDialog: Boolean = false,
    val geminiInput: String = "",
    val geminiResult: List<String>? = null,
    val isGeminiLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao,
    private val geminiAssistant: GeminiAssistant
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val featured = MockPriceDataSource.getFeaturedProducts()
            val categories = MockPriceDataSource.getCategories()
            val allProducts = MockPriceDataSource.getAllProducts()

            val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
            val mostFrequent = searchHistoryDao.getMostFrequentProduct(sevenDaysAgo)

            _uiState.value = _uiState.value.copy(
                featuredProducts = featured,
                categories = categories,
                recentProducts = allProducts.take(4),
                suggestedProductName = mostFrequent?.query,
                isLoading = false
            )
        }
    }

    fun onSuggestionQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            suggestionQuery = query,
            suggestions = if (query.isBlank()) emptyList()
            else MockPriceDataSource.getSuggestions(query)
        )
    }

    fun clearSuggestions() {
        _uiState.value = _uiState.value.copy(
            suggestionQuery = "",
            suggestions = emptyList()
        )
    }

    fun refreshData() {
        loadHomeData()
    }

    // ── Gemini dialog ──────────────────────────────────────────────────

    fun showGeminiDialog() {
        _uiState.value = _uiState.value.copy(
            showGeminiDialog = true,
            geminiInput = "",
            geminiResult = null,
            isGeminiLoading = false
        )
    }

    fun dismissGeminiDialog() {
        _uiState.value = _uiState.value.copy(showGeminiDialog = false)
    }

    fun onGeminiInputChanged(input: String) {
        _uiState.value = _uiState.value.copy(geminiInput = input)
    }

    fun generateShoppingList() {
        val input = _uiState.value.geminiInput.trim()
        if (input.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGeminiLoading = true,
                geminiResult = null
            )
            val result = geminiAssistant.generateShoppingList(input)
            result.fold(
                onSuccess = { items ->
                    _uiState.value = _uiState.value.copy(
                        isGeminiLoading = false,
                        geminiResult = items
                    )
                },
                onFailure = { _ ->
                    _uiState.value = _uiState.value.copy(
                        isGeminiLoading = false,
                        geminiResult = emptyList()
                    )
                }
            )
        }
    }
}
