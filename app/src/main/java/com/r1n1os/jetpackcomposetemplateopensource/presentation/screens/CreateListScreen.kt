package com.r1n1os.jetpackcomposetemplateopensource.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.r1n1os.jetpackcomposetemplateopensource.presentation.viewmodels.CreateListViewModel
import com.r1n1os.jetpackcomposetemplateopensource.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListScreen(
    navController: NavController,
    viewModel: CreateListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.savedListId) {
        if (uiState.savedListId != null) {
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(AppColors.Background)
    ) {
        TopAppBar(
            title = { Text("Nueva lista", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Volver")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Background)
        )

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            // List name
            OutlinedTextField(
                value = uiState.listName,
                onValueChange = viewModel::onListNameChanged,
                label = { Text("Nombre de la lista") },
                placeholder = { Text("Ej: Compras semanales") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.GreenPrimary,
                    unfocusedBorderColor = AppColors.Border,
                    cursorColor = AppColors.GreenPrimary
                )
            )

            Spacer(Modifier.height(16.dp))

            // Add item row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.newItemName,
                    onValueChange = viewModel::onNewItemNameChanged,
                    placeholder = { Text("Producto") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.GreenPrimary,
                        unfocusedBorderColor = AppColors.Border,
                        cursorColor = AppColors.GreenPrimary
                    )
                )
                OutlinedTextField(
                    value = uiState.newItemQuantity,
                    onValueChange = viewModel::onNewItemQuantityChanged,
                    placeholder = { Text("Cant") },
                    modifier = Modifier.width(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.GreenPrimary,
                        unfocusedBorderColor = AppColors.Border,
                        cursorColor = AppColors.GreenPrimary
                    )
                )
                IconButton(onClick = { viewModel.addItem() }) {
                    Icon(Icons.Filled.Add, "Agregar", tint = AppColors.GreenPrimary)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Gemini button
            OutlinedButton(
                onClick = { viewModel.showGeminiDialog() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Star, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Generar con IA")
            }

            Spacer(Modifier.height(12.dp))

            // Items list
            Text(
                "${uiState.items.size} productos",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.MutedForeground
            )
            Spacer(Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(uiState.items) { index, item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Muted),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.productName, modifier = Modifier.weight(1f))
                            Text("x${item.quantity}", color = AppColors.MutedForeground)
                            IconButton(onClick = { viewModel.removeItem(index) }) {
                                Icon(Icons.Filled.Clear, "Eliminar", tint = AppColors.Destructive, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Save button
            Button(
                onClick = { viewModel.saveList() },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.GreenPrimary),
                enabled = uiState.listName.isNotBlank()
            ) {
                Text("Guardar lista", color = AppColors.White)
            }
        }
    }

    // Gemini dialog
    if (uiState.showGeminiDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissGeminiDialog() },
            title = { Text("✨ Generar con IA", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    Text("Describí lo que querés cocinar:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.geminiInput,
                        onValueChange = viewModel::onGeminiInputChanged,
                        placeholder = { Text("Ej: cena italiana para 4") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.GreenPrimary,
                            unfocusedBorderColor = AppColors.Border
                        )
                    )
                    if (uiState.isGeminiLoading) {
                        Spacer(Modifier.height(12.dp))
                        CircularProgressIndicator(color = AppColors.GreenPrimary, modifier = Modifier.size(32.dp))
                    }
                    uiState.geminiError?.let { error ->
                        Spacer(Modifier.height(8.dp))
                        Text(error, color = AppColors.Destructive, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.generateWithGemini() }, enabled = uiState.geminiInput.isNotBlank() && !uiState.isGeminiLoading) {
                    Text("Generar", color = AppColors.GreenPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissGeminiDialog() }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurface) }
            }
        )
    }
}
