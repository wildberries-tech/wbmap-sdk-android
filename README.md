# WBMap Compose Demo App

A demo application for a WBMap Compose SDK library.
WBMap Compose SDK is a Jetpack Compose wrapper for WBMap, providing a declarative way to work with maps in your Android application.

## Description

WBMap Compose is a Kotlin library that provides Jetpack Compose bindings for the WBMap SDK. It allows you to easily integrate interactive maps into your Compose-based Android applications using a declarative API.

Key features:
- Seamless integration with Jetpack Compose
- Declarative API for map configuration
- Type-safe builder patterns for map layers and properties
- Support for dynamic styling and property updates
- Simplified handling of map markers, polygons, and other geometries

### [Changelog](CHANGELOG.md)

## API Key Setup

To run the application, you need to obtain an API key for map style access.

### Setting up the Key

Add the key (in quotes) to the `gradle.properties` file in the project root:
```properties
API_KEY="<<PUT YOUR API KEY HERE>>"
```

## GitHib credentials Setup

To run the application, you need to obtain an GitHub login and token for WBMap Compose sdk access.

### Setting up credentials

Add the credentials (without quotes) to the `gradle.properties` file in the project root:
```properties
githubUser=<<PUT YOUR GITHUB LOGIN HERE>>
githubToken=<<PUT YOUR GITHUB TOKEN HERE>>
```

## Usage Example

Here's a simple example of how to use WBMap Compose in your application:

```kotlin
@Composable
fun MapScreen() {
    val cameraPositionState = rememberCameraPositionState()

    WbMap(
        modifier = Modifier.fillMaxSize(),
        styleProvider = DefaultStyleProvider(),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            // Handle map click
        }
    ) {
        // Define the source ID that will be used for both layers
        val sourceId = "source_id"

        // Create a GeoJSON source with clustering enabled
        // Features are points that will be visualized on the map
        GeoJsonSource(
            id = sourceId,
            features = features,
            options = GeoJsonOptions(cluster = true)
        )

        // Add a custom image to the map that can be used by symbol layers
        // This loads an image from resources to use as a marker icon
        MapImage(
            MapIcon("icon", R.drawable.pin_pvz_all)
        )

        // Create a symbol layer to display clusters of points
        // This layer uses text and icon properties for visualization
        // and includes click handlers for interaction
        SymbolLayer(
            id = "cluster",
            source = sourceId,
            filter = has("point_count"),  // Only show points that are clusters
            textProperty = clusterTextProperties,
            iconProperty = clusterIconProperties,
            onClick = { 
                // Handle cluster click 
                // Return true to consume the click event
                true  
            },
            onLongClick = {
                // Handle cluster long click 
                // Return true to consume the click event
                true  
            }
        )

        // Create a circle layer to display individual points (non-clustered)
        // This layer uses circle properties for visualization
        // and includes click handlers for interaction
        CircleLayer(
            id = "regular",
            source = sourceId,
            filter = not(has("point_count")),  // Only show points that are NOT clusters
            circleProperty = circleProperties,
            onClick = {
                // Handle feture click 
                // Return true to consume the click event
                true 
            },
            onLongClick = {
                // Handle feature long click 
                // Return true to consume the click event
                true
            }
        )
    }
}
```

### Setting Camera Position

```kotlin
val cameraPositionState = rememberCameraPositionState()

val moscow = remember {
    CameraPosition(
        target = LatLng(lat = 55.751244, lng = 37.618423),
        zoom = 12.0
    )
}
//...
cameraPositionState.position = moscow
```