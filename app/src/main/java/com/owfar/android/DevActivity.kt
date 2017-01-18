package com.owfar.android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails

class DevActivity : AppCompatActivity() {

    companion object {
        @JvmStatic private val TAG = DevActivity::class.java.simpleName
    }

    private var tvContent: TextView? = null
    private var btReadFromRealm: Button? = null

    private var billingProcessor: BillingProcessor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dev)

        tvContent = findViewById(R.id.activity_dev_tvContent) as? TextView
        btReadFromRealm = findViewById(R.id.activity_dev_btReadFromRealm) as? Button

        btReadFromRealm?.setOnClickListener { onReadFromRealmClick() }

        billingProcessor = BillingProcessor(this, resources.getString(R.string.billing_license_key), billingHandler)
    }

    override fun onDestroy() {
        billingProcessor?.release()
        super.onDestroy()
    }

    fun onReadFromRealmClick() {

        Log.d("BILLING", "onReadFromRealmClick() called ${BillingProcessor.isIabServiceAvailable(this)}")
     Log.d("BILLING", "getPurchaseListingDetails() called ${billingProcessor?.getPurchaseListingDetails("stickers_group_27")}")
        Log.d("BILLING", "listOwnedProducts ${ billingProcessor?.getPurchaseListingDetails("stickers_group_27")}")
        billingProcessor?.purchase(this,"stickers_group_27")
    }

    private val billingHandler = object : BillingProcessor.IBillingHandler {

        override fun onBillingInitialized() {
            Log.d("BILLING", "onBillingInitialized() called")
        }

        override fun onPurchaseHistoryRestored() {
            Log.d("BILLING", "onPurchaseHistoryRestored() called")
            tvContent?.text = billingProcessor?.listOwnedProducts()?.joinToString()
        }

        override fun onProductPurchased(productId: String, details: TransactionDetails) {
            Log.d("BILLING", "onProductPurchased() called with: productId = [$productId], details = [$details]")
        }

        override fun onBillingError(errorCode: Int, error: Throwable) {
            Log.d("BILLING", "onBillingError() called with: errorCode = [$errorCode], error = [$error]")
        }
    }
}