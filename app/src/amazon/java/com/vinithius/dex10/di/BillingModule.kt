package com.vinithius.dex10.di

import com.vinithius.dex10.datasource.data.AmazonBillingHandler
import com.vinithius.dex10.datasource.data.BillingHandler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val billingModule = module {
    single<BillingHandler> { AmazonBillingHandler(androidContext()) }
}
