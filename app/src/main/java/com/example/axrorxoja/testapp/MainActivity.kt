package com.example.axrorxoja.testapp

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import org.osmdroid.bonuspack.routing.MapQuestRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapController
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay


private val startPoint = GeoPoint(41.3245, 69.2875)
private const val KEY = "WGbN9aRBeEN0Yctqme999xZQazA4dcRa"
private val PERMISSIONS = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
private const val REQ_CODE = 1001

class MainActivity : AppCompatActivity() {
    private var markerIcon: Drawable? = null
    private val list = arrayListOf<GeoPoint>()
    private var roadManager: MapQuestRoadManager? = null
    private val overlayList = arrayListOf<Overlay>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        checkPermissions()
        initRoad()
        fab.setOnClickListener { loadGeometry() }
        fabClear.setOnClickListener { clearAll() }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, REQ_CODE)
            } else {
                initMap()
            }
        } else {
            initMap()
        }
    }

    private fun clearAll() {
        overlayList.forEach { mapView.overlayManager.remove(it) }
        mapView.invalidate()

    }

    private fun loadGeometry() {
        launch(UI) {
            val road = withContext(CommonPool) { roadManager?.getRoad(list) }
            val roadOverlay = RoadManager.buildRoadOverlay(road)
            roadOverlay.points.size
            mapView.overlayManager.add(roadOverlay)
            overlayList.add(roadOverlay)
            mapView.invalidate()
        }
    }

    private fun initRoad() {
        roadManager = MapQuestRoadManager(KEY)
    }

    private fun initMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        val mapController = mapView.controller as MapController
        mapView.setMultiTouchControls(true)
        mapController.setCenter(startPoint)
        mapView.setUseDataConnection(true)
        mapView.controller.setZoom(15.0)
        markerIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_pin, theme)
        mapView.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun longPressHelper(p: GeoPoint?) = false

            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                setIcon(p)
                return true
            }
        }))
    }

    private fun setIcon(p: GeoPoint?) {
        if (p != null) {
            val marker = Marker(mapView)
            marker.icon = markerIcon
            marker.position = p
            mapView.overlayManager.add(marker)
            overlayList.add(marker)
            mapView.invalidate()
            list.add(p)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initMap()
        }
    }
}
