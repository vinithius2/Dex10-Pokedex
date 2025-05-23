package com.vinithius.dex10.extension

import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable

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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ComponentActivity.getWindowColumns(): Int {
    val windowSizeClass = calculateWindowSizeClass(this)

    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium,
        WindowWidthSizeClass.Expanded -> 2
        else -> 1
    }
}
