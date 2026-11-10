package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.TerracottaContainer
import com.example.ui.theme.TerracottaOnContainer
import com.example.ui.theme.TerracottaPrimary
import com.example.viewmodel.AppScreen

@Composable
fun BottomNavBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.RECEIPTS,
            onClick = { onNavigate(AppScreen.RECEIPTS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = "Recibos"
                )
            },
            label = { Text("Recibos") },
            modifier = Modifier.testTag("nav_item_receipts"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TerracottaPrimary,
                selectedTextColor = TerracottaPrimary,
                indicatorColor = TerracottaContainer
            )
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.PATIENTS,
            onClick = { onNavigate(AppScreen.PATIENTS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = "Pacientes"
                )
            },
            label = { Text("Pacientes") },
            modifier = Modifier.testTag("nav_item_patients"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TerracottaPrimary,
                selectedTextColor = TerracottaPrimary,
                indicatorColor = TerracottaContainer
            )
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.FINANCIAL,
            onClick = { onNavigate(AppScreen.FINANCIAL) },
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Financeiro"
                )
            },
            label = { Text("Financeiro") },
            modifier = Modifier.testTag("nav_item_financial"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TerracottaPrimary,
                selectedTextColor = TerracottaPrimary,
                indicatorColor = TerracottaContainer
            )
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.SETTINGS,
            onClick = { onNavigate(AppScreen.SETTINGS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ajustes"
                )
            },
            label = { Text("Ajustes") },
            modifier = Modifier.testTag("nav_item_settings"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TerracottaPrimary,
                selectedTextColor = TerracottaPrimary,
                indicatorColor = TerracottaContainer
            )
        )
    }
}
