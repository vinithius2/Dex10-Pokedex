data class AlertMessage(
    val show: Boolean,
    val version_code: Long?,
    val url_action: String?,
    val content: Map<String, LocalizedContent>
) {
    fun getLocalizedContent(languageCode: String?): LocalizedContent {
        val supportedLanguages = listOf("pt", "es", "fr", "hi", "en")
        val code = languageCode?.takeIf { it in supportedLanguages } ?: "en"
        return content[code] ?: content["en"] ?: LocalizedContent(
            title = String(),
            message = String(),
            title_button = String()
        )
    }
}

data class LocalizedContent(
    val title: String,
    val message: String,
    val title_button: String
)
