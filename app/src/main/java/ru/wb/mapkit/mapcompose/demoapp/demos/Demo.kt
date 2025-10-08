package ru.wb.mapkit.mapcompose.demoapp.demos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import ru.wb.mapkit.mapcompose.demoapp.DemoAppBar

val DEMOS = buildList {
    add(SimpleMap)
    add(ClusterWithCustomAggregation)
    add(StretchableImages)
    add(AnimatedLayers)
    add(AnnotationsMap)
    add(ComposableSymbols)
    add(UserLocation)
    add(MapInLazyList)
}

abstract class Demo {
    abstract val route: String
    abstract val title: String
    abstract val description: String
    abstract val testTag: String

    abstract fun NavGraphBuilder.destination(navigateUp: () -> Unit)

    @Composable
    abstract fun Content()

    @Composable
    fun Component(navigateUp: () -> Unit) {
        DemoScaffold(this, navigateUp) { Content() }
    }

    @Composable
    private fun DemoScaffold(demo: Demo, navigateUp: () -> Unit, content: @Composable () -> Unit) {
        Scaffold(
            topBar = { DemoAppBar(demo, navigateUp) },
        ) { padding ->
            Box(
                modifier = Modifier
                    .consumeWindowInsets(padding)
                    .padding(padding)
            ) { content() }
        }
    }
}