package com.duke.xial.elliot.kim.kotlin.yangsankoala.data

import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint

object ScreeningClinics {
    private const val CLINIC_01 = "베데스다병원_055-392-5220,5227"
    private const val CLINIC_02 = "양산부산대학교병원_055-384-9901"
    private const val CLINIC_03 = "양산시 보건소_055-360-1476"

    private val screeningClinics = mutableMapOf(
        CLINIC_01 to Pair(35.3532324484062, 129.043600274679),
        CLINIC_02 to Pair(35.3278213049947, 129.005885912653),
        CLINIC_03 to Pair(35.3357394475375, 129.024974765224)
    )

    fun createClinicsMarkers(): ArrayList<MapPOIItem>{
        val markers = arrayListOf<MapPOIItem>()
        for ((index, entry) in screeningClinics.entries.withIndex()) {
            markers.add(MapPOIItem().apply {
                itemName = entry.key
                tag = index - (screeningClinics.count() * 2)
                mapPoint = MapPoint.mapPointWithGeoCoord(
                    entry.value.first,
                    entry.value.second)
                markerType = MapPOIItem.MarkerType.BluePin
                selectedMarkerType = MapPOIItem.MarkerType.BluePin
            })
        }

        return markers
    }
}