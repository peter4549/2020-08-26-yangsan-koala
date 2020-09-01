package com.duke.xial.elliot.kim.kotlin.yangsankoala.licenses

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duke.xial.elliot.kim.kotlin.yangsankoala.R
import com.duke.xial.elliot.kim.kotlin.yangsankoala.utilities.collapse
import com.duke.xial.elliot.kim.kotlin.yangsankoala.utilities.expand
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.android.synthetic.main.activity_open_source_license.*
import kotlinx.android.synthetic.main.activity_open_source_license.toolbar
import kotlinx.android.synthetic.main.item_view_license.view.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.Comparator

class OpenSourceLicenseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_source_license)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Collections.sort(licenses, Comparator { o1: LicenseModel, o2: LicenseModel ->
            return@Comparator o1.name.compareTo(o2.name)
        })

        recycler_view_licenses.apply {
            adapter = LicensesRecyclerViewAdapter(licenses)
            layoutManager = GridLayoutManagerWrapper(this@OpenSourceLicenseActivity, 1)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class LicensesRecyclerViewAdapter(private val licenses: ArrayList<LicenseModel>):
        RecyclerView.Adapter<LicensesRecyclerViewAdapter.ViewHolder>() {

        private var originalHeightSaved = false
        private var originHeight = 0

        inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view)

        private val isExpandedMap = mutableMapOf<Int, Boolean>()

        init {
            for(i in 0 until licenses.count())
                isExpandedMap[i] = false
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_license, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = licenses.count()

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val license = licenses[position]

            holder.view.text_view_name.text = license.name
            holder.view.text_view_link.text = license.link

            if (!originalHeightSaved) {
                originHeight = holder.view.text_view_copyright.layoutParams.height
                originalHeightSaved = true
            }

            val copyRight = license.copyright?.let { getCopyright(it) }
            if (copyRight == null || copyRight.isBlank()) {
                // Specific case
                if (license.name == "Android") {
                    val specificText = "show all"
                    holder.view.text_view_copyright
                        .setTextColor(ContextCompat.getColor(this@OpenSourceLicenseActivity, R.color.colorAccent))
                    holder.view.text_view_copyright.text = specificText
                    holder.view.text_view_copyright.setOnClickListener {
                        startActivity(Intent(this@OpenSourceLicenseActivity,
                            OssLicensesMenuActivity::class.java))
                    }
                }
                else
                    holder.view.text_view_copyright.visibility = View.GONE
            }
            else
                holder.view.text_view_copyright.text = copyRight

            if (license.name != "Android") {
                holder.view.text_view_copyright.setOnClickListener {
                    if (isExpandedMap[position] != false) {
                        it.collapse(originHeight)
                        isExpandedMap[position] = false
                    } else {
                        it.expand()
                        isExpandedMap[position] = true
                    }
                }
            }
        }

        private fun getCopyright(file: String): String {
            var reader: BufferedReader? = null
            val stringBuilder = StringBuilder()
            try {
                reader = BufferedReader(InputStreamReader(this@OpenSourceLicenseActivity.assets.open("licenses/$file")))
                for (line in reader.readLines()) {
                    stringBuilder.append(line + "\n")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (reader != null) {
                    try {
                        reader.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            return stringBuilder.toString()
        }
    }
}

@Suppress("unused")
class GridLayoutManagerWrapper: GridLayoutManager {
    constructor(context: Context, spanCount: Int) : super(context, spanCount)
    constructor(context: Context, spanCount: Int, orientation: Int, reverseLayout: Boolean) :
            super(context, spanCount, orientation, reverseLayout)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)
    override fun supportsPredictiveItemAnimations(): Boolean { return false }
}