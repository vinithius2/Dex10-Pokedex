package com.vinithius.dex10.datasource.data

interface BillingListener {
    fun onPremiumStatusChecked(isPremium: Boolean)
    fun onPremiumPurchased()
    fun onDonationConsumed()
    fun onPurchaseFailed(reason: String)
    fun onPricesLoaded(premiumPrice: String?, coffeePrice: String?)
}
