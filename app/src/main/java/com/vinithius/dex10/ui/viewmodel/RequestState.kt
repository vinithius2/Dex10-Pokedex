package com.vinithius.dex10.ui.viewmodel

sealed class RequestStateDetail<out T> {
    object Loading : RequestStateDetail<Nothing>()
    object Success: RequestStateDetail<Nothing>()
    data class Error(val exception: Throwable) : RequestStateDetail<Nothing>()
}

sealed class RequestStateList<out T> {
    object Loading : RequestStateList<Nothing>()
    object LoadingFirebase : RequestStateList<Nothing>()
    object Success: RequestStateList<Nothing>()
    data class Error(val exception: Throwable) : RequestStateList<Nothing>()
}
