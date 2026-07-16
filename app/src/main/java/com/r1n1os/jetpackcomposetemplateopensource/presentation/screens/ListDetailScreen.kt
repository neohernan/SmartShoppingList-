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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.r1n1os.jetpackcomposetemplateopensource.presentation.viewmodels.ListDetailViewModel
import com.r1n1os.jetpackcomposetemplateopensource.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    navController: NavController,
    listId: Long,
    viewModel: ListDetailViewModel = hiltViewModel()
) {
    val list by viewModel.list.collectAsState()
    val items by viewModel.items.collectAsState()

    LaunchedEffect(listId) { viewModel.loadList(listId) }

    Column(
        modifier = Modifier.fillMaxSize().background(AppColors.Background)
    ) {
        TopAppBar(
            title = { Text(list?.name ?: "Lista", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Background)
        )

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            // Progress
            val checked = items.count { it.isChecked }
            val total = items.size
            Text(
                if (total > 0) "$checked de $total comprados" else "Sin productos",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.MutedForeground
            )
            Spacer(Modifier.height(12.dp))

            if (items.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.ShoppingCart, null, Modifier.size(64.dp), tint = AppColors.MutedForeground)
                    Spacer(Modifier.height(12.dp))
                    Text("La lista está vacía", color = AppColors.MutedForeground)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(items, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.isChecked) AppColors.GreenPrimary.copy(alpha = 0.06f) else AppColors.Card
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.toggleItemChecked(item) }) {
                                    Icon(
                                        if (item.isChecked) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                        if (item.isChecked) "Marcar no comprado" else "Marcar comprado",
                                        tint = if (item.isChecked) AppColors.GreenPrimary else AppColors.MutedForeground,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        item.productName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (item.isChecked) AppColors.MutedForeground else AppColors.Foreground
                                    )
                                }
                                Text("x${item.quantity}", color = AppColors.MutedForeground)
                                IconButton(onClick = { viewModel.deleteItem(item) }) {
                                    Icon(Icons.Filled.Clear, "Eliminar", tint = AppColors.Destructive, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Delete list button
            Button(
                onClick = {
                    viewModel.deleteList()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Destructive)
            ) {
                Icon(Icons.Filled.Clear, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Eliminar lista", color = AppColors.White)
            }
        }
    }
}
