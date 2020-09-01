package com.duke.xial.elliot.kim.kotlin.yangsankoala.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.duke.xial.elliot.kim.kotlin.yangsankoala.R
import com.duke.xial.elliot.kim.kotlin.yangsankoala.utilities.Constants
import com.duke.xial.elliot.kim.kotlin.yangsankoala.utilities.showToast
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
        billingProcessor.purchase(this, "donation")
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        if (productId == DONATION_PRODUCT_ID) {
            showToast(this, "따뜻한 후원에 감사드립니다.")
        }
    }

    override fun onPurchaseHistoryRestored() {  }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        if (errorCode != Constants.BILLING_RESPONSE_RESULT_OK) {
            val errorMessage = when (errorCode) {
                Constants.BILLING_RESPONSE_RESULT_OK ->
                    Constants.errorCodeMessageMap[Constants.BILLING_RESPONSE_RESULT_OK]
                Constants.BILLING_RESPONSE_RESULT_USER_CANCELED ->
                    Constants.errorCodeMessageMap[Constants.BILLING_RESPONSE_RESULT_USER_CANCELED]
                Constants.BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE ->
                    Constants.errorCodeMessageMap[Constants.BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE]
                Constants.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE ->
                    Constants.errorCodeMessageMap[Constants.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE]
                Constants.BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE ->
                    Constants.errorCodeMessageMap[Constants.BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE]
                Constants.BILLING_RESPONSE_RESULT_DEVELOPER_ERROR ->
                    Constants.errorCodeMessageMap[Constants.BILLING_RESPONSE_RESULT_DEVELOPER_ERROR]
                Constants.BILLING_RESPONSE_RESULT_ERROR ->
                    Constants.errorCodeMessageMap[Constants.BILLING_RESPONSE_RESULT_ERROR]
                Constants.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED ->
                    Constants.errorCodeMessageMap[Constants.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED]
                Constants.BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED ->
                    Constants.errorCodeMessageMap[Constants.BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED]
                else -> "알 수 없는 오류가 발생했습니다."
            }
            showToast(this, errorMessage ?: "알 수 없는 오류가 발생했습니다.")
        }
    }

    override fun onBillingInitialized() {  }

    companion object {
        private const val GOOGLE_PLAY_LICENSE_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsRC4qzkbI4nqwLhkI7Y6g0Yv82LR6HmjBB6z2AhFfmwX/ROvF8QgjhpzqlKS1V4ldXz4PdQ8oWod1P6FVWJMNPc/gF/oF0V43QIYIFmtTyBlewsh8Uf/Ukq/Q7fcVPXSjuddVseReo7d/LLSAizRajKrFs0VZwwXNqnwR72N4JMZ64sRR8/l7wb8eIybAZpQ8SXMYvlmmcpnndHBEliw1VXuR1laA4Xk0RGDXQVXDWHk7eDM7su7ByeK+36zGnFug9iDXD7MICfH2DApQzrsxQP3BP5Yj8lPVtl+L9YVTewrtKU6a/6ULWupN6rPjj7VHuhe62FsqLzJi7wx1pj1dwIDAQAB"
        private const val DONATION_PRODUCT_ID = "donation"
    }
}