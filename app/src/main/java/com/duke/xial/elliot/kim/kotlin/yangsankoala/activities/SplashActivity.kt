package com.duke.xial.elliot.kim.kotlin.yangsankoala.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.duke.xial.elliot.kim.kotlin.yangsankoala.PERMISSIONS_REQUEST_CODE
import com.duke.xial.elliot.kim.kotlin.yangsankoala.Permissions
import com.duke.xial.elliot.kim.kotlin.yangsankoala.R
import com.duke.xial.elliot.kim.kotlin.yangsankoala.utilities.getVersionName
import com.duke.xial.elliot.kim.kotlin.yangsankoala.utilities.goToPlayStore
import com.duke.xial.elliot.kim.kotlin.yangsankoala.utilities.showToast
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import timber.log.Timber

class SplashActivity : AppCompatActivity() {
    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val permissionsRequired = Permissions.getPermissionsRequired(this)
        if (permissionsRequired.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(
                    permissionsRequired,
                    PERMISSIONS_REQUEST_CODE
                )
        } else {
            firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build()
            firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
            firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config)
            firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        println("$TAG: remote config activate successful")
                        val versionUpdate = firebaseRemoteConfig.getBoolean("version_update")
                        val versionName = firebaseRemoteConfig.getString("version_name")
                        if (versionUpdate && (versionName != getVersionName(this))) {
                            showConfigMessage()
                        }
                        else {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                if (Permissions.hasAccessFindLocationPermissions(this) &&
                    Permissions.hasAccessCoarseLocationPermissions(this)
                ) {
                    Timber.d("permission ACCESS_FINE_LOCATION has been granted")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            } else {
                if (!Permissions.hasAccessFindLocationPermissions(this)) {
                    showToast(this, getString(R.string.location_permission_grant_request_message))
                    finish()
                }
                if (!Permissions.hasAccessCoarseLocationPermissions(this)) {
                    showToast(this, getString(R.string.location_permission_grant_request_message))
                    finish()
                }
            }
        }
    }

    private fun showConfigMessage() {
        val message = firebaseRemoteConfig.getString("message")
        val exitWhenNotUpdating = firebaseRemoteConfig.getBoolean("exit_when_not_updating")
        var positiveButtonClicked = false

        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setPositiveButton(getString(R.string.update)) { _, _ ->
                positiveButtonClicked = true
                goToPlayStore(this)
                finish()
            }
            .setNegativeButton(
                if (exitWhenNotUpdating) getString(R.string.exit)
                else getString(R.string.later)) { _, _ ->

                if (exitWhenNotUpdating)
                    finish()
                else {
                    positiveButtonClicked = false
                }
            }
            .setOnDismissListener {
                if (!positiveButtonClicked && !exitWhenNotUpdating) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }

        builder.create()

        val dialog = builder.show()!!

        val titleTextView = dialog.findViewById<TextView>(android.R.id.message)!!
        val okButton = dialog.findViewById<Button>(android.R.id.button1)!!
        val cancelButton = dialog.findViewById<Button>(android.R.id.button2)!!

        val font = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            resources.getFont(R.font.font_family_nexon_lv2_gothic_bold)
        else ResourcesCompat.getFont(this, R.font.font_family_nexon_lv2_gothic_bold)

        titleTextView.typeface = font
        okButton.typeface = font
        cancelButton.typeface = font
    }

    companion object {
        private const val TAG = "SplashActivity"
    }
}