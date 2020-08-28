package com.duke.xial.elliot.kim.kotlin.yangsankoala

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.duke.xial.elliot.kim.kotlin.yangsankoala.Permissions.getPermissionsRequired
import com.duke.xial.elliot.kim.kotlin.yangsankoala.Permissions.hasAccessCoarseLocationPermissions
import com.duke.xial.elliot.kim.kotlin.yangsankoala.Permissions.hasAccessFindLocationPermissions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
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

        val job = Job()
        CoroutineScope(Dispatchers.IO + job).launch {
            getConfirmedPatients()
        }

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

    private suspend fun getConfirmedPatients() = withContext(Dispatchers.IO) {
        Jsoup.connect(BASE_URL).get().let { document ->
            val tableBodies = document.body().getElementsByClass("cov_03")[0]
                .getElementsByClass("route_box")[0]
                .select("li").select("tbody")

            // Key: Date
            // Pair: (Time, Place)
            val timePlaceArrayList: ArrayList<Pair<String, Pair<String, String>>> = arrayListOf()
            val timePlaceMap =
                mutableMapOf<String, ArrayList<Pair<String, Pair<String, String>>>>()

            for ((index, table) in tableBodies.withIndex()) { // 테이블을 도는거.

                var date = ""
                timePlaceArrayList.clear()

                for (item in table.select("tr")) { // 줄을 도는거
                    val th = item.select("th")
                    if (th != null && th.isNotEmpty())
                        date = th[0].text()

                    if (item.getElementsByClass("bo-left").isEmpty())
                        continue
                    val time = item.getElementsByClass("bo-left")[0]?.text()!!
                    val place = item.getElementsByClass("taL")[0].text()
                    if (place == null || place.toString().isEmpty())
                        continue
                    val pair = Pair(time, place)
                    println("UUUUUUU $date")
                    println("OOOOOO $pair")
                    timePlaceArrayList.add(Pair(date, pair))

                }
                println("=======++++++++++========AAAA")

                timePlaceMap[index.toString()] = timePlaceArrayList.map { it.copy() } as ArrayList
            }

            println(timePlaceMap)
        }
    }

    private fun setupTimber() {
        Timber.plant(Timber.DebugTree())
    }

    companion object {
        private const val BASE_URL = "http://www.yangsan.go.kr/portal/contents/popup/corona/corona19.jsp"
    }
}