package com.r1n1os.jetpackcomposetemplateopensource.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.r1n1os.jetpackcomposetemplateopensource.presentation.screens.CreateListScreen
import com.r1n1os.jetpackcomposetemplateopensource.presentation.screens.HomeScreen
import com.r1n1os.jetpackcomposetemplateopensource.presentation.screens.ListDetailScreen
import com.r1n1os.jetpackcomposetemplateopensource.presentation.screens.SearchScreen
import com.r1n1os.jetpackcomposetemplateopensource.presentation.screens.ScannerScreen
import com.r1n1os.jetpackcomposetemplateopensource.ui.theme.JetpackComposeTemplateOpenSourceTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetpackComposeTemplateOpenSourceTheme {
                MyAppNav()
            }
        }
    }
}

@Composable
fun MyAppNav() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(navController = navController) }

            composable(
                route = "search?barcode={barcode}&productName={productName}",
                arguments = listOf(
                    navArgument("barcode") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("productName") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val barcode = backStackEntry.arguments?.getString("barcode") ?: ""
                val productName = backStackEntry.arguments?.getString("productName") ?: ""
                SearchScreen(
                    navController = navController,
                    initialBarcode = barcode,
                    initialProductName = productName
                )
            }

            composable("scanner") { ScannerScreen(navController = navController) }

            composable("create_list") { CreateListScreen(navController = navController) }

            composable(
                route = "list_detail/{listId}",
                arguments = listOf(navArgument("listId") { type = NavType.LongType })
            ) { entry ->
                val listId = entry.arguments?.getLong("listId") ?: return@composable
                ListDetailScreen(navController = navController, listId = listId)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

    val items = listOf(
        BottomNavItem("home", "Inicio", Icons.Filled.Home),
        BottomNavItem("search", "Buscar", Icons.Filled.Search),
        BottomNavItem("scanner", "Escanear", Icons.Filled.QrCodeScanner)
    )

    val currentRoute = navController.currentBackStackEntryAsState()?.value?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { navController.navigate(item.route) }
            )
        }
    }
}