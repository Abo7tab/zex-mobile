package com.zex.tracker.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun TacticalMapView(
    modifier: Modifier = Modifier,
    targetLat: Double,
    targetLng: Double
) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    var mapView by remember { mutableStateOf<MapView?>(null) }

    DisposableEffect(mapView) {
        mapView?.onResume()
        onDispose {
            mapView?.onPause()
            mapView?.onDetach()
        }
    }

    var marker by remember { mutableStateOf<Marker?>(null) }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)
                    
                    val startPoint = GeoPoint(targetLat, targetLng)
                    controller.setCenter(startPoint)

                    val newMarker = Marker(this).apply {
                        position = startPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "TARGET"
                    }
                    overlays.add(newMarker)
                    
                    mapView = this
                    marker = newMarker
                }
            },
            update = { view ->
                val point = GeoPoint(targetLat, targetLng)
                marker?.position = point
                view.invalidate()
            }
        )

        FloatingActionButton(
            onClick = {
                mapView?.controller?.animateTo(GeoPoint(targetLat, targetLng))
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF2563EB),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Center on Target")
        }
    }
}
