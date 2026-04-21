package ru.wb.mapkit.mapcompose.demoapp.styles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.wb.mapkit.mapcompose.StyleProvider
import ru.wb.mapkit.mapcompose.demoapp.MainViewModel
import ru.wb.mapkit.mapcompose.demoapp.ui.theme.AppTheme
import ru.wb.mapkit.mapcompose.demoapp.ui.topAppBarColors
import ru.wb.mapkit.mapcompose.demoapp.utils.navControllerViewModel

internal const val StyleSettingsScreenRoute = "StyleSettingsScreen"

@Composable
internal fun StyleSettingsScreen(
    navigateUp: () -> Unit = {},
) {
    val viewModel: MainViewModel = navControllerViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Стиль карты") },
                navigationIcon = {
                    IconButton(onClick = navigateUp) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = topAppBarColors()
            )
        },
        containerColor = AppTheme.colors.bgLevel1,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .weight(1f)
            ) {
                items(
                    items = state.allStyles,
                    key = { it.id }
                ) { style ->
                    StyleListItem(
                        style = style,
                        isSelected = (style == state.selectedStyle),
                        onSelect = {
                            viewModel.handleIntent(StyleSettingsIntent.SelectStyle(style))
                        },
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            ) {
                Button(
                    onClick = { viewModel.handleIntent(StyleSettingsIntent.ShowAddDialog) },
                    colors = AppTheme.colors.buttonPrimary
                ) {
                    Text("Добавить")
                }
                Button(
                    onClick = { viewModel.handleIntent(StyleSettingsIntent.ShowDeleteDialog) },
                    enabled = with(state.selectedStyle) { this != null && !isBuiltIn() },
                    colors = AppTheme.colors.buttonSecondary,
                ) {
                    Text("Удалить")
                }
            }
        }
    }

    if (state.showAddDialog) {
        AddStyleDialog(
            onDismiss = {
                viewModel.handleIntent(StyleSettingsIntent.HideAddDialog)
            },
            onConfirm = { name, url ->
                viewModel.handleIntent(StyleSettingsIntent.AddCustomStyle(name, url))
            }
        )
    }

    if (state.showDeleteDialog) {
        val selectedStyle = state.selectedStyle ?: return

        DeleteStyleDialog(
            style = selectedStyle,
            onDismiss = {
                viewModel.handleIntent(StyleSettingsIntent.HideDeleteDialog)
            },
            onDelete = { style ->
                viewModel.handleIntent(StyleSettingsIntent.DeleteCustomStyle(style))
            }
        )
    }
}

@Composable
private fun StyleListItem(
    style: MapStyle,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = AppTheme.colors.buttonPrimary.containerColor),
        )
        Text(text = style.name)
    }
    HorizontalDivider(thickness = Dp.Hairline)
}

@Composable
private fun AddStyleDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, url: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var urlError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Добавить стиль")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Наименование") },
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        urlError = false
                    },
                    label = { Text("URL стиля") },
                    isError = urlError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    urlError = url.isBlank()
                    nameError = name.isBlank()
                    if (urlError || nameError) return@TextButton
                    onConfirm(name, url)
                }
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        containerColor = AppTheme.colors.bgLevel1
    )
}

@Composable
private fun DeleteStyleDialog(
    style: MapStyle,
    onDismiss: () -> Unit,
    onDelete: (MapStyle) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Удалить стиль ${style.name}?")
        },
        confirmButton = {
            TextButton(onClick = { onDelete(style) }) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        containerColor = AppTheme.colors.bgLevel1
    )
}

private fun MapStyle.isBuiltIn() = (this.provider is StyleProvider.WB)

@Composable
@Preview
private fun Preview() = StyleSettingsScreen()