package com.r1n1os.jetpackcomposetemplateopensource.data.mock

/**
 * Mock data source that simulates product prices across different supermarkets.
 * This follows the app's concept of comparing prices across local supermarkets
 * to generate the best shopping list.
 */
object MockPriceDataSource {

    data class StorePrice(
        val storeName: String,
        val price: Double,
        val storeLogo: String = ""
    )

    data class Product(
        val id: String,
        val name: String,
        val category: String,
        val imageUrl: String = "",
        val barcode: String = "",
        val prices: List<StorePrice>,
        val isFeatured: Boolean = false
    ) {
        val bestPrice: Double
            get() = prices.minOf { it.price }

        val bestStore: String
            get() = prices.minByOrNull { it.price }?.storeName ?: ""
    }

    private val stores = listOf("SuperMercado Central", "Express Market", "Mega Discount", "Fresh Shop")

    private val products = mutableListOf(
        Product(
            id = "1",
            name = "Leche Entera 1L",
            category = "Lácteos",
            barcode = "780001000101",
            isFeatured = true,
            prices = listOf(
                StorePrice("SuperMercado Central", 1.99),
                StorePrice("Express Market", 2.15),
                StorePrice("Mega Discount", 1.85),
                StorePrice("Fresh Shop", 2.05)
            )
        ),
        Product(
            id = "2",
            name = "Pan Artesanal 500g",
            category = "Panadería",
            barcode = "780002000202",
            isFeatured = true,
            prices = listOf(
                StorePrice("SuperMercado Central", 3.50),
                StorePrice("Express Market", 3.20),
                StorePrice("Mega Discount", 3.75),
                StorePrice("Fresh Shop", 3.40)
            )
        ),
        Product(
            id = "3",
            name = "Huevos Color 12un",
            category = "Lácteos",
            barcode = "780003000303",
            isFeatured = true,
            prices = listOf(
                StorePrice("SuperMercado Central", 4.20),
                StorePrice("Express Market", 4.50),
                StorePrice("Mega Discount", 3.95),
                StorePrice("Fresh Shop", 4.10)
            )
        ),
        Product(
            id = "4",
            name = "Arroz Largo Fina 1kg",
            category = "Despensa",
            barcode = "780004000404",
            isFeatured = false,
            prices = listOf(
                StorePrice("SuperMercado Central", 2.80),
                StorePrice("Express Market", 2.65),
                StorePrice("Mega Discount", 2.50),
                StorePrice("Fresh Shop", 2.90)
            )
        ),
        Product(
            id = "5",
            name = "Fideos Tallarín 500g",
            category = "Despensa",
            barcode = "780005000505",
            isFeatured = false,
            prices = listOf(
                StorePrice("SuperMercado Central", 1.75),
                StorePrice("Express Market", 1.90),
                StorePrice("Mega Discount", 1.60),
                StorePrice("Fresh Shop", 1.85)
            )
        ),
        Product(
            id = "6",
            name = "Pechuga de Pollo 1kg",
            category = "Carnes",
            barcode = "780006000606",
            isFeatured = true,
            prices = listOf(
                StorePrice("SuperMercado Central", 8.50),
                StorePrice("Express Market", 8.90),
                StorePrice("Mega Discount", 7.95),
                StorePrice("Fresh Shop", 8.20)
            )
        ),
        Product(
            id = "7",
            name = "Manzanas Roja 1kg",
            category = "Frutas",
            barcode = "780007000707",
            isFeatured = false,
            prices = listOf(
                StorePrice("SuperMercado Central", 3.20),
                StorePrice("Express Market", 3.50),
                StorePrice("Mega Discount", 2.99),
                StorePrice("Fresh Shop", 3.10)
            )
        ),
        Product(
            id = "8",
            name = "Yogurt Natural 1L",
            category = "Lácteos",
            barcode = "780008000808",
            isFeatured = false,
            prices = listOf(
                StorePrice("SuperMercado Central", 3.10),
                StorePrice("Express Market", 3.30),
                StorePrice("Mega Discount", 2.95),
                StorePrice("Fresh Shop", 3.00)
            )
        ),
        Product(
            id = "9",
            name = "Coca-Cola 2L",
            category = "Bebidas",
            barcode = "780009000909",
            isFeatured = true,
            prices = listOf(
                StorePrice("SuperMercado Central", 2.50),
                StorePrice("Express Market", 2.70),
                StorePrice("Mega Discount", 2.30),
                StorePrice("Fresh Shop", 2.60)
            )
        ),
        Product(
            id = "10",
            name = "Cerveza Rubia 6pack",
            category = "Bebidas",
            barcode = "780010001010",
            isFeatured = false,
            prices = listOf(
                StorePrice("SuperMercado Central", 7.20),
                StorePrice("Express Market", 7.50),
                StorePrice("Mega Discount", 6.90),
                StorePrice("Fresh Shop", 7.00)
            )
        ),
        Product(
            id = "11",
            name = "Tomate Perita 1kg",
            category = "Frutas",
            barcode = "780011001111",
            isFeatured = false,
            prices = listOf(
                StorePrice("SuperMercado Central", 2.40),
                StorePrice("Express Market", 2.60),
                StorePrice("Mega Discount", 2.20),
                StorePrice("Fresh Shop", 2.50)
            )
        ),
        Product(
            id = "12",
            name = "Queso Fresco 500g",
            category = "Lácteos",
            barcode = "780012001212",
            isFeatured = true,
            prices = listOf(
                StorePrice("SuperMercado Central", 5.50),
                StorePrice("Express Market", 5.80),
                StorePrice("Mega Discount", 5.20),
                StorePrice("Fresh Shop", 5.40)
            )
        ),
    )

    private val categories = listOf(
        "Lácteos", "Panadería", "Despensa", "Carnes", "Frutas", "Bebidas"
    )

    fun getAllProducts(): List<Product> = products

    fun getFeaturedProducts(): List<Product> = products.filter { it.isFeatured }

    fun getCategories(): List<String> = categories

    fun searchProducts(query: String): List<Product> {
        if (query.isBlank()) return products
        return products.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true) ||
                    it.barcode.contains(query, ignoreCase = true)
        }
    }

    fun searchByCategory(category: String): List<Product> =
        products.filter { it.category == category }

    fun getProductByBarcode(barcode: String): Product? =
        products.find { it.barcode == barcode }

    fun getSuggestions(query: String): List<String> {
        if (query.isBlank()) return emptyList()
        return products.map { it.name }
            .filter { it.contains(query, ignoreCase = true) }
            .take(5)
    }

    /**
     * Adds a new product dynamically (e.g. from barcode scan).
     * Uses the next available id and default pricing from all stores.
     */
    fun addProduct(name: String, barcode: String, category: String = "Otros") {
        // Avoid duplicates
        if (products.any { it.barcode == barcode }) return

        val nextId = (products.size + 1).toString()
        val defaultPrices = stores.map { store ->
            StorePrice(storeName = store, price = 0.0)
        }
        products.add(
            Product(
                id = nextId,
                name = name,
                category = category,
                barcode = barcode,
                prices = defaultPrices,
                isFeatured = false
            )
        )
    }
}
