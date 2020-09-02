package com.duke.xial.elliot.kim.kotlin.yangsankoala.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.duke.xial.elliot.kim.kotlin.yangsankoala.*
import com.duke.xial.elliot.kim.kotlin.yangsankoala.data.ConfirmedPatients.createPatientMarkers
import com.duke.xial.elliot.kim.kotlin.yangsankoala.data.ConfirmedPatients.loadConfirmedPatients
import com.duke.xial.elliot.kim.kotlin.yangsankoala.data.ScreeningClinics.createClinicsMarkers
import com.duke.xial.elliot.kim.kotlin.yangsankoala.data.ConfirmedPatientModel
import com.duke.xial.elliot.kim.kotlin.yangsankoala.data.ConfirmedPatients
import com.duke.xial.elliot.kim.kotlin.yangsankoala.data.SituationNotificationKey.CONFIRMED_PATIENTS
import com.duke.xial.elliot.kim.kotlin.yangsankoala.data.SituationNotificationKey.DATE
import com.duke.xial.elliot.kim.kotlin.yangsankoala.data.SituationNotificationKey.SPAN
import com.duke.xial.elliot.kim.kotlin.yangsankoala.data.SituationNotificationKey.TEST_RESULTS
import com.duke.xial.elliot.kim.kotlin.yangsankoala.data.SituationNotificationKey.UNDER_INSPECTION
import com.duke.xial.elliot.kim.kotlin.yangsankoala.licenses.OpenSourceLicenseActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
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

    lateinit var adRequest: AdRequest
    lateinit var nativeAdvancedAd: NativeAdvancedAd
    private lateinit var exitDialogFragment: EndDialog
    private lateinit var mapView: MapView
    private var confirmedPatients: ArrayList<ConfirmedPatientModel>? = null
    private var displayCircles = true
    private var displayPolyline = true
    private var mapMovingCount = 0
    private var mapTypePosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_drawer)

        // Hash Key: Dq8Gg7LDtrK2P1vH4eJulfzthIY=
        // printHashKey(this)

        setupTimber()
        restoreOptions()
        MobileAds.initialize(this)

        /*
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@MainActivity)
            nativeAdvancedAd = NativeAdvancedAd(this@MainActivity)
            val view = layoutInflater.inflate(R.layout.fragment_end_dialog, null)
            nativeAdvancedAd.refreshAd(view.ad_frame)
            exitDialogFragment = ExitDialogFragment(view)
        }

         */
        exitDialogFragment = EndDialog(this)

        adRequest = AdRequest.Builder().build()
        setSupportActionBar(toolbar)
        setNavigationViewItemClickActions()

        @Suppress("SpellCheckingInspection")
        // Kakao Maps
        if (!::mapView.isInitialized) {
            mapView = MapView(this)
            map_view_container.addView(mapView)
            mapView.setMapCenterPoint(
                MapPoint.mapPointWithGeoCoord(
                    35.3349920568695,
                    129.037272788817
                ), true
            )
            setSpinner(spinner_map_type, mapView)

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
        }
    }

    private fun initializeMapViewOptions(mapView: MapView) {
        mapView.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        mapView.setZoomLevel(5, true)
        mapView.setCalloutBalloonAdapter(CustomCalloutBalloonAdapter())
        mapView.mapType = when(mapTypePosition) {
            1 -> MapView.MapType.Satellite
            2 -> MapView.MapType.Hybrid
            else -> MapView.MapType.Standard
        }
        mapView.setCurrentLocationEventListener(this)
        mapView.setPOIItemEventListener(this)

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

    private fun setNavigationViewItemClickActions() {
        text_view_open_source_licenses.setOnClickListener {
            startActivityForResult(Intent(this, OpenSourceLicenseActivity::class.java), REQUEST_CODE_OPEN_SOURCE_LICENSES)
        }
        text_view_donation.setOnClickListener {
            startActivityForResult(Intent(this, DonationActivity::class.java), REQUEST_CODE_DONATION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_OPEN_SOURCE_LICENSES -> {
                if (drawer_layout_activity_main.isDrawerOpen(GravityCompat.END))
                    drawer_layout_activity_main.closeDrawer(GravityCompat.END)
            }
            REQUEST_CODE_DONATION -> {
                if (drawer_layout_activity_main.isDrawerOpen(GravityCompat.END))
                    drawer_layout_activity_main.closeDrawer(GravityCompat.END)
            }
        }
    }

    private fun setSpinner(spinner: Spinner, mapView: MapView) {
        spinner.adapter = ArrayAdapter(this,
            R.layout.item_view_spinner, arrayOf(
                "일반", "위성", "하이브리드"
            ))
        spinner.setSelection(mapTypePosition)
        
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, l: Long) {
                when (position) {
                    0 -> mapView.mapType = MapView.MapType.Standard
                    1 -> mapView.mapType = MapView.MapType.Satellite
                    2 -> mapView.mapType = MapView.MapType.Hybrid
                }
                mapTypePosition = position
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {  }
        }
    }

    override fun onBackPressed() {
        if (drawer_layout_activity_main.isDrawerOpen(GravityCompat.END))
            drawer_layout_activity_main.closeDrawer(GravityCompat.END)
        else {
            exitDialogFragment.show(supportFragmentManager, TAG)
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

        mapView.addPOIItems((clinicMarkers + patientMarkers).toTypedArray())
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
            .putInt(KEY_MAP_TYPE_POSITION, mapTypePosition)
            .apply()
    }

    private fun restoreOptions() {
        val preferences =  getSharedPreferences(PREFERENCES_OPTIONS, Context.MODE_PRIVATE)
        displayCircles = preferences.getBoolean(KEY_DISPLAY_CIRCLES, true)
        displayPolyline = preferences.getBoolean(KEY_DISPLAY_POLYLINE, true)
        mapTypePosition = preferences.getInt(KEY_MAP_TYPE_POSITION, 0)
    }

    // Start: POIItemEventListener
    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {  }
    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {  }
    override fun onCalloutBalloonOfPOIItemTouched(
        p0: MapView?,
        p1: MapPOIItem?,
        p2: MapPOIItem.CalloutBalloonButtonType?
    ) {
        if (p1?.tag!! < 1) {
            val placePhoneNumber = p1.itemName?.split("_")
            CallDialogFragment(
                place = placePhoneNumber?.get(0),
                phoneNumber = placePhoneNumber?.get(1)
            ).show(supportFragmentManager, TAG)
        }
    }
    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {  }
    // End: POIItemEventListener

    // Start: CurrentLocationEventListener
    override fun onCurrentLocationUpdate(mapView: MapView?, p1: MapPoint?, p2: Float) {
        if (mapMovingCount > 2)
            mapView?.currentLocationTrackingMode =
                MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving
        ++mapMovingCount
    }
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
                val namePhoneNumberStrings = poiItem.itemName.split("_")
                calloutBalloon.text_view_patient_id.text = ""
                calloutBalloon.text_view_place.text = namePhoneNumberStrings[0]
                calloutBalloon.image_view_disinfection.visibility = View.GONE

                // Used to display phone numbers
                calloutBalloon.text_view_date_time.text = namePhoneNumberStrings[1]
                calloutBalloon.text_view_date_time.isClickable = true
            } else {
                val dateTimePlace = poiItem.itemName.split(",")

                // Numbers exceeding 10000 refer to patients from other regions
                if (poiItem.tag > 10000) {
                    val otherRegionText = "${dateTimePlace[3]} ${poiItem.tag - 10000}"  // Region name
                    calloutBalloon.text_view_patient_id.text = otherRegionText
                } else {
                    calloutBalloon.text_view_patient_id.text = poiItem.tag.toString()
                }

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
        private const val KEY_MAP_TYPE_POSITION = "key_map_type_position"
        private const val TAG = "MainActivity"

        private const val REQUEST_CODE_DONATION = 1000
        private const val REQUEST_CODE_OPEN_SOURCE_LICENSES = 1001
    }
}