package com.duke.xial.elliot.kim.kotlin.yangsankoala.licenses

data class LicenseModel(val name: String,
                        val link: String,
                        val copyright: String?)

internal val licenses = arrayListOf(
    LicenseModel("Timber",
        "https://github.com/JakeWharton/timber",
        "timber.txt"),
    LicenseModel("Android",
        "https://developer.android.com/topic/libraries/support-library",
        "android.txt"),
)