package ru.wb.mapkit.mapcompose.demoapp.demos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.wb.mapkit.mapcompose.WbMap
import ru.wb.mapkit.mapcompose.demoapp.R
import ru.wb.mapkit.mapcompose.demoapp.utils.collectStyleProviderAsState
import ru.wb.mapkit.mapcompose.lib.CameraPosition
import ru.wb.mapkit.mapcompose.lib.UiSettings
import ru.wb.mapkit.mapcompose.lib.ZoomButtons
import ru.wb.mapkit.mapcompose.lib.rememberCameraPositionState
import ru.wb.mapkit.mapcompose.location.CameraMode
import ru.wb.mapkit.mapcompose.location.LocationComponent
import ru.wb.mapkit.mapcompose.location.LocationCursor1
import ru.wb.mapkit.mapcompose.location.LocationCursorMode
import ru.wb.mapkit.mapcompose.location.LocationStyle
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
    ) {
        var permissionsGranted by remember { mutableStateOf(false) }
        LocationPermissionRequest { isGranted -> permissionsGranted = isGranted }

        val moscow = remember { CameraPosition(zoom = 12.0) }

        val cameraPositionState = rememberCameraPositionState { position = moscow }
        var renderMode by remember { mutableStateOf(RenderMode.COMPASS) }
        val cameraMode by remember { mutableStateOf(CameraMode.TRACKING_GPS) }

        var isEnabled by remember { mutableStateOf(true) }

        val locationCursor = remember {
            LocationCursor1(
                isEnabled = true,
                availableModes = listOf(LocationCursorMode.TRACKING_NORTH)
            ) {
                Log.i("UserLocation", "onClick $it")
            }
        }

        // LBS очень умный и не дает симулировать произвольные маршруты при помощи android эмулятора
        // Иногда для тестов удобнее использовать внешний стандартный провайдер
        // val context = LocalContext.current
        // val engine = remember { FusedLocationEngine(context) }

        Box(modifier = modifier.fillMaxSize()) {
            WbMap(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("map_container"),
                styleProvider = collectStyleProviderAsState(),
                cameraPositionState = cameraPositionState,
                uiSettings = UiSettings(
                    rotateGesturesEnabled = true,
                    zoomButtons = ZoomButtons(isEnabled = true)
                ),
            ) {
                if (isEnabled) {
                    LocationComponent(
                        permissionsGranted = permissionsGranted,
                        locationStyle = LocationStyle.WB,
                        renderMode = renderMode,
                        cameraMode = cameraMode,
                        //externalLocationEngine = engine
                        externalLocationEngine = null, // null, если хотим пользоваться встроенным engine
                        locationCursor = locationCursor,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .width(200.dp)
                    .align(Alignment.TopEnd)
            ) {
                RenderModeSwitcher(
                    onCompassClick = { renderMode = RenderMode.COMPASS },
                    onGPSClick = { renderMode = RenderMode.GPS }
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (isEnabled) "Отслеживается" else "Не отслеживается",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
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
    }

    @Composable
    private fun RenderModeSwitcher(
        onCompassClick: () -> Unit,
        onGPSClick: () -> Unit,
    ) {
        var selectedMode by remember { mutableStateOf("Compass") }
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                onClick = {
                    selectedMode = "Compass"
                    onCompassClick()
                },
                selected = selectedMode == "Compass",
                label = { Text("Compass") },
            )
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                onClick = {
                    selectedMode = "GPS"
                    onGPSClick()
                },
                selected = selectedMode == "GPS",
                label = { Text("GPS") },
            )
        }
    }

    @Composable
    fun LocationPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        val context = LocalContext.current

        val permissions = buildList {
            // Обязательные разрешения
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)

            // Необязательные - нужны для работы locationEngine (LBS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        fun checkPermissions(): Boolean {
            return permissions.all { perm ->
                ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
            }
        }

        val multiplePermissionsLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsResult ->
            val granted = !permissionsResult.values.any { !it }

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