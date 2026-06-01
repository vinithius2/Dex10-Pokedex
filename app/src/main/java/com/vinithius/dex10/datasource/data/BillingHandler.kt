package com.vinithius.dex10.datasource.data

import android.app.Activity

interface BillingHandler {
    fun setup(activity: Activity, listener: BillingListener)
    fun launchPurchaseFlow(activity: Activity)
    fun launchDonationFlow(activity: Activity)
    fun queryExistingPurchases()
    fun destroy()
}
