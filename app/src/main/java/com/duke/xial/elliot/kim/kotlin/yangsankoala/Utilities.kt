package com.duke.xial.elliot.kim.kotlin.yangsankoala

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
private fun printHashKey(context: Context) {
    try {
        val info: PackageInfo =
            context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
        for (signature in info.signatures) {
            val md: MessageDigest = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            val hashKey = String(Base64.encode(md.digest(), 0))
            println("printHashKey() Hash Key: $hashKey")
        }
    } catch (e: NoSuchAlgorithmException) {
        println("printHashKey() $e")
    } catch (e: Exception) {
        println("printHashKey() $e")
    }
}
 */

fun showToast(context: Context, text: String, duration: Int = Toast.LENGTH_LONG) {
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(context, text, duration).show()
    }
}