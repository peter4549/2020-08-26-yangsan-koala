package com.duke.xial.elliot.kim.kotlin.yangsankoala

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_call_dialog.view.*

class CallDialogFragment(private val place: String? = null,
                         private val phoneNumber: String? = null) : DialogFragment() {

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_call_dialog, null)
        builder.setView(view)

        view.text_view_place.text = place ?: ""
        view.text_view_phone_number.text = phoneNumber ?: ""

        view.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"));
            requireActivity().startActivity(intent)
        }

        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        val width = resources.getDimensionPixelSize(R.dimen.dialog_width_240)
        dialog?.window?.setLayout(width,
            WindowManager.LayoutParams.WRAP_CONTENT)
    }
}