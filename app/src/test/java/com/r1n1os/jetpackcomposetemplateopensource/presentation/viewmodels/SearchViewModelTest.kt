package com.r1n1os.jetpackcomposetemplateopensource.presentation.viewmodels

import com.r1n1os.jetpackcomposetemplateopensource.data.local.dao.ProductDao
import com.r1n1os.jetpackcomposetemplateopensource.data.local.dao.SearchHistoryDao
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.ProductEntity
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.SearchHistoryEntity
import com.r1n1os.jetpackcomposetemplateopensource.data.mock.MockPriceDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [SearchViewModel].
 *
 * Uses fake implementations of [ProductDao] and [SearchHistoryDao]
 * to avoid dependency on Room and the Android framework.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    // ── Test dispatcher ──────────────────────────────────────────────────
    private val testDispatcher = StandardTestDispatcher()

    // ── Fakes ────────────────────────────────────────────────────────────
    private lateinit var fakeProductDao: FakeProductDao
    private lateinit var fakeSearchHistoryDao: FakeSearchHistoryDao
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeProductDao = FakeProductDao()
        fakeSearchHistoryDao = FakeSearchHistoryDao()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Helper: create ViewModel after setting up fakes ──────────────────

    private fun createViewModel(): SearchViewModel {
        return SearchViewModel(
            productDao = fakeProductDao,
            searchHistoryDao = fakeSearchHistoryDao
        )
    }

    /**
     * Keeps the [SearchViewModel.results] StateFlow upstream alive by
     * collecting it in the given [scope]. This is needed because the flow
     * uses [kotlinx.coroutines.flow.SharingStarted.WhileSubscribed], which
     * defers upstream start until the first subscriber appears.
     */
    private fun keepResultsAlive(vm: SearchViewModel, scope: CoroutineScope) {
        scope.launch(testDispatcher) {
            vm.results.collect { /* keep upstream alive */ }
        }
    }

    // ────────────────────────────────────────────────────────────────────
    // Initial state
    // ────────────────────────────────────────────────────────────────────

    @Test
    fun `initialState query is empty and categories are loaded`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("La consulta inicial debe estar vacia", "", viewModel.query.value)
        assertTrue("Las categorias no deben estar vacias", viewModel.categories.value.isNotEmpty())
        assertEquals(
            "Deben cargarse las categorias del datasource",
            MockPriceDataSource.getCategories(),
            viewModel.categories.value
        )
    }

    @Test
    fun `initialState suggestions are empty`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue("Las sugerencias iniciales deben estar vacias", viewModel.suggestions.value.isEmpty())
    }

    @Test
    fun `initialState results contain all products when query is empty`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        val results = viewModel.results.first()
        val allMockProducts = MockPriceDataSource.getAllProducts()
        assertEquals(
            "Con query vacio deben mostrarse todos los productos",
            allMockProducts.size,
            results.size
        )
    }

    // ────────────────────────────────────────────────────────────────────
    // onQueryChanged
    // ────────────────────────────────────────────────────────────────────

    @Test
    fun `onQueryChanged updates the query state`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChanged("leche")
        assertEquals("El query debe actualizarse", "leche", viewModel.query.value)
    }

    @Test
    fun `onQueryChanged with 2+ chars loads suggestions`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChanged("la")
        advanceUntilIdle()

        val suggestions = viewModel.suggestions.value
        assertTrue("Debe haber sugerencias para 'la'", suggestions.isNotEmpty())
        assertTrue("Las sugerencias deben contener 'la'", suggestions.all { it.contains("la", ignoreCase = true) })
    }

    @Test
    fun `onQueryChanged with less than 2 chars clears suggestions`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        // First trigger suggestions
        viewModel.onQueryChanged("la")
        advanceUntilIdle()
        assertTrue("Debe haber sugerencias primero", viewModel.suggestions.value.isNotEmpty())

        // Then reduce to single char
        viewModel.onQueryChanged("l")
        advanceUntilIdle()

        assertTrue("Con 1 caracter no debe haber sugerencias", viewModel.suggestions.value.isEmpty())
    }

    @Test
    fun `onQueryChanged with empty string clears suggestions`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChanged("la")
        advanceUntilIdle()
        assertTrue("Debe haber sugerencias primero", viewModel.suggestions.value.isNotEmpty())

        viewModel.onQueryChanged("")
        advanceUntilIdle()

        assertTrue("Con string vacio no debe haber sugerencias", viewModel.suggestions.value.isEmpty())
    }

    // ────────────────────────────────────────────────────────────────────
    // clearSearch
    // ────────────────────────────────────────────────────────────────────

    @Test
    fun `clearSearch resets query and suggestions`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChanged("leche")
        advanceUntilIdle()
        assertEquals("El query debe ser 'leche'", "leche", viewModel.query.value)

        viewModel.clearSearch()
        assertEquals("El query debe estar vacio tras clearSearch", "", viewModel.query.value)
        assertTrue("Las sugerencias deben estar vacias tras clearSearch", viewModel.suggestions.value.isEmpty())
    }

    // ────────────────────────────────────────────────────────────────────
    // onSuggestionSelected
    // ────────────────────────────────────────────────────────────────────

    @Test
    fun `onSuggestionSelected sets query and clears suggestions`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChanged("le")
        advanceUntilIdle()
        assertTrue("Debe haber sugerencias primero", viewModel.suggestions.value.isNotEmpty())

        val selected = viewModel.suggestions.value.first()
        viewModel.onSuggestionSelected(selected)

        assertEquals("El query debe ser la sugerencia seleccionada", selected, viewModel.query.value)
        assertTrue("Las sugerencias deben limpiarse tras seleccionar", viewModel.suggestions.value.isEmpty())
    }

    // ────────────────────────────────────────────────────────────────────
    // Search results (via `results` StateFlow)
    // ────────────────────────────────────────────────────────────────────

    @Test
    fun `search filters products by name case-insensitively`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        keepResultsAlive(viewModel, backgroundScope)
        advanceUntilIdle()

        viewModel.onQueryChanged("leche")
        advanceUntilIdle()

        val results = viewModel.results.first()
        assertTrue("Debe encontrar 'Leche Entera 1L'", results.any { it.name.contains("Leche", ignoreCase = true) })
        assertTrue("Todos los resultados deben coincidir con el query", results.all { result ->
            result.name.contains("leche", ignoreCase = true) ||
                    result.category.contains("leche", ignoreCase = true)
        })
    }

    @Test
    fun `search filters products by category case-insensitively`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        keepResultsAlive(viewModel, backgroundScope)
        advanceUntilIdle()

        viewModel.onQueryChanged("carne")
        advanceUntilIdle()

        val results = viewModel.results.first()
        assertTrue("Debe encontrar productos de categoria 'Carnes'", results.isNotEmpty())
        assertTrue("Todos los resultados deben ser de la categoria 'Carnes'", results.all { it.category == "Carnes" })
    }

    @Test
    fun `search returns empty list when no products match`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        keepResultsAlive(viewModel, backgroundScope)
        advanceUntilIdle()

        viewModel.onQueryChanged("xyznoexiste")
        advanceUntilIdle()

        val results = viewModel.results.first()
        assertTrue("Sin coincidencias debe devolver lista vacia", results.isEmpty())
    }

    @Test
    fun `search with partial name returns matching products`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChanged("pan")
        advanceUntilIdle()

        val results = viewModel.results.first()
        assertTrue("Debe encontrar 'Pan Artesanal'", results.any { it.name.contains("Pan", ignoreCase = true) })
    }

    // ────────────────────────────────────────────────────────────────────
    // processBarcodeArg
    // ────────────────────────────────────────────────────────────────────

    @Test
    fun `processBarcodeArg finds existing barcode in MockDataSource`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        val barcode = "780001000101" // Leche Entera 1L
        val found = viewModel.processBarcodeArg(barcode)

        assertTrue("El codigo de barras existente debe ser encontrado", found)
        assertEquals("El query debe autocompletarse con el nombre del producto", "Leche Entera 1L", viewModel.query.value)
    }

    @Test
    fun `processBarcodeArg returns false for non-existing barcode`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        val barcode = "999999999999"
        val found = viewModel.processBarcodeArg(barcode)

        assertFalse("El codigo de barras inexistente no debe ser encontrado", found)
        assertEquals("El query no debe modificarse", "", viewModel.query.value)
    }

    @Test
    fun `processBarcodeArg finds barcode from scanned product in Room`() = runTest(testDispatcher) {
        fakeProductDao.insertProduct(
            ProductEntity(barcode = "123456789012", name = "Producto Escaneado", category = "Otros")
        )
        viewModel = createViewModel()
        advanceUntilIdle()

        val found = viewModel.processBarcodeArg("123456789012")

        assertTrue("El producto guardado en Room debe ser encontrado", found)
        assertEquals(
            "El query debe autocompletarse con el nombre del producto escaneado",
            "Producto Escaneado",
            viewModel.query.value
        )
    }

    // ────────────────────────────────────────────────────────────────────
    // selectProduct
    // ────────────────────────────────────────────────────────────────────

    @Test
    fun `selectProduct saves query to search history`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectProduct("Leche Entera 1L")
        advanceUntilIdle()

        val history = fakeSearchHistoryDao.getAllSearches()
        assertTrue("El producto seleccionado debe estar en el historial", history.any { it.query == "Leche Entera 1L" })
    }

    // ────────────────────────────────────────────────────────────────────
    // Add-product dialog
    // ────────────────────────────────────────────────────────────────────

    @Test
    fun `showAddProductDialog sets dialog state with barcode and empty name`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.showAddProductDialog("123456789012")

        val dialog = viewModel.showAddDialog.value
        assertNotNull("El dialogo debe estar visible", dialog)
        assertEquals("El barcode debe ser el especificado", "123456789012", dialog?.first)
        assertEquals("El nombre debe iniciar vacio", "", dialog?.second)
    }

    @Test
    fun `updateNewProductName updates name in dialog state`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.showAddProductDialog("123456789012")
        viewModel.updateNewProductName("Producto Nuevo")

        val dialog = viewModel.showAddDialog.value
        assertNotNull("El dialogo debe estar visible", dialog)
        assertEquals("El nombre debe actualizarse", "Producto Nuevo", dialog?.second)
    }

    @Test
    fun `dismissAddProductDialog clears dialog state`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.showAddProductDialog("123456789012")
        assertNotNull("El dialogo debe estar visible primero", viewModel.showAddDialog.value)

        viewModel.dismissAddProductDialog()
        assertNull("El dialogo debe cerrarse", viewModel.showAddDialog.value)
    }

    @Test
    fun `saveNewProduct inserts product and updates query`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Open dialog with barcode
        viewModel.showAddProductDialog("888888888888")
        viewModel.updateNewProductName("Producto Test")
        advanceUntilIdle()

        viewModel.saveNewProduct()
        advanceUntilIdle()

        // Dialog should be closed
        assertNull("El dialogo debe cerrarse tras guardar", viewModel.showAddDialog.value)

        // Query should be autocompleted
        assertEquals("El query debe ser el nombre del producto guardado", "Producto Test", viewModel.query.value)

        // Product should be in Room DB
        val dbProduct = fakeProductDao.getProductByBarcode("888888888888")
        assertNotNull("El producto debe estar en Room", dbProduct)
        assertEquals("Producto Test", dbProduct?.name)

        // Product should be in search history
        val history = fakeSearchHistoryDao.getAllSearches()
        assertTrue("El producto debe estar en el historial", history.any { it.query == "Producto Test" })
    }

    @Test
    fun `saveNewProduct with blank name does nothing`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.showAddProductDialog("888888888888")
        // Don't set a name (remains blank)
        advanceUntilIdle()

        viewModel.saveNewProduct()
        advanceUntilIdle()

        // Dialog should still be open because name is blank
        assertNotNull("El dialogo no debe cerrarse si el nombre esta vacio", viewModel.showAddDialog.value)
    }

    @Test
    fun `saveNewProduct does not duplicate existing user product`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Save once
        viewModel.showAddProductDialog("888888888888")
        viewModel.updateNewProductName("Producto Test")
        advanceUntilIdle()
        viewModel.saveNewProduct()
        advanceUntilIdle()

        // Save again with same barcode
        viewModel.showAddProductDialog("888888888888")
        viewModel.updateNewProductName("Producto Test")
        advanceUntilIdle()
        viewModel.saveNewProduct()
        advanceUntilIdle()

        // Should not crash and dialog should close
        assertNull("El dialogo debe cerrarse", viewModel.showAddDialog.value)
    }

    // ────────────────────────────────────────────────────────────────────
    // results StateFlow reactivity
    // ────────────────────────────────────────────────────────────────────

    @Test
    fun `results update reactively when query changes`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        val initialCount = viewModel.results.first().size

        viewModel.onQueryChanged("leche")
        advanceUntilIdle()

        val filteredResults = viewModel.results.first()
        assertTrue(
            "Los resultados deben reducirse al filtrar por 'leche'",
            filteredResults.size < initialCount
        )
        assertTrue("Todos deben contener 'leche'", filteredResults.all {
            it.name.contains("leche", ignoreCase = true) || it.category.contains("leche", ignoreCase = true)
        })
    }

    @Test
    fun `results contain correct product mapping`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        keepResultsAlive(viewModel, backgroundScope)
        advanceUntilIdle()

        viewModel.onQueryChanged("Leche Entera 1L")
        advanceUntilIdle()

        val results = viewModel.results.first()
        assertEquals("Debe encontrar exactamente 1 producto", 1, results.size)

        val product = results.first()
        assertEquals("Leche Entera 1L", product.name)
        assertTrue("La categoria no debe estar vacia", product.category.isNotBlank())
        assertTrue("Debe tener precios de varios supermercados", product.prices.isNotEmpty())
        assertTrue("Debe tener un precio mas economico", product.cheapestPrice > 0)
        assertTrue("Debe indicar la tienda mas economica", product.cheapestStore.isNotBlank())
    }

    // ────────────────────────────────────────────────────────────────────
    // Potential savings
    // ────────────────────────────────────────────────────────────────────

    @Test
    fun `productResult includes potentialSavings`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChanged("Leche Entera 1L")
        advanceUntilIdle()

        val product = viewModel.results.first().first()
        assertTrue(
            "El ahorro potencial debe ser positivo si hay multiples precios",
            product.potentialSavings >= 0
        )
    }

    // ────────────────────────────────────────────────────────────────────
    // MockPriceDataSource state isolation (optional cleanup note)
    // ────────────────────────────────────────────────────────────────────

    @Test
    fun `scanned product appears in search results after save`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        keepResultsAlive(viewModel, backgroundScope)
        advanceUntilIdle()

        // Save a new scanned product
        viewModel.showAddProductDialog("555555555555")
        viewModel.updateNewProductName("Producto Escaneado Test")
        advanceUntilIdle()
        viewModel.saveNewProduct()
        advanceUntilIdle()

        // Clear the auto-set query and search again
        viewModel.clearSearch()
        advanceUntilIdle()

        viewModel.onQueryChanged("Producto Escaneado Test")
        advanceUntilIdle()

        val results = viewModel.results.first()
        assertTrue(
            "El producto escaneado debe aparecer en los resultados de busqueda",
            results.any { it.name == "Producto Escaneado Test" }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Fake DAO implementations
// ═══════════════════════════════════════════════════════════════════════════

/**
 * In-memory fake for [ProductDao] that avoids Room dependencies.
 */
private class FakeProductDao : ProductDao {

    private val products = mutableListOf<ProductEntity>()

    override suspend fun insertProduct(product: ProductEntity) {
        // REPLACE strategy: remove existing with same barcode, then add
        products.removeAll { it.barcode == product.barcode }
        products.add(product)
    }

    override suspend fun insertProducts(products: List<ProductEntity>) {
        products.forEach { insertProduct(it) }
    }

    override suspend fun getProductByBarcode(barcode: String): ProductEntity? {
        return products.find { it.barcode == barcode }
    }

    override fun searchProducts(query: String): Flow<List<ProductEntity>> {
        val filtered = products.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
        }
        return flowOf(filtered)
    }

    override fun getAllProducts(): Flow<List<ProductEntity>> = flowOf(products.toList())

    override suspend fun getAllProductsOnce(): List<ProductEntity> = products.toList()

    override fun getFeaturedProducts(): Flow<List<ProductEntity>> =
        flowOf(products.filter { it.isFeatured })

    override fun getCategories(): Flow<List<String>> =
        flowOf(products.map { it.category }.distinct().sorted())

    override suspend fun deleteAll() {
        products.clear()
    }
}

/**
 * In-memory fake for [SearchHistoryDao] that avoids Room dependencies.
 */
private class FakeSearchHistoryDao : SearchHistoryDao {

    private val searches = mutableListOf<SearchHistoryEntity>()
    private var nextId = 1L

    override suspend fun insertSearch(search: SearchHistoryEntity) {
        val entity = if (search.id == 0L) {
            search.copy(id = nextId++)
        } else {
            search
        }
        searches.add(0, entity) // most recent first
    }

    override fun getRecentSearches(): Flow<List<SearchHistoryEntity>> =
        flowOf(searches.toList())

    override suspend fun getMostFrequentProduct(since: Long): SearchHistoryEntity? {
        return searches
            .filter { it.timestamp >= since }
            .groupBy { it.query }
            .maxByOrNull { it.value.size }
            ?.value
            ?.first()
    }

    override suspend fun clearAll() {
        searches.clear()
    }

    override suspend fun deleteById(id: Long) {
        searches.removeAll { it.id == id }
    }

    /**
     * Helper for test assertions — returns all stored searches.
     */
    fun getAllSearches(): List<SearchHistoryEntity> = searches.toList()
}
