package com.duke.xial.elliot.kim.kotlin.yangsankoala

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import kotlinx.android.synthetic.main.activity_donation.*

class DonationActivity : AppCompatActivity(), BillingProcessor.IBillingHandler {

    private lateinit var billingProcessor: BillingProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        billingProcessor = BillingProcessor(this, GOOGLE_PLAY_LICENSE_KEY, this)
        billingProcessor.initialize()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    override fun onDestroy() {
        if (::billingProcessor.isInitialized)
            billingProcessor.release()
        super.onDestroy()
    }

    fun donate(view: View) {
        billingProcessor.purchase(this, "")
        println("ZZZZZZZZZ")
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {

    }

    override fun onPurchaseHistoryRestored() {

    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {

        println("EEEEEEEE ${error?.message}")
        // errorCode == Constants.BILLING_RESPONSE_RESULT_USER_CANCELED 일때는
        // 사용자가 단순히 구매 창을 닫은것임으로 이것 제외하고 핸들링하기.
    }

    override fun onBillingInitialized() {

    }

    companion object {
        private const val GOOGLE_PLAY_LICENSE_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsRC4qzkbI4nqwLhkI7Y6g0Yv82LR6HmjBB6z2AhFfmwX/ROvF8QgjhpzqlKS1V4ldXz4PdQ8oWod1P6FVWJMNPc/gF/oF0V43QIYIFmtTyBlewsh8Uf/Ukq/Q7fcVPXSjuddVseReo7d/LLSAizRajKrFs0VZwwXNqnwR72N4JMZ64sRR8/l7wb8eIybAZpQ8SXMYvlmmcpnndHBEliw1VXuR1laA4Xk0RGDXQVXDWHk7eDM7su7ByeK+36zGnFug9iDXD7MICfH2DApQzrsxQP3BP5Yj8lPVtl+L9YVTewrtKU6a/6ULWupN6rPjj7VHuhe62FsqLzJi7wx1pj1dwIDAQAB"
    }
}