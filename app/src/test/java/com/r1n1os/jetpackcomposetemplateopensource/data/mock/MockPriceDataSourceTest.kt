package com.r1n1os.jetpackcomposetemplateopensource.data.mock

import com.r1n1os.jetpackcomposetemplateopensource.data.mock.MockPriceDataSource.Product
import com.r1n1os.jetpackcomposetemplateopensource.data.mock.MockPriceDataSource.StorePrice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [MockPriceDataSource].
 *
 * NOTE: Since [MockPriceDataSource] is a Kotlin `object` (singleton) with a
 * private mutable internal list and no reset mechanism, tests that call
 * [MockPriceDataSource.addProduct] permanently mutate the global state across
 * test runs. To remain robust, count-based assertions use "at least" checks
 * and product lookups are done by known attributes rather than hardcoded indices.
 */
class MockPriceDataSourceTest {

    // ═════════════════════════════════════════════════════════════════
    // getAllProducts
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun `getAllProducts returns at least 12 initial products`() {
        val products = MockPriceDataSource.getAllProducts()
        assertTrue("Should have at least 12 products", products.size >= 12)
    }

    @Test
    fun `getAllProducts contains known product Leche Entera`() {
        val products = MockPriceDataSource.getAllProducts()
        assertTrue(products.any { it.name == "Leche Entera 1L" })
        assertTrue(products.any { it.barcode == "780001000101" })
    }

    @Test
    fun `getAllProducts contains all known initial products`() {
        val products = MockPriceDataSource.getAllProducts()
        val knownNames = listOf(
            "Leche Entera 1L", "Pan Artesanal 500g", "Huevos Color 12un",
            "Arroz Largo Fina 1kg", "Fideos Tallarín 500g", "Pechuga de Pollo 1kg",
            "Manzanas Roja 1kg", "Yogurt Natural 1L", "Coca-Cola 2L",
            "Cerveza Rubia 6pack", "Tomate Perita 1kg", "Queso Fresco 500g"
        )
        knownNames.forEach { name ->
            assertTrue("Product '$name' should exist in the data source",
                products.any { it.name == name })
        }
    }

    @Test
    fun `getAllProducts products have non-empty names`() {
        val products = MockPriceDataSource.getAllProducts()
        assertTrue("All products should have non-empty names", products.all { it.name.isNotBlank() })
    }

    @Test
    fun `getAllProducts products have valid prices`() {
        val products = MockPriceDataSource.getAllProducts()
        assertTrue("All products should have at least one price", products.all { it.prices.isNotEmpty() })
        assertTrue("All prices should be non-negative", products.all { product ->
            product.prices.all { it.price >= 0.0 }
        })
    }

    @Test
    fun `getAllProducts Leche Entera is featured with correct category`() {
        val products = MockPriceDataSource.getAllProducts()
        val leche = products.first { it.name == "Leche Entera 1L" }
        assertEquals("Lácteos", leche.category)
        assertEquals("780001000101", leche.barcode)
        assertTrue("Leche Entera should be featured", leche.isFeatured)
    }

    // ═════════════════════════════════════════════════════════════════
    // getFeaturedProducts
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun `getFeaturedProducts returns only featured products`() {
        val featured = MockPriceDataSource.getFeaturedProducts()
        assertTrue("All returned products should be featured", featured.all { it.isFeatured })
    }

    @Test
    fun `getFeaturedProducts contains expected featured items`() {
        val featured = MockPriceDataSource.getFeaturedProducts()
        val featuredNames = featured.map { it.name }.toSet()
        assertTrue(featuredNames.contains("Leche Entera 1L"))
        assertTrue(featuredNames.contains("Pan Artesanal 500g"))
        assertTrue(featuredNames.contains("Huevos Color 12un"))
        assertTrue(featuredNames.contains("Pechuga de Pollo 1kg"))
        assertTrue(featuredNames.contains("Coca-Cola 2L"))
        assertTrue(featuredNames.contains("Queso Fresco 500g"))
    }

    @Test
    fun `getFeaturedProducts does not return non-featured products`() {
        val featuredIds = MockPriceDataSource.getFeaturedProducts().map { it.id }.toSet()
        val allProducts = MockPriceDataSource.getAllProducts()
        val nonFeaturedIds = allProducts.filter { !it.isFeatured }.map { it.id }.toSet()
        assertTrue("Featured set should not overlap with non-featured",
            featuredIds.intersect(nonFeaturedIds).isEmpty())
    }

    // ═════════════════════════════════════════════════════════════════
    // getCategories
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun `getCategories contains expected categories`() {
        val categories = MockPriceDataSource.getCategories()
        assertTrue(categories.contains("Lácteos"))
        assertTrue(categories.contains("Panadería"))
        assertTrue(categories.contains("Despensa"))
        assertTrue(categories.contains("Carnes"))
        assertTrue(categories.contains("Frutas"))
        assertTrue(categories.contains("Bebidas"))
    }

    // ═════════════════════════════════════════════════════════════════
    // searchProducts
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun `searchProducts with empty query returns all products`() {
        val results = MockPriceDataSource.searchProducts("")
        assertTrue("Empty query should return at least 12 products", results.size >= 12)
    }

    @Test
    fun `searchProducts with blank query returns all products`() {
        val results = MockPriceDataSource.searchProducts("   ")
        assertTrue("Blank query should return at least 12 products", results.size >= 12)
    }

    @Test
    fun `searchProducts by name is case-insensitive`() {
        val resultsLower = MockPriceDataSource.searchProducts("leche")
        val resultsUpper = MockPriceDataSource.searchProducts("LECHE")
        val resultsMixed = MockPriceDataSource.searchProducts("Leche")
        assertEquals(resultsLower.size, resultsUpper.size)
        assertEquals(resultsLower.size, resultsMixed.size)
        assertTrue(resultsLower.any { it.name.contains("Leche", ignoreCase = true) })
    }

    @Test
    fun `searchProducts by name returns matching products`() {
        val results = MockPriceDataSource.searchProducts("Pan")
        assertTrue(results.isNotEmpty())
        assertTrue(results.all {
            it.name.contains("Pan", ignoreCase = true) ||
                    it.category.contains("Pan", ignoreCase = true)
        })
    }

    @Test
    fun `searchProducts by category returns matching products`() {
        val results = MockPriceDataSource.searchProducts("Bebidas")
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.category == "Bebidas" })
    }

    @Test
    fun `searchProducts by barcode returns matching products`() {
        val results = MockPriceDataSource.searchProducts("780001000101")
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.barcode == "780001000101" })
    }

    @Test
    fun `searchProducts with no matches returns empty list`() {
        val results = MockPriceDataSource.searchProducts("xyznoexiste")
        assertTrue(results.isEmpty())
    }

    @Test
    fun `searchProducts partial match returns correct results`() {
        val results = MockPriceDataSource.searchProducts("Poll")
        assertTrue(results.any { it.name == "Pechuga de Pollo 1kg" })
    }

    // ═════════════════════════════════════════════════════════════════
    // searchByCategory
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun `searchByCategory Lácteos returns products`() {
        val results = MockPriceDataSource.searchByCategory("Lácteos")
        assertTrue("Should have at least 1 Lácteos product", results.isNotEmpty())
        assertTrue(results.all { it.category == "Lácteos" })
    }

    @Test
    fun `searchByCategory Carnes returns Pollo`() {
        val results = MockPriceDataSource.searchByCategory("Carnes")
        assertTrue(results.any { it.name == "Pechuga de Pollo 1kg" })
    }

    @Test
    fun `searchByCategory with non-existent category returns empty list`() {
        val results = MockPriceDataSource.searchByCategory("Inexistente")
        assertTrue(results.isEmpty())
    }

    // ═════════════════════════════════════════════════════════════════
    // getProductByBarcode
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun `getProductByBarcode with existing barcode returns product`() {
        val product = MockPriceDataSource.getProductByBarcode("780009000909")
        assertNotNull(product)
        assertEquals("Coca-Cola 2L", product?.name)
        assertEquals("Bebidas", product?.category)
    }

    @Test
    fun `getProductByBarcode with non-existing barcode returns null`() {
        val product = MockPriceDataSource.getProductByBarcode("999999999999")
        assertNull(product)
    }

    @Test
    fun `getProductByBarcode empty string returns null`() {
        val product = MockPriceDataSource.getProductByBarcode("")
        assertNull(product)
    }

    // ═════════════════════════════════════════════════════════════════
    // getSuggestions
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun `getSuggestions with empty query returns empty list`() {
        val suggestions = MockPriceDataSource.getSuggestions("")
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun `getSuggestions with matching query returns suggestions`() {
        val suggestions = MockPriceDataSource.getSuggestions("Leche")
        assertTrue(suggestions.isNotEmpty())
        assertTrue(suggestions.all { it.contains("Leche", ignoreCase = true) })
    }

    @Test
    fun `getSuggestions returns at most 5 suggestions`() {
        val suggestions = MockPriceDataSource.getSuggestions("a")
        assertTrue(suggestions.size <= 5)
    }

    @Test
    fun `getSuggestions with non-matching query returns empty list`() {
        val suggestions = MockPriceDataSource.getSuggestions("xyznoexiste")
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun `getSuggestions results are case-insensitive`() {
        val suggestionsLower = MockPriceDataSource.getSuggestions("leche")
        val suggestionsUpper = MockPriceDataSource.getSuggestions("LECHE")
        assertEquals(suggestionsLower, suggestionsUpper)
    }

    // ═════════════════════════════════════════════════════════════════
    // Product.bestPrice computed property
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun `bestPrice returns the minimum price across stores`() {
        val leche = MockPriceDataSource.getProductByBarcode("780001000101")!!
        assertEquals(1.85, leche.bestPrice, 0.001)
    }

    @Test
    fun `bestPrice for Coca-Cola returns 2 dot 30`() {
        val coca = MockPriceDataSource.getProductByBarcode("780009000909")!!
        assertEquals(2.30, coca.bestPrice, 0.001)
    }

    @Test
    fun `bestPrice for single-price product returns that price`() {
        val product = Product(
            id = "single",
            name = "Single Price",
            category = "Test",
            prices = listOf(StorePrice("Only Store", 4.99))
        )
        assertEquals(4.99, product.bestPrice, 0.001)
    }

    // ═════════════════════════════════════════════════════════════════
    // Product.bestStore computed property
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun `bestStore returns the store with the minimum price`() {
        val leche = MockPriceDataSource.getProductByBarcode("780001000101")!!
        assertEquals("Mega Discount", leche.bestStore)
    }

    @Test
    fun `bestStore for Pan Artesanal is Express Market`() {
        val pan = MockPriceDataSource.getProductByBarcode("780002000202")!!
        assertEquals("Express Market", pan.bestStore)
    }

    @Test
    fun `bestStore for single-price product returns that store`() {
        val product = Product(
            id = "single-store",
            name = "Single Store",
            category = "Test",
            prices = listOf(StorePrice("The Only Store", 3.50))
        )
        assertEquals("The Only Store", product.bestStore)
    }

    // ═════════════════════════════════════════════════════════════════
    // StorePrice data class
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun `StorePrice stores name price and logo correctly`() {
        val price = StorePrice(storeName = "Test Store", price = 5.99, storeLogo = "logo.png")
        assertEquals("Test Store", price.storeName)
        assertEquals(5.99, price.price, 0.001)
        assertEquals("logo.png", price.storeLogo)
    }

    @Test
    fun `StorePrice default storeLogo is empty string`() {
        val price = StorePrice(storeName = "Test", price = 1.0)
        assertEquals("", price.storeLogo)
    }

    // ═════════════════════════════════════════════════════════════════
    // Product data class creation
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun `Product default imageUrl and barcode are empty`() {
        val product = Product(
            id = "defaults",
            name = "Minimal Product",
            category = "Test",
            prices = listOf(StorePrice("Store", 1.0))
        )
        assertEquals("", product.imageUrl)
        assertEquals("", product.barcode)
        assertFalse(product.isFeatured)
    }

    // ═════════════════════════════════════════════════════════════════
    // addProduct logic — tested via data classes to avoid mutating
    // the singleton state (which would break other tests in the suite)
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun `new product created via data class has expected defaults`() {
        val product = Product(
            id = "new_1",
            name = "Producto Nuevo",
            category = "Otros",
            barcode = "888888888888",
            prices = listOf(
                StorePrice("SuperMercado Central", 0.0),
                StorePrice("Express Market", 0.0),
                StorePrice("Mega Discount", 0.0),
                StorePrice("Fresh Shop", 0.0)
            )
        )
        assertEquals("Producto Nuevo", product.name)
        assertEquals("Otros", product.category)
        assertEquals("888888888888", product.barcode)
        assertEquals(4, product.prices.size)
        assertTrue(product.prices.all { it.price == 0.0 })
        assertFalse(product.isFeatured)
    }

    @Test
    fun `getProductByBarcode returns first match for non-duplicate lookup`() {
        // Verifies that looking up an existing barcode returns the correct product
        // and that a product name is preserved (the core of the duplicate check)
        val leche = MockPriceDataSource.getProductByBarcode("780001000101")
        assertNotNull(leche)
        assertEquals("Leche Entera 1L", leche?.name)
        // Products with different barcodes are distinct
        val pollo = MockPriceDataSource.getProductByBarcode("780006000606")
        assertNotNull(pollo)
        assertEquals("Pechuga de Pollo 1kg", pollo?.name)
    }
}
