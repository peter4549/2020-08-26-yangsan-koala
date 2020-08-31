package com.duke.xial.elliot.kim.kotlin.yangsankoala

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.gms.ads.AdListener
import kotlinx.android.synthetic.main.fragment_exit_dialog.view.*

class ExitDialogFragment: DialogFragment() {

    private lateinit var alertDialog: AlertDialog
    private val adListener = object : AdListener() {
        override fun onAdFailedToLoad(p0: Int) {
            println("$TAG: onAdFailedToLoad")
        }

        override fun onAdLoaded() {
            super.onAdLoaded()
            println("$TAG: onAdLoaded")
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (::alertDialog.isInitialized)
            return alertDialog

        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_exit_dialog, null)
        builder.setView(view)

        view.ad_view.adListener = adListener
        view.ad_view.loadAd((requireActivity() as MainActivity).adRequest)

        view.button_go_to_review.setOnClickListener {
            goToPlayStore(requireContext())
            Thread.sleep(120L)
            (requireActivity() as MainActivity).finish()
        }

        view.button_ok.setOnClickListener {
            dismiss()
            Thread.sleep(120L)
            (requireActivity() as MainActivity).finish()
        }

        alertDialog = builder.create()

        return alertDialog
    }

    override fun onResume() {
        super.onResume()
        val width = resources.getDimensionPixelSize(R.dimen.dialog_width_300)
        dialog?.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        private const val TAG = "ExitApplicationDialogFragment"
    }
}