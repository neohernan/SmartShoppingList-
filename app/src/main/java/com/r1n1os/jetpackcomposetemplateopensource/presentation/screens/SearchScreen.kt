package com.r1n1os.jetpackcomposetemplateopensource.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.r1n1os.jetpackcomposetemplateopensource.presentation.models.ProductResult
import com.r1n1os.jetpackcomposetemplateopensource.presentation.viewmodels.SearchViewModel
import com.r1n1os.jetpackcomposetemplateopensource.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController? = null,
    viewModel: SearchViewModel = hiltViewModel(),
    initialBarcode: String = "",
    initialProductName: String = "",
    onProductClick: (ProductResult) -> Unit = {}
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val dialogState by viewModel.showAddDialog.collectAsState()

    // ── Process navigation arguments once ───────────────────────────────
    LaunchedEffect(initialBarcode, initialProductName) {
        if (initialProductName.isNotBlank()) {
            viewModel.onQueryChanged(initialProductName)
        } else if (initialBarcode.isNotBlank()) {
            // Suspend call — first checks Room, then MockPriceDataSource
            val found = viewModel.processBarcodeArg(initialBarcode)
            if (!found) {
                viewModel.showAddProductDialog(initialBarcode)
            }
        }
    }

    // ── Add-product dialog ──────────────────────────────────────────────
    dialogState?.let { (barcode, name) ->
        AddProductDialog(
            barcode = barcode,
            name = name,
            onNameChanged = viewModel::updateNewProductName,
            onDismiss = { viewModel.dismissAddProductDialog() },
            onConfirm = { viewModel.saveNewProduct() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Buscar Productos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Foreground
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppColors.Background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Search input field
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Buscar productos, categorías...",
                        color = AppColors.TextTertiary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Buscar",
                        tint = AppColors.MutedForeground
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearSearch) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Limpiar",
                                tint = AppColors.MutedForeground
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.GreenPrimary,
                    unfocusedBorderColor = AppColors.Border,
                    cursorColor = AppColors.GreenPrimary,
                    focusedContainerColor = AppColors.White,
                    unfocusedContainerColor = AppColors.InputBackground
                ),
                singleLine = true
            )

            // Suggestions dropdown
            AnimatedVisibility(
                visible = suggestions.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    suggestions.forEach { suggestion ->
                        Text(
                            text = suggestion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onSuggestionSelected(suggestion) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Foreground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.onQueryChanged(category) },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.GreenPrimary,
                            selectedLabelColor = AppColors.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results
            if (results.isEmpty() && query.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = AppColors.MutedForeground,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No se encontraron productos",
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppColors.MutedForeground
                        )
                        Text(
                            text = "Intenta con otro término de búsqueda",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextTertiary
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (query.isBlank()) "${results.size} productos"
                        else "${results.size} resultados para \"$query\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.MutedForeground
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(results, key = { it.id }) { product ->
                        ProductResultCard(
                            product = product,
                            onClick = {
                                viewModel.selectProduct(product.name)
                                onProductClick(product)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddProductDialog(
    barcode: String,
    name: String,
    onNameChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo producto") },
        text = {
            Column {
                Text(
                    text = "Código de barras: $barcode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.MutedForeground
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChanged,
                    label = { Text("Nombre del producto") },
                    placeholder = { Text("Ej: Leche Deslactosada 1L") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.GreenPrimary,
                        unfocusedBorderColor = AppColors.Border,
                        cursorColor = AppColors.GreenPrimary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = name.isNotBlank()
            ) {
                Text(
                    "Guardar",
                    color = if (name.isNotBlank()) AppColors.GreenPrimary
                    else AppColors.MutedForeground
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = AppColors.MutedForeground)
            }
        }
    )
}

@Composable
private fun ProductResultCard(
    product: ProductResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Muted),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        tint = AppColors.GreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.Foreground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.MutedForeground
                    )
                }

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.GreenPrimary.copy(alpha = 0.12f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$${String.format("%.2f", product.cheapestPrice)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.GreenPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "¡Oferta!",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.GreenPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Comparativa de precios",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = AppColors.Foreground
            )

            Spacer(modifier = Modifier.height(8.dp))

            product.prices.forEach { (storeName, price) ->
                val isCheapest = price == product.cheapestPrice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Store,
                            contentDescription = null,
                            tint = if (isCheapest) AppColors.GreenPrimary
                            else AppColors.MutedForeground,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = storeName,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCheapest) AppColors.Foreground
                            else AppColors.MutedForeground,
                            fontWeight = if (isCheapest) FontWeight.Medium
                            else FontWeight.Normal
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$${String.format("%.2f", price)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isCheapest) FontWeight.Bold
                            else FontWeight.Normal,
                            color = if (isCheapest) AppColors.GreenPrimary
                            else AppColors.Foreground
                        )
                        if (isCheapest) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.LocalOffer,
                                contentDescription = "Mejor precio",
                                tint = AppColors.GreenPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            if (product.potentialSavings > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.GreenPrimary.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalOffer,
                            contentDescription = null,
                            tint = AppColors.GreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Compra en ${product.cheapestStore} y ahorra " +
                                    "$${String.format("%.2f", product.potentialSavings)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.GreenDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}