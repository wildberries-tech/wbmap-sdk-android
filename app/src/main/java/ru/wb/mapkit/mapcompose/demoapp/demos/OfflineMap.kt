package ru.wb.mapkit.mapcompose.demoapp.demos

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.gson.Gson
import kotlinx.coroutines.launch
import ru.wb.mapkit.mapcompose.WbMap
import ru.wb.mapkit.mapcompose.demoapp.utils.collectStyleProviderAsState
import ru.wb.mapkit.mapcompose.lib.Attribution
import ru.wb.mapkit.mapcompose.lib.CameraPosition
import ru.wb.mapkit.mapcompose.lib.UiSettings
import ru.wb.mapkit.mapcompose.lib.ZoomButtons
import ru.wb.mapkit.mapcompose.lib.rememberCameraPositionState
import ru.wb.mapkit.mapcompose.models.LatLng
import ru.wb.mapkit.offline.OfflineManager
import ru.wb.mapkit.offline.OfflineRegion
import ru.wb.mapkit.offline.models.DownloadEvent
import ru.wb.mapkit.offline.models.RemoteRegionInfo
import java.io.File

object OfflineMap : Demo() {
    override val route: String = this.javaClass.name
    override val title = "Оффлайн карта"
    override val description = "Демонстрация возможностей offline режима"
    override val testTag = "offline_map"

    private const val TAG = "OfflineMap"

    private data class RegionMetadata(
        val name: String,
        val remoteId: Int? = null,
        val createdAt: Long = System.currentTimeMillis()
    )

    override fun NavGraphBuilder.destination(navigateUp: () -> Unit) {
        composable(this@OfflineMap.route) {
            Component(navigateUp)
        }
    }

    @Composable
    override fun Content() {
        Map()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Map(modifier: Modifier = Modifier) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val gson = remember { Gson() }

        val manager = remember { OfflineManager(context = context) }

        var showBottomSheet by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        var isLoading by remember { mutableStateOf(false) }
        var remoteRegions by remember { mutableStateOf<List<RemoteRegionInfo>>(emptyList()) }
        var downloadedRegions by remember { mutableStateOf<List<OfflineRegion>>(emptyList()) }

        // Прогресс загрузки для каждого региона (remoteId -> progress 0..1)
        val downloadProgress = remember { mutableStateMapOf<Int, Float>() }
        // Регионы в процессе скачивания
        val downloadingRegions = remember { mutableStateMapOf<Int, Boolean>() }

        LaunchedEffect(showBottomSheet) {
            if (!showBottomSheet) return@LaunchedEffect

            isLoading = true
            try {
                downloadedRegions = manager.listOfflineRegions()
                remoteRegions = manager.getAllRemoteRegions()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load regions", e)
            } finally {
                isLoading = false
            }
        }

        fun getRegionName(region: OfflineRegion): String {
            return try {
                val json = region.metadata.toString(Charsets.UTF_8)
                gson.fromJson(json, RegionMetadata::class.java)?.name ?: "Регион ${region.id}"
            } catch (e: Exception) {
                "Регион ${region.id}"
            }
        }

        fun downloadRegion(remoteRegion: RemoteRegionInfo) {
            val downloadUrl = remoteRegion.downloadUrl ?: return
            if (downloadingRegions[remoteRegion.id] == true) return

            downloadingRegions[remoteRegion.id] = true
            downloadProgress[remoteRegion.id] = 0f

            scope.launch {
                try {
                    val destinationFile = File(context.filesDir, "region_${remoteRegion.id}.db")

                    manager.downloadRegionArchive(downloadUrl, destinationFile)
                        .collect { event ->
                            when (event) {
                                is DownloadEvent.Progress -> {
                                    downloadProgress[remoteRegion.id] = event.progress ?: 0f
                                }
                                is DownloadEvent.Completed -> {
                                    manager.mergeOfflineRegions(event.file.absolutePath)
                                    downloadedRegions = manager.listOfflineRegions()
                                    event.file.delete()

                                    Log.i(TAG, "Region ${remoteRegion.getDisplayName()} downloaded successfully")
                                }
                            }
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to download region ${remoteRegion.getDisplayName()}", e)
                } finally {
                    downloadingRegions.remove(remoteRegion.id)
                    downloadProgress.remove(remoteRegion.id)
                }
            }
        }

        fun deleteRegion(region: OfflineRegion) {
            scope.launch {
                try {
                    region.delete()
                    downloadedRegions = manager.listOfflineRegions()
                    Log.i(TAG, "Region deleted successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete region", e)
                }
            }
        }

        val moscow = remember {
            CameraPosition(
                target = LatLng(lat = 55.751244, lng = 37.618423),
                zoom = 12.0
            )
        }

        val cameraPositionState = rememberCameraPositionState { position = moscow }

        Box(modifier = modifier.fillMaxSize()) {
            WbMap(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("map_container"),
                styleProvider = collectStyleProviderAsState(),
                cameraPositionState = cameraPositionState,
                uiSettings = UiSettings(
                    rotateGesturesEnabled = false,
                    attribution = Attribution(
                        isEnabled = true,
                        alignment = Alignment.TopEnd
                    ),
                    zoomButtons = ZoomButtons(isEnabled = true)
                ),
            )

            FloatingActionButton(
                onClick = { showBottomSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Управление регионами"
                )
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = sheetState,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Text(
                            text = "Оффлайн регионы",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            // Имена скачанных регионов
                            val downloadedNames = downloadedRegions.map { getRegionName(it) }.toSet()
                            // Фильтруем нескачанные по имени
                            val notDownloadedRemote = remoteRegions.filter { it.getDisplayName() !in downloadedNames }

                            if (downloadedRegions.isEmpty() && notDownloadedRemote.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Нет доступных регионов",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Скачанные регионы
                                    if (downloadedRegions.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "Скачанные",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }

                                        items(
                                            items = downloadedRegions,
                                            key = { it.id }
                                        ) { region ->
                                            DownloadedRegionItem(
                                                name = getRegionName(region),
                                                onDelete = { deleteRegion(region) }
                                            )
                                            HorizontalDivider()
                                        }
                                    }

                                    // Доступные для скачивания
                                    if (notDownloadedRemote.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "Доступные для скачивания",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }

                                        items(
                                            items = notDownloadedRemote,
                                            key = { it.id }
                                        ) { remoteRegion ->
                                            val isDownloading = downloadingRegions[remoteRegion.id] == true
                                            val progress = downloadProgress[remoteRegion.id]

                                            RemoteRegionItem(
                                                name = remoteRegion.getDisplayName(),
                                                isDownloading = isDownloading,
                                                progress = progress,
                                                onDownload = { downloadRegion(remoteRegion) }
                                            )
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DownloadedRegionItem(
        name: String,
        onDelete: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    @Composable
    private fun RemoteRegionItem(
        name: String,
        isDownloading: Boolean,
        progress: Float?,
        onDownload: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    TextButton(onClick = onDownload) {
                        Text("Скачать")
                    }
                }
            }

            // Прогресс-бар при скачивании
            if (isDownloading && progress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
