package com.r1n1os.jetpackcomposetemplateopensource.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.r1n1os.jetpackcomposetemplateopensource.data.local.dao.ProductDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val productDao: ProductDao
) : ViewModel() {

    fun onBarcodeDetected(barcode: String, navController: NavController) {
        viewModelScope.launch {
            val encodedBarcode = java.net.URLEncoder.encode(barcode, "UTF-8")
            // Always pass the barcode — SearchScreen.processBarcodeArg will
            // check all sources (Mock, userProducts, Room) and decide what to do.
            navController.navigate("search?barcode=$encodedBarcode&productName=") {
                popUpTo("home") { inclusive = false }
            }
        }
    }
}