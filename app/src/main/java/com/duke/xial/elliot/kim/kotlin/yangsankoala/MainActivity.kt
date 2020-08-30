package com.duke.xial.elliot.kim.kotlin.yangsankoala

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.duke.xial.elliot.kim.kotlin.yangsankoala.ConfirmedPatients.createPatientMarkers
import com.duke.xial.elliot.kim.kotlin.yangsankoala.ConfirmedPatients.loadConfirmedPatients
import com.duke.xial.elliot.kim.kotlin.yangsankoala.Permissions.getPermissionsRequired
import com.duke.xial.elliot.kim.kotlin.yangsankoala.Permissions.hasAccessCoarseLocationPermissions
import com.duke.xial.elliot.kim.kotlin.yangsankoala.Permissions.hasAccessFindLocationPermissions
import com.duke.xial.elliot.kim.kotlin.yangsankoala.ScreeningClinics.createClinicsMarkers
import com.duke.xial.elliot.kim.kotlin.yangsankoala.SituationNotificationKey.CONFIRMED_PATIENTS
import com.duke.xial.elliot.kim.kotlin.yangsankoala.SituationNotificationKey.DATE
import com.duke.xial.elliot.kim.kotlin.yangsankoala.SituationNotificationKey.SPAN
import com.duke.xial.elliot.kim.kotlin.yangsankoala.SituationNotificationKey.TEST_RESULTS
import com.duke.xial.elliot.kim.kotlin.yangsankoala.SituationNotificationKey.UNDER_INSPECTION
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_drawer.*
import kotlinx.android.synthetic.main.custom_callout_balloon.view.*
import kotlinx.coroutines.*
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import timber.log.Timber

class MainActivity : AppCompatActivity(), MapView.POIItemEventListener, MapView.CurrentLocationEventListener {

    private var confirmedPatients: ArrayList<ConfirmedPatientModel>? = null
    private var displayCircles = true
    private var displayPolyline = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_drawer)

        // Hash Key: Dq8Gg7LDtrK2P1vH4eJulfzthIY=
        // printHashKey(this)

        setSupportActionBar(toolbar)
        setMenuItemClickActions()

        setupTimber()
        restoreOptions()

        @Suppress("SpellCheckingInspection")
        // Kakao Maps
        val mapView = MapView(this)
        map_view_container.addView(mapView)
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(35.3349920568695, 129.037272788817), true)

        // Crawling
        val job = Job()
        CoroutineScope(Dispatchers.IO + job).launch {
            val map = ConfirmedPatients.getSituationNotificationMap()
            setStateNotificationUi(map)
            launch {
                confirmedPatients = loadConfirmedPatients()
            }.join()

            launch(Dispatchers.Main) {
                initializeMapViewOptions(mapView)
            }
        }

        val permissionsRequired = getPermissionsRequired(this)
        if (permissionsRequired.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(
                    permissionsRequired,
                    PERMISSIONS_REQUEST_CODE
                )
        }
    }

    private fun initializeMapViewOptions(mapView: MapView) {
        mapView.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving
        mapView.setZoomLevel(5, true)
        mapView.setCalloutBalloonAdapter(CustomCalloutBalloonAdapter())
        setMarkers(mapView)

        if (displayCircles)
            setCircles(mapView)

        if (displayPolyline)
            setPolyline(mapView)

        initializeSwitchStates(mapView)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_options -> {
                drawer_layout_activity_main.openDrawer(GravityCompat.END)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initializeSwitchStates(mapView: MapView) {
        switch_circles.isChecked = displayCircles
        switch_circles.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setCircles(mapView)
            }
            else {
                mapView.removeAllCircles()
            }
            displayCircles = isChecked
        }

        switch_polyline.isChecked = displayPolyline
        switch_polyline.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setPolyline(mapView)
            }
            else {
                mapView.removeAllPolylines()
            }
            displayPolyline = isChecked
        }
    }

    private fun setMenuItemClickActions() {
        text_view_open_source_licenses.setOnClickListener {
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
        }
    }

    override fun onBackPressed() {
        if (drawer_layout_activity_main.isDrawerOpen(GravityCompat.END))
            drawer_layout_activity_main.closeDrawer(GravityCompat.END)
        else
            super.onBackPressed()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                if (hasAccessFindLocationPermissions(this))
                    Timber.d("permission ACCESS_FINE_LOCATION has been granted")
                if (hasAccessCoarseLocationPermissions(this))
                    Timber.d("permission ACCESS_COARSE_LOCATION has been granted")
            } else {
                if (!hasAccessFindLocationPermissions(this))
                    showToast(this, getString(R.string.location_permission_grant_request_message))
                if (!hasAccessCoarseLocationPermissions(this))
                    showToast(this, getString(R.string.location_permission_grant_request_message))
            }
        }
    }

    private fun setupTimber() {
        Timber.plant(Timber.DebugTree())
    }

    private fun setStateNotificationUi(map: Map<String, String>?) =
        runBlocking(Dispatchers.Main) {
        if (map != null) {
            text_view_date.text = map[DATE]
            text_view_number_01.text = map[CONFIRMED_PATIENTS]?.split("(")?.get(0) ?:
                    map[CONFIRMED_PATIENTS]
            text_view_span.text = map[SPAN]
            text_view_number_02.text = map[TEST_RESULTS]
            text_view_number_03.text = map[UNDER_INSPECTION]
        } else {
            val errorMessage = getString(R.string.failed_to_load_data)
            text_view_date.text = errorMessage
            text_view_number_01.text = errorMessage
            text_view_span.text = ""
            text_view_number_02.text = errorMessage
            text_view_number_03.text = errorMessage
        }
    }

    private fun setMarkers(mapView: MapView) {
        val clinicMarkers = createClinicsMarkers()
        val patientMarkers = createPatientMarkers((confirmedPatients ?: arrayListOf()))

        for (marker in (clinicMarkers + patientMarkers))
            mapView.addPOIItem(marker)
    }

    private fun setPolyline(mapView: MapView) {
        for(patient in (confirmedPatients ?: arrayListOf())) {
            mapView.addPolyline(patient.getPolyline())
        }
    }

    private fun setCircles(mapView: MapView) {
        for (patient in (confirmedPatients ?: arrayListOf())) {
            for (circle in patient.getCircles()) {
                mapView.addCircle(circle)
            }
        }
    }

    override fun onStop() {
        storeOptions()
        super.onStop()
    }

    private fun storeOptions() {
        getSharedPreferences(PREFERENCES_OPTIONS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_DISPLAY_CIRCLES, displayCircles)
            .putBoolean(KEY_DISPLAY_POLYLINE, displayPolyline)
            .apply()
    }

    private fun restoreOptions() {
        val preferences =  getSharedPreferences(PREFERENCES_OPTIONS, Context.MODE_PRIVATE)
        displayCircles = preferences.getBoolean(KEY_DISPLAY_CIRCLES, true)
        displayPolyline = preferences.getBoolean(KEY_DISPLAY_POLYLINE, true)
    }

    // Start: POIItemEventListener
    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
        TODO("Not yet implemented")
    }

    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
        TODO("Not yet implemented")
    }

    override fun onCalloutBalloonOfPOIItemTouched(
        p0: MapView?,
        p1: MapPOIItem?,
        p2: MapPOIItem.CalloutBalloonButtonType?
    ) {
        TODO("Not yet implemented")
    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
        TODO("Not yet implemented")
    }
    // End: POIItemEventListener

    // Start: CurrentLocationEventListener
    override fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {  }
    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {  }
    override fun onCurrentLocationUpdateFailed(p0: MapView?) {  }
    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {  }
    // End: CurrentLocationEventListener

    @Suppress("SpellCheckingInspection")
    inner class CustomCalloutBalloonAdapter : CalloutBalloonAdapter {
        @SuppressLint("InflateParams")
        private val calloutBalloon: View = layoutInflater.inflate(R.layout.custom_callout_balloon, null)

        override fun getCalloutBalloon(poiItem: MapPOIItem): View {
            if (poiItem.tag < 1) {
                calloutBalloon.text_view_patient_id.text = ""
                calloutBalloon.text_view_date_time.text = ""
                calloutBalloon.text_view_place.text = poiItem.itemName
                calloutBalloon.image_view_disinfection.visibility = View.GONE
            } else {
                calloutBalloon.text_view_patient_id.text = poiItem.tag.toString()

                val dateTimePlace = poiItem.itemName.split(",")
                calloutBalloon.text_view_date_time.text = dateTimePlace[0]
                calloutBalloon.text_view_place.text = dateTimePlace[1]
                calloutBalloon.image_view_disinfection.visibility = View.VISIBLE
                if (dateTimePlace[2] != "true")
                    calloutBalloon.image_view_disinfection.setImageResource(R.drawable.ic_baseline_cancel_24)
                else
                    calloutBalloon.image_view_disinfection.setImageResource(R.drawable.ic_baseline_check_circle_24)
            }
            return calloutBalloon
        }

        override fun getPressedCalloutBalloon(poiItem: MapPOIItem): View {
            return calloutBalloon
        }
    }

    companion object {
        private const val PREFERENCES_OPTIONS = "yangsan_koala_preferences_options"
        private const val KEY_DISPLAY_CIRCLES = "key_display_circles"
        private const val KEY_DISPLAY_POLYLINE = "key_display_polyline"
    }
}