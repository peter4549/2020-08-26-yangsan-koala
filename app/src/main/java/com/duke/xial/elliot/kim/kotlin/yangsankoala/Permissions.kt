package com.duke.xial.elliot.kim.kotlin.yangsankoala

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

const val PERMISSIONS_REQUEST_CODE = 1000

object Permissions {
    val accessFindLocationRequired = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    val accessCoarseLocationRequired = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)

    fun getPermissionsRequired(context: Context): Array<String> {
        var permissionsRequired = arrayOf<String>()
        if (!hasAccessFindLocationPermissions(context))
            permissionsRequired += accessFindLocationRequired

        if (!hasAccessCoarseLocationPermissions(context))
            permissionsRequired += accessCoarseLocationRequired

        return permissionsRequired
    }

    fun hasAccessFindLocationPermissions(context: Context) = accessFindLocationRequired.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    fun hasAccessCoarseLocationPermissions(context: Context) = accessCoarseLocationRequired.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}