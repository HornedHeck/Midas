package com.hornedheck.midas.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hornedheck.midas.ui.main.Main
import midas.app.generated.resources.Res
import midas.app.generated.resources.nav_categories
import midas.app.generated.resources.nav_home
import midas.app.generated.resources.nav_settings
import midas.app.generated.resources.nav_transactions
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomNavBar(modifier: Modifier = Modifier) {
    val backStack = LocalNavBackStack.current
    val current = backStack.lastOrNull()

    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(stringResource(Res.string.nav_home)) },
            selected = current is Main.Dashboard,
            onClick = { if (current !is Main.Dashboard) backStack.add(Main.Dashboard) },
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
            label = { Text(stringResource(Res.string.nav_transactions)) },
            selected = current is Main.TransactionsList,
            onClick = { if (current !is Main.TransactionsList) backStack.add(Main.TransactionsList) },
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Category, contentDescription = null) },
            label = { Text(stringResource(Res.string.nav_categories)) },
            selected = current is Main.CategoriesList,
            onClick = { if (current !is Main.CategoriesList) backStack.add(Main.CategoriesList) },
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(stringResource(Res.string.nav_settings)) },
            selected = current is Main.Settings,
            onClick = { if (current !is Main.Settings) backStack.add(Main.Settings) },
        )
    }
}
