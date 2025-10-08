package ru.wb.mapkit.mapcompose.demoapp.demos

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.wb.mapkit.mapcompose.WbMap
import ru.wb.mapkit.mapcompose.demoapp.R
import ru.wb.mapkit.mapcompose.demoapp.utils.FusedLocationEngine
import ru.wb.mapkit.mapcompose.demoapp.utils.rememberWBStyleProvider
import ru.wb.mapkit.mapcompose.lib.CameraPosition
import ru.wb.mapkit.mapcompose.lib.UiSettings
import ru.wb.mapkit.mapcompose.lib.ZoomButtons
import ru.wb.mapkit.mapcompose.lib.rememberCameraPositionState
import ru.wb.mapkit.mapcompose.location.CameraMode
import ru.wb.mapkit.mapcompose.location.LocationComponent
import ru.wb.mapkit.mapcompose.location.LocationCursor
import ru.wb.mapkit.mapcompose.location.RenderMode

object UserLocation : Demo() {
    override val route: String = this.javaClass.name
    override val title = "Location Component"
    override val description = "Реализация отображения текущего местоположения на карте"
    override val testTag = "user_location"

    override fun NavGraphBuilder.destination(navigateUp: () -> Unit) {
        composable(this@UserLocation.route) {
            Component(navigateUp)
        }
    }

    @Composable
    override fun Content() {
        Map()
    }

    @Composable
    fun Map(
        modifier: Modifier = Modifier,
        isSystemNightMode: Boolean = isSystemInDarkTheme(),
    ) {
        var permissionsGranted by remember { mutableStateOf(false) }
        LocationPermissionRequest { isGranted -> permissionsGranted = isGranted }

        val moscow = remember { CameraPosition(zoom = 12.0) }

        val cameraPositionState = rememberCameraPositionState { position = moscow }
        val renderMode by remember { mutableStateOf(RenderMode.COMPASS) }
        val cameraMode by remember { mutableStateOf(CameraMode.TRACKING_GPS) }

        var isEnabled by remember { mutableStateOf(true) }
        var currentLocation by remember { mutableStateOf("") }

        val context = LocalContext.current
        val locationEngine = remember { FusedLocationEngine(context) }

        DisposableEffect(Unit) {
            onDispose { locationEngine.cleanUp() }
        }

        Box(modifier = modifier.fillMaxSize()) {
            WbMap(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("map_container"),
                styleProvider = rememberWBStyleProvider(isSystemNightMode),
                cameraPositionState = cameraPositionState,
                uiSettings = UiSettings(
                    rotateGesturesEnabled = true,
                    zoomButtons = ZoomButtons(isEnabled = true)
                ),
            ) {
                if (isEnabled) {
                    LocationComponent(
                        permissionsGranted = permissionsGranted,
                        renderMode = renderMode,
                        cameraMode = cameraMode,
                        externalLocationEngine = locationEngine, // Или null, если хотим пользоваться встроенным engine
                        locationCursor = LocationCursor(isEnabled = true)
                    ) {
                        currentLocation = it?.let { "lat = ${it.latitude}, lon = ${it.longitude}" } ?: "Неопределено"
                    }
                } else {
                    currentLocation = "Не отслеживается"
                }
            }

            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentLocation,
                    color = Color.White,
                )

                IconButton(
                    modifier = Modifier,
                    onClick = { isEnabled = !isEnabled }
                ) {
                    Icon(
                        painter = painterResource(if (isEnabled) R.drawable.ic_location_enabled else R.drawable.ic_location_disabled),
                        contentDescription = "Location",
                    )
                }
            }
        }
    }

    @Composable
    fun LocationPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        val context = LocalContext.current

        val permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        fun checkPermissions(): Boolean {
            return permissions.any { perm ->
                ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
            }
        }

        val multiplePermissionsLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsResult ->
            val granted = permissionsResult.values.any { it }
            onPermissionResult(granted)
        }

        LaunchedEffect(Unit) {
            if (!checkPermissions()) {
                multiplePermissionsLauncher.launch(permissions.toTypedArray())
            } else {
                onPermissionResult(true)
            }
        }
    }
}