package com.duke.xial.elliot.kim.kotlin.yangsankoala

import android.graphics.Color
import com.duke.xial.elliot.kim.kotlin.yangsankoala.SituationNotificationKey.CONFIRMED_PATIENTS
import com.duke.xial.elliot.kim.kotlin.yangsankoala.SituationNotificationKey.DATE
import com.duke.xial.elliot.kim.kotlin.yangsankoala.SituationNotificationKey.SPAN
import com.duke.xial.elliot.kim.kotlin.yangsankoala.SituationNotificationKey.TEST_RESULTS
import com.duke.xial.elliot.kim.kotlin.yangsankoala.SituationNotificationKey.UNDER_INSPECTION
import net.daum.mf.map.api.MapCircle
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapPolyline
import org.jsoup.Jsoup

object ConfirmedPatients {
    private const val BASE_URL = "http://www.yangsan.go.kr/portal/contents/popup/corona/corona19.jsp"
    private const val DATA_URL = "https://peter4549.github.io/yansang-koala-data/yansan_koala/data"

    fun loadConfirmedPatients(): ArrayList<ConfirmedPatientModel>? {
        // Format: date time,place,longitude,latitude,disinfection
        Jsoup.connect(DATA_URL).get().let { document ->
            try {
                val results = arrayListOf<ConfirmedPatientModel>()
                val confirmedPatients =
                    document.body().getElementsByClass("confirmed_patient")

                var id = -1
                for (confirmedPatient in confirmedPatients) {
                    id = confirmedPatient.getElementById("id").text().toInt()
                    val dateTimes = arrayListOf<String>()
                    val places = arrayListOf<String>()
                    val longitudeLatitudes = arrayListOf<Pair<Double, Double>>()
                    val disinfection = arrayListOf<Boolean>()

                    for ((index, data) in confirmedPatient
                        .getElementsByClass("data")
                        .select("li").withIndex()) {
                        val dataStrings = data.text().split(",")
                        dateTimes.add(index, dataStrings[0])
                        places.add(index, dataStrings[1])
                        longitudeLatitudes.add(index, Pair(dataStrings[2].toDouble(), dataStrings[3].toDouble()))
                        disinfection.add(index, dataStrings[4].toBoolean())
                    }
                    results.add(ConfirmedPatientModel(id = id, dateTimes = dateTimes,
                        places = places, positions = longitudeLatitudes, disinfection = disinfection))
                }
                return results
            } catch (e: Exception) {
                return null
            }
        }
    }

    fun getSituationNotificationMap(): Map<String, String>? {
        Jsoup.connect(BASE_URL).get().let { document ->
            try {
                val cov1Element = document.body().getElementsByClass("cov_01")[0]
                    .getElementsByClass("cov_state")[0]
                val date = cov1Element.getElementsByClass("time")[0].text()
                val stateList = cov1Element.getElementsByClass("cl").select("li")

                val stateTexts = arrayListOf<String>()
                var span = ""
                for ((index, state) in stateList.withIndex()) {
                    if (index == 0)
                        span = state.select("span").text()
                    stateTexts.add(index, state.getElementsByClass("tit")[0].text())
                }

                return mapOf(
                    DATE to date,
                    CONFIRMED_PATIENTS to stateTexts[0],
                    SPAN to span,
                    TEST_RESULTS to stateTexts[1],
                    UNDER_INSPECTION to stateTexts[2]
                )

            } catch (e: Exception) {
                return null
            }
        }
    }

    fun createPatientMarkers(confirmedPatients: ArrayList<ConfirmedPatientModel>): ArrayList<MapPOIItem> {
        val markers = arrayListOf<MapPOIItem>()
        for (patient in confirmedPatients) {
            for ((index, dateTime) in  patient.dateTimes.withIndex()) {
                markers.add(MapPOIItem().apply {
                    var itemText = dateTime + ",${patient.places[index]}"
                    itemText += if (patient.disinfection[index])
                        ",true"
                    else
                        ",false"
                    itemName = itemText
                    tag = patient.id
                    mapPoint = MapPoint.mapPointWithGeoCoord(patient.positions[index].first,
                        patient.positions[index].second)
                    markerType = MapPOIItem.MarkerType.RedPin
                    selectedMarkerType = MapPOIItem.MarkerType.RedPin
                })
            }
        }

        return markers
    }
}

object SituationNotificationKey {
    const val DATE = "date"
    const val CONFIRMED_PATIENTS = "confirmed_patients"
    const val SPAN = "span"
    const val TEST_RESULTS = "test_result"
    const val UNDER_INSPECTION = "under_inspection"
}

data class ConfirmedPatientModel(val id: Int,
                                 val dateTimes: ArrayList<String>,
                                 val places: ArrayList<String>,
                                 val positions:
                                 ArrayList<Pair<Double, Double>>,
                                 val disinfection: ArrayList<Boolean>) {
    fun getPolyline(): MapPolyline {
        val polyline = MapPolyline()
        polyline.tag = id
        polyline.lineColor = Color.argb(112, 255, 51, 0)

        for(position in positions) {
            polyline.addPoint(MapPoint.mapPointWithGeoCoord(position.first, position.second))
        }

        return polyline
    }

    fun getCircles(radius: Int = 320): ArrayList<MapCircle> {
        val circles = arrayListOf<MapCircle>()
        for (position in positions) {
            circles.add(MapCircle(position.toMapPointWithGeoCoord(), radius,
                Color.argb(112, 255, 0, 0),
                Color.argb(112, 255, 255, 0)).apply {
                tag = id
            })
        }

        return circles
    }
}

@Suppress("SpellCheckingInspection")
fun Pair<Double, Double>.toMapPointWithGeoCoord(): MapPoint {
    return MapPoint.mapPointWithGeoCoord(this.first, this.second)
}