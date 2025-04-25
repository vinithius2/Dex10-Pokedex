package com.vinithius.dex10.extension

import android.content.Context
import android.os.Build
import java.io.File

fun Context.getVersionCode(): Long {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        this.packageManager.getPackageInfo(this.packageName, 0).longVersionCode
    } else {
        @Suppress("DEPRECATION")
        this.packageManager.getPackageInfo(this.packageName, 0).versionCode.toLong()
    }
}
