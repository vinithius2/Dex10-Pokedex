package com.vinithius.dex10.extension

import android.content.Context
import android.os.Build

fun Context.getVersionCode(): Long {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        this.packageManager.getPackageInfo(this.packageName, 0).longVersionCode
    } else {
        @Suppress("DEPRECATION")
        this.packageManager.getPackageInfo(this.packageName, 0).versionCode.toLong()
    }
}

fun Context.getVersionName(): String? {
    return this.packageManager.getPackageInfo(this.packageName, 0).versionName
}
