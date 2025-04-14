package com.vinithius.poke10x.extension

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
