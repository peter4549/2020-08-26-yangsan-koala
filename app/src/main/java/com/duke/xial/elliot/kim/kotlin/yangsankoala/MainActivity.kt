package com.duke.xial.elliot.kim.kotlin.yangsankoala

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.duke.xial.elliot.kim.kotlin.yangsankoala.Permissions.getPermissionsRequired
import com.duke.xial.elliot.kim.kotlin.yangsankoala.Permissions.hasAccessCoarseLocationPermissions
import com.duke.xial.elliot.kim.kotlin.yangsankoala.Permissions.hasAccessFindLocationPermissions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.daum.mf.map.api.MapView
import org.jsoup.Jsoup
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hash Key: Dq8Gg7LDtrK2P1vH4eJulfzthIY=
        // printHashKey(this)

        setupTimber()

        getConfirmedPatients()

        val permissionsRequired = getPermissionsRequired(this)
        if (permissionsRequired.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(permissionsRequired,
                    PERMISSIONS_REQUEST_CODE
                )
        }

        val mapView = MapView(this)
        map_view_container.addView(mapView)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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

    private fun getConfirmedPatients(): Int = runBlocking(Dispatchers.IO) {
        Jsoup.connect(BASE_URL).get().let { document ->
            val crawlResults = document.body().getElementsByClass("cov_03")[0]
                .getElementsByClass("route_box")[0]
                .select("h4").select("li").toString()
            println("PPPPPPP $crawlResults")
            val k = document.body()
            val z = document.body().getElementsByClass("cov_03")
            try {
                return@runBlocking Regex("[^0-9]").replace(crawlResults, "").toInt()
            } catch (e: NumberFormatException) {
                return@runBlocking 0
            }
        }
    }

    private fun setupTimber() {
        Timber.plant(Timber.DebugTree())
    }

    companion object {
        private const val BASE_URL = "https://www.yangsan.go.kr/portal/contents/popup/corona/corona19.jsp"
    }
}