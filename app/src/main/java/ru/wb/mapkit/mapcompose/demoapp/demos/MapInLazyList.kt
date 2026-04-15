package ru.wb.mapkit.mapcompose.demoapp.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import ru.wb.mapkit.mapcompose.demoapp.LocalNavController
import ru.wb.mapkit.mapcompose.demoapp.ui.theme.AppTheme

object MapInLazyList : Demo() {
    override val route: String = this.javaClass.name
    override val title = "Список карт"
    override val description = "Демонстрация встраивания карты внутрь списка"
    override val testTag = "map_in_lazy_list"

    private val mainScreenRoute = "MainScreen$route"
    private val secondScreenRoute = "SecondScreen$route"

    override fun NavGraphBuilder.destination(navigateUp: () -> Unit) {
        navigation(
            route = this@MapInLazyList.route,
            startDestination = mainScreenRoute
        ) {
            composable(mainScreenRoute) {
                Component(navigateUp)
            }

            composable(secondScreenRoute) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = AppTheme.colors.bgLevel1),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Тестовый второй экран для тестирования перехода из списка на другой экран и возврата в список",
                        fontSize = 24.sp,
                        color = AppTheme.colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    @Composable
    override fun Content() {
        val navController = LocalNavController.current
        var mapHasFocus by remember { mutableStateOf(false) } // Позволяет не мешать друг другу скролам карты и списка
        val demos = DEMOS.filter { it.route != route } // Добавляем в список все демо кроме текущего

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                userScrollEnabled = !mapHasFocus,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(
                    items = demos,
                    key = { _, demo -> demo.route }
                ) { index, demo ->
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .height(500.dp)
                            .pointerInput(demo.route) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        when (event.type) {
                                            PointerEventType.Press -> mapHasFocus = true
                                            PointerEventType.Release -> mapHasFocus = false
                                        }
                                    }
                                }
                            },
                        content = { demo.Content() }
                    )

                    if (index != demos.lastIndex) {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }

            Button(
                modifier = Modifier.padding(vertical = 24.dp),
                onClick = { navController.navigate(secondScreenRoute) },
            ) {
                Text("Go to Next screen")
            }
        }
    }
}