package ru.wb.mapkit.mapcompose.demoapp.ui

import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import ru.wb.mapkit.mapcompose.demoapp.ui.theme.AppTheme

@Composable
internal fun topAppBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = AppTheme.colors.bgLevel1,
    titleContentColor = AppTheme.colors.textPrimary,
    navigationIconContentColor = AppTheme.colors.textPrimary,
    actionIconContentColor = AppTheme.colors.textPrimary,
    scrolledContainerColor = AppTheme.colors.bgLevel1,
)

@Composable
internal fun listItemColors(): ListItemColors = ListItemDefaults.colors(
    containerColor = AppTheme.colors.bgLevel1,
    headlineColor = AppTheme.colors.textPrimary,
    supportingColor = AppTheme.colors.textSecondary
)