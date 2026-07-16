package com.r1n1os.jetpackcomposetemplateopensource.presentation.screens
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// Notifications icon is imported from outlined above
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.ShoppingListEntity
import com.r1n1os.jetpackcomposetemplateopensource.data.mock.MockPriceDataSource
import com.r1n1os.jetpackcomposetemplateopensource.presentation.viewmodels.HomeViewModel
import com.r1n1os.jetpackcomposetemplateopensource.presentation.viewmodels.ShoppingListViewModel
import com.r1n1os.jetpackcomposetemplateopensource.ui.theme.AppColors
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController? = null,
    viewModel: HomeViewModel = hiltViewModel(),
    shoppingListViewModel: ShoppingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lists by shoppingListViewModel.lists.collectAsState(initial = emptyList())

    fun navigateToSearch(query: String = "") {
        val encoded = URLEncoder.encode(query, "UTF-8")
        navController?.navigate("search?barcode=&productName=$encoded")
    }

    fun navigateToProduct(productName: String) {
        val encoded = URLEncoder.encode(productName, "UTF-8")
        navController?.navigate("search?barcode=&productName=$encoded")
    }

    // Gemini dialog
    if (uiState.showGeminiDialog) {
        GeminiDialog(
            input = uiState.geminiInput,
            onInputChanged = viewModel::onGeminiInputChanged,
            result = uiState.geminiResult,
            isLoading = uiState.isGeminiLoading,
            onGenerate = viewModel::generateShoppingList,
            onDismiss = viewModel::dismissGeminiDialog,
            onItemClick = { item -> navigateToSearch(item) }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
        ) {
        // TopAppBar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "¡Buen día!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.MutedForeground,
                        fontSize = 14.sp
                    )
                    Text(
                        text = uiState.userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Foreground
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* Notifications */ }) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notificaciones",
                        tint = AppColors.MutedForeground
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppColors.Background
            )
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.GreenPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Predictive suggestion chip
                uiState.suggestedProductName?.let { productName ->
                    item {
                        SuggestionChip(
                            productName = productName,
                            onClick = { navigateToProduct(productName) }
                        )
                    }
                }

                // Search bar shortcut
                item {
                    SearchBarShortcut(
                        query = uiState.suggestionQuery,
                        onQueryChanged = viewModel::onSuggestionQueryChanged,
                        onClear = viewModel::clearSuggestions,
                        suggestions = uiState.suggestions,
                        onSearchClick = { navigateToSearch() },
                        onSuggestionClick = { suggestion ->
                            viewModel.onSuggestionQueryChanged(suggestion)
                            navigateToSearch(suggestion)
                        }
                    )
                }

                // My lists section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mis Listas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Foreground
                        )
                        TextButton(onClick = { navController?.navigate("create_list") }) {
                            Icon(Icons.Filled.Add, null, Modifier.size(16.dp), tint = AppColors.GreenPrimary)
                            Spacer(Modifier.width(4.dp))
                            Text("Nueva", color = AppColors.GreenPrimary)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                if (lists.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = AppColors.Muted)
                        ) {
                            Text(
                                "No tenés listas aún. Creá una nueva para empezar.",
                                modifier = Modifier.padding(16.dp),
                                color = AppColors.MutedForeground,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    items(lists, key = { it.id }) { list ->
                        ListItemCard(
                            list = list,
                            onClick = { navController?.navigate("list_detail/${list.id}") },
                            onDelete = { shoppingListViewModel.deleteList(list) }
                        )
                    }
                }

                // Categories section
                item {
                    SectionHeader(
                        title = "Categorías",
                        actionText = "Ver todo",
                        onActionClick = { navigateToSearch() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.categories) { category ->
                            CategoryChip(category = category)
                        }
                    }
                }

                // Featured products section
                item {
                    SectionHeader(
                        title = "Productos Destacados",
                        actionText = "Ver más",
                        onActionClick = { navigateToSearch() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.featuredProducts) { product ->
                            FeaturedProductCard(
                                product = product,
                                onClick = { navigateToProduct(product.name) }
                            )
                        }
                    }
                }

                // Recent products section
                item {
                    SectionHeader(
                        title = "Recién Agregados",
                        actionText = "Ver más",
                        onActionClick = { navigateToSearch() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(uiState.recentProducts) { product ->
                    RecentProductItem(
                        product = product,
                        onClick = { navigateToProduct(product.name) }
                    )
                }

                // Bottom spacer
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // FAB to open Gemini AI shopping list generator
        FloatingActionButton(
            onClick = { viewModel.showGeminiDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = AppColors.GreenPrimary,
            contentColor = AppColors.White
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Generar lista con IA",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun ListItemCard(
    list: ShoppingListEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.ShoppingCart, null,
                tint = if (list.isCompleted) AppColors.GreenPrimary else AppColors.MutedForeground,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    list.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.Foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Creada ${java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(java.util.Date(list.createdAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.MutedForeground
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Clear, "Eliminar", tint = AppColors.Destructive, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    productName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.BlueSecondary.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🧠", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Basado en tu historial",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.BlueSecondary
                )
                Text(
                    text = "¿Necesitas $productName?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Foreground
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Ir",
                tint = AppColors.BlueSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun GeminiDialog(
    input: String,
    onInputChanged: (String) -> Unit,
    result: List<String>?,
    isLoading: Boolean,
    onGenerate: () -> Unit,
    onDismiss: () -> Unit,
    onItemClick: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "✨", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Asistente de compras IA", color = MaterialTheme.colorScheme.onSurface)
            }
        },
        text = {
            Column {
                Text(
                    text = "Describí lo que querés cocinar y la IA generará la lista de compras.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChanged,
                    placeholder = { Text("Ej: cena italiana para 4") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.GreenPrimary,
                        unfocusedBorderColor = AppColors.Border,
                        cursorColor = AppColors.GreenPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AppColors.GreenPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                result?.let { items ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Lista generada:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    items.forEach { item ->
                        ChipContent(
                            text = item,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onGenerate,
                enabled = input.isNotBlank() && !isLoading
            ) {
                Text(
                    if (isLoading) "Generando..." else "Generar",
                    color = if (input.isNotBlank() && !isLoading) AppColors.GreenPrimary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}

/**
 * Chip-like item that forces high-contrast solid colors for AI-generated list results.
 * Avoids transparency/theming issues inside AlertDialog content areas.
 */
@Composable
private fun ChipContent(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.InputBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingCart,
                contentDescription = null,
                tint = AppColors.GreenPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Buscar",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SearchBarShortcut(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClear: () -> Unit,
    suggestions: List<String>,
    onSearchClick: () -> Unit,
    onSuggestionClick: (String) -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.InputBackground)
                .clickable { onSearchClick() },
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Explore,
                    contentDescription = "Buscar",
                    tint = AppColors.MutedForeground,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (query.isNotBlank()) query else "Buscar productos...",
                    color = if (query.isNotBlank()) AppColors.Foreground else AppColors.TextTertiary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
            }
        }

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
                            .clickable { onSuggestionClick(suggestion) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Foreground
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Foreground
        )
        if (actionText != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onActionClick() }
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.GreenPrimary,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = AppColors.GreenPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(category: String) {
    val categoryIcons = mapOf(
        "Lácteos" to "🥛",
        "Panadería" to "🍞",
        "Despensa" to "🥫",
        "Carnes" to "🥩",
        "Frutas" to "🍎",
        "Bebidas" to "🥤"
    )

    Card(
        modifier = Modifier.width(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Accent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = categoryIcons[category] ?: "📦",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Foreground,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FeaturedProductCard(
    product: MockPriceDataSource.Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppColors.GreenPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = AppColors.GreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Foreground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = product.category,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.MutedForeground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Best price
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${String.format("%.2f", product.bestPrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.GreenPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "en ${product.bestStore.take(8)}...",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.MutedForeground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RecentProductItem(
    product: MockPriceDataSource.Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
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

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
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

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", product.bestPrice)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.GreenPrimary
                )
                Text(
                    text = "Mejor precio",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.MutedForeground
                )
            }
        }
    }
}
