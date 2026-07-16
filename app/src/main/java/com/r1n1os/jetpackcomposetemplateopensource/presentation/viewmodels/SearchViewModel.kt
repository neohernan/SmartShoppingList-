package com.r1n1os.jetpackcomposetemplateopensource.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r1n1os.jetpackcomposetemplateopensource.data.local.dao.ProductDao
import com.r1n1os.jetpackcomposetemplateopensource.data.local.dao.SearchHistoryDao
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.ProductEntity
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.SearchHistoryEntity
import com.r1n1os.jetpackcomposetemplateopensource.data.mock.MockPriceDataSource
import com.r1n1os.jetpackcomposetemplateopensource.presentation.models.ProductResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val productDao: ProductDao,
    private val searchHistoryDao: SearchHistoryDao
) : ViewModel() {

    init {
        // Load persisted products from Room ONCE (not a persistent Flow).
        // This ensures saved products appear in search across app restarts.
        viewModelScope.launch {
            try {
                val dbProducts = productDao.getAllProductsOnce()
                val persisted = dbProducts.map { entity ->
                    MockPriceDataSource.Product(
                        id = "room_${entity.barcode}",
                        name = entity.name,
                        category = entity.category,
                        barcode = entity.barcode,
                        prices = listOf(
                            MockPriceDataSource.StorePrice("Sin precio", 0.0)
                        ),
                        isFeatured = entity.isFeatured
                    )
                }
                _userProducts.value = persisted
                // Also populate MockPriceDataSource for ScannerViewModel lookups
                persisted.forEach { p ->
                    MockPriceDataSource.addProduct(p.name, p.barcode)
                }
                Log.d("SearchViewModel", "Loaded ${persisted.size} products from Room")
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Failed to load products from Room", e)
            }
        }
    }

    /** The current search query, updated on each keystroke. */
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** User-added products (scanned barcodes) — declared BEFORE results. */
    private val _userProducts = MutableStateFlow<List<MockPriceDataSource.Product>>(emptyList())

    /** Reactive search results derived from the query using flatMapLatest. */
    val results: StateFlow<List<ProductResult>> = _query
        .flatMapLatest { q -> flow { emit(search(q.trim())) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = search("")
        )

    /** Categories for filter chips. */
    val categories: StateFlow<List<String>> = MutableStateFlow(
        MockPriceDataSource.getCategories()
    ).asStateFlow()

    /** Suggestions shown while typing. */
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    // ── Add-product dialog state ────────────────────────────────────────

    private val _showAddDialog = MutableStateFlow<Pair<String, String>?>(null)
    val showAddDialog: StateFlow<Pair<String, String>?> = _showAddDialog.asStateFlow()

    // ── Public API ───────────────────────────────────────────────────────

    fun onQueryChanged(query: String) {
        _query.value = query
        _suggestions.value = if (query.length >= 2) {
            MockPriceDataSource.getSuggestions(query)
        } else {
            emptyList()
        }
    }

    fun onSuggestionSelected(suggestion: String) {
        _query.value = suggestion
        _suggestions.value = emptyList()
    }

    fun clearSearch() {
        _query.value = ""
        _suggestions.value = emptyList()
    }

    fun selectProduct(productName: String) {
        viewModelScope.launch {
            searchHistoryDao.insertSearch(
                SearchHistoryEntity(query = productName)
            )
        }
    }

    /** Opens the add-product dialog for the given barcode. */
    fun showAddProductDialog(barcode: String) {
        _showAddDialog.value = Pair(barcode, "")
    }

    /**
     * Checks if a product with [barcode] exists first in MockPriceDataSource,
     * then in the user-added list, then in Room.
     * If found anywhere, autocompletes the search and returns true.
     */
    suspend fun processBarcodeArg(barcode: String): Boolean {
        // 1) MockPriceDataSource (built-in + recently scanned via addProduct)
        val mockProduct = MockPriceDataSource.getProductByBarcode(barcode)
        if (mockProduct != null) {
            _query.value = mockProduct.name
            return true
        }

        // 2) User-added products (local list in this ViewModel)
        val userProduct = _userProducts.value.find { it.barcode == barcode }
        if (userProduct != null) {
            _query.value = userProduct.name
            return true
        }

        // 3) Room database (persistent storage)
        val dbProduct = productDao.getProductByBarcode(barcode)
        if (dbProduct != null) {
            _query.value = dbProduct.name
            return true
        }

        return false
    }

    /** Updates the name field inside the dialog while the user types. */
    fun updateNewProductName(name: String) {
        _showAddDialog.value = _showAddDialog.value?.let { (barcode, _) ->
            Pair(barcode, name)
        }
    }

    /** Closes the dialog without saving. */
    fun dismissAddProductDialog() {
        _showAddDialog.value = null
    }

    /**
     * Persists the new product into Room database AND into the local
     * user-products list so it appears immediately in search results.
     * After saving, closes the dialog and autocompletes the search.
     */
    fun saveNewProduct() {
        val current = _showAddDialog.value ?: return
        val (barcode, name) = current
        if (name.isBlank()) return

        viewModelScope.launch {
            try {
                // 1) Insert into Room database (persistent storage)
                Log.d("SearchViewModel", "Inserting product: barcode=$barcode, name=$name")
                productDao.insertProduct(
                    ProductEntity(
                        barcode = barcode,
                        name = name,
                        category = "Otros"
                    )
                )
                Log.d("SearchViewModel", "Room insert succeeded")

                // 2) Add to MockPriceDataSource (for ScannerViewModel + processBarcodeArg)
                MockPriceDataSource.addProduct(name = name, barcode = barcode)

                // 3) Add to local user-products list (only if not already present)
                val alreadyExists = _userProducts.value.any { it.barcode == barcode }
                if (!alreadyExists) {
                    val newProduct = MockPriceDataSource.Product(
                        id = "user_${barcode}",
                        name = name,
                        category = "Otros",
                        barcode = barcode,
                        prices = listOf(
                            MockPriceDataSource.StorePrice("Sin precio", 0.0)
                        ),
                        isFeatured = false
                    )
                    _userProducts.value = _userProducts.value + newProduct
                }

                // 4) Save to search history
                searchHistoryDao.insertSearch(
                    SearchHistoryEntity(query = name)
                )

                // 5) Close dialog and autocomplete search
                _showAddDialog.value = null
                _query.value = name

                Log.d("SearchViewModel", "Product saved successfully: $name")
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Failed to save product", e)
                // Re-throw so the app can handle it if needed
                throw e
            }
        }
    }

    // ── Private search logic ─────────────────────────────────────────────

    private fun search(query: String): List<ProductResult> {
        // Combine MockPriceDataSource products with user-added products
        val allProducts = MockPriceDataSource.getAllProducts() + _userProducts.value

        val matchingProducts = if (query.isBlank()) {
            allProducts
        } else {
            allProducts.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.category.contains(query, ignoreCase = true)
            }
        }

        return matchingProducts.map { product ->
            val priceMap = product.prices.associate { it.storeName to it.price }
            ProductResult(
                id = product.id,
                name = product.name,
                category = product.category,
                barcode = product.barcode,
                prices = priceMap,
                cheapestStore = product.bestStore,
                cheapestPrice = product.bestPrice,
                isFeatured = product.isFeatured
            )
        }
    }
}
