package com.vinithius.poke10.datasource.data

data class AlertMessage(
    var show: Boolean,
    var appVersion: String,
    var title: String,
    var message: String,
    var title_button: String,
    var url_action: String
)
