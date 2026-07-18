package com.r1n1os.jetpackcomposetemplateopensource.presentation.screens

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.r1n1os.jetpackcomposetemplateopensource.data.local.dao.ProductDao
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.ProductEntity
import com.r1n1os.jetpackcomposetemplateopensource.presentation.viewmodels.ScannerViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for [ScannerScreen].
 *
 * Camera permission is pre-granted via [GrantPermissionRule] to avoid
 * the system permission dialog blocking the test. The tests verify that
 * the camera preview UI is rendered without actually testing CameraX
 * functionality (which requires real hardware).
 */
@RunWith(AndroidJUnit4::class)
class ScannerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val cameraPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @Test
    fun cameraPreview_flashButton_isDisplayed() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            ScannerScreen(
                navController = navController,
                viewModel = ScannerViewModel(
                    productDao = FakeProductDao()
                )
            )
        }

        // The flash button is always visible when camera permission is granted
        composeTestRule.onNodeWithText("Flash On")
            .assertIsDisplayed()
    }

    @Test
    fun cameraPreview_viewModel_isCreated() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            ScannerScreen(
                navController = navController,
                viewModel = ScannerViewModel(
                    productDao = FakeProductDao()
                )
            )
        }

        // Verify the screen does not crash and renders content
        composeTestRule.onNodeWithText("Flash On")
            .assertExists()
    }
}

/**
 * In-memory fake for [ProductDao] used by [ScannerViewModel].
 */
class FakeProductDao : ProductDao {

    private val products = mutableListOf<ProductEntity>()

    override suspend fun insertProduct(product: ProductEntity) {
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
