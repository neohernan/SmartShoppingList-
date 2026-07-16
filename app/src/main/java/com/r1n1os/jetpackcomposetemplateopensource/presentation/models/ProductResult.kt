package com.r1n1os.jetpackcomposetemplateopensource.presentation.models

/**
 * Presentation model representing a product search result with price comparison.
 */
data class ProductResult(
    val id: String,
    val name: String,
    val category: String,
    val barcode: String,
    val prices: Map<String, Double>,
    val cheapestStore: String,
    val cheapestPrice: Double,
    val isFeatured: Boolean = false
) {
    /**
     * Returns the price difference between the most expensive and cheapest store.
     */
    val potentialSavings: Double
        get() = if (prices.size >= 2) prices.maxOf { it.value } - cheapestPrice else 0.0
}
