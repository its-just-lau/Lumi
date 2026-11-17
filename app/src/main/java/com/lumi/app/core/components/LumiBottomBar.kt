package com.lumi.app.core.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Checklist
import com.lumi.app.ui.theme.LumiColors

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun LumiBottomBar(
    selectedRoute: String,
    onItemSelected: (String) -> Unit
) {

    val items = listOf(
        BottomNavItem("Inicio", Icons.Default.Home, "home"),
        BottomNavItem("Materias", Icons.Default.Book, "subjects"),
        BottomNavItem("Tareas", Icons.Default.Checklist, "tasks"),
        BottomNavItem("Estudio", Icons.Default.Timer, "study"),
        BottomNavItem("Tienda", Icons.Default.ShoppingCart, "store"),
        BottomNavItem("Perfil", Icons.Default.Person, "profile")
    )

    NavigationBar(
        containerColor = LumiColors.White,
        contentColor = LumiColors.Dark
    ) {
        items.forEach { item ->

            NavigationBarItem(
                selected = selectedRoute == item.route,

                onClick = {
                    onItemSelected(item.route)
                },

                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },

                label = {
                    Text(item.title)
                },

                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = LumiColors.Dark,
                    selectedTextColor = LumiColors.Dark,
                    indicatorColor = LumiColors.Yellow,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}