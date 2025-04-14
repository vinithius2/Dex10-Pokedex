package com.vinithius.poke10x.datasource.data

data class AlertMessage(
    var show: Boolean,
    var version_code: Long?,
    var title: String,
    var message: String,
    var title_button: String?,
    var url_action: String?
)
