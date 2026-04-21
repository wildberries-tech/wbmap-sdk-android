package ru.wb.mapkit.mapcompose.demoapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.wb.mapkit.mapcompose.demoapp.demos.DEMOS
import ru.wb.mapkit.mapcompose.demoapp.demos.Demo
import ru.wb.mapkit.mapcompose.demoapp.styles.StyleSettingsScreen
import ru.wb.mapkit.mapcompose.demoapp.styles.StyleSettingsScreenRoute
import ru.wb.mapkit.mapcompose.demoapp.ui.listItemColors
import ru.wb.mapkit.mapcompose.demoapp.ui.theme.AppTheme
import ru.wb.mapkit.mapcompose.demoapp.ui.theme.darkAppColors
import ru.wb.mapkit.mapcompose.demoapp.ui.theme.lightAppColors
import ru.wb.mapkit.mapcompose.demoapp.ui.topAppBarColors

internal val LocalNavController = staticCompositionLocalOf<NavController> { error("NavController not provided") }
internal const val NavGraphRoute = "root"

@Composable
fun DemoApp(navController: NavHostController = rememberNavController()) {
    val colors = if (isSystemInDarkTheme()) darkAppColors() else lightAppColors()

    AppTheme(colors) {
        CompositionLocalProvider(LocalNavController provides navController) {
            NavHost(
                navController = navController,
                startDestination = "DemoMenu",
                route = NavGraphRoute
            ) {
                composable("DemoMenu") {
                    DemoMenu { demo ->
                        if (demo == null)
                            navController.navigate(StyleSettingsScreenRoute)
                        else
                            navController.navigate(demo.route)
                    }
                }
                DEMOS.forEach { demo -> with(demo) { destination { navController.popBackStack() } } }
                composable(StyleSettingsScreenRoute) { StyleSettingsScreen { navController.popBackStack() } }
            }
        }
    }
}

@Composable
private fun DemoMenu(onDemoClick: (Demo?) -> Unit) {
    val colors = if (isSystemInDarkTheme()) darkAppColors() else lightAppColors()

    AppTheme(colors) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Compose SDK Demos") },
                    colors = topAppBarColors()
                )
            },
            containerColor = AppTheme.colors.bgLevel1,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .testTag("navigation_menu")
                    .verticalScroll(rememberScrollState())
            ) {
                DEMOS.forEach { demo ->
                    ListItem(
                        headlineContent = { Text(text = demo.title) },
                        supportingContent = { Text(text = demo.description) },
                        colors = listItemColors(),
                        modifier = Modifier
                            .testTag(demo.testTag)
                            .clickable { onDemoClick(demo) }
                    )

                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun DemoAppBar(demo: Demo, navigateUp: () -> Unit) {
    var showInfo by remember { mutableStateOf(false) }
    val navController = LocalNavController.current

    TopAppBar(
        title = { Text(demo.title) },
        navigationIcon = {
            IconButton(onClick = navigateUp) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = { showInfo = true }) {
                Icon(imageVector = Icons.Default.Info, contentDescription = "Info")
            }
            IconButton(onClick = { navController.navigate(StyleSettingsScreenRoute) }) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
            }
        },
        colors = topAppBarColors()
    )

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text(text = demo.title) },
            text = { Text(text = demo.description) },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("OK") } },
            containerColor = AppTheme.colors.containerColor,
            titleContentColor = AppTheme.colors.titleContentColor,
            textContentColor = AppTheme.colors.textContentColor,
        )
    }
}