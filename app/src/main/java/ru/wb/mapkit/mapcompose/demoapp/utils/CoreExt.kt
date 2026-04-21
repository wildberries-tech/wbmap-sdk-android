package ru.wb.mapkit.mapcompose.demoapp.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import ru.wb.mapkit.mapcompose.demoapp.LocalNavController
import ru.wb.mapkit.mapcompose.demoapp.NavGraphRoute

@Composable
internal inline fun <reified T : ViewModel> navControllerViewModel(): T {
    val navController = LocalNavController.current
    val navGraphRoute = navController.graph.route ?: NavGraphRoute

    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(navGraphRoute)
    }

    return hiltViewModel(parentEntry)
}