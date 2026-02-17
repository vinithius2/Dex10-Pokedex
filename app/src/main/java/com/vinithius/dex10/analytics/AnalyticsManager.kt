package com.vinithius.dex10.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Centralized manager for Firebase Analytics events.
 * Helps track user behavior, feature usage, and monetization funnels.
 */
object AnalyticsManager {

    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    // Screen Names
    const val SCREEN_TEAM_LIST = "team_list"
    const val SCREEN_TEAM_DETAIL = "team_detail"
    const val SCREEN_POKEDEX_LIST = "pokedex_list"
    const val SCREEN_POKEDEX_DETAIL = "pokedex_detail"
    const val SCREEN_SETTINGS = "settings"
    const val SCREEN_PREMIUM_UPSELL = "premium_upsell"
    const val SCREEN_MOVE_SELECTION = "move_selection"
    const val SCREEN_EDIT_MEMBER = "edit_member"

    // Events
    const val EVENT_CREATE_TEAM = "create_team"
    const val EVENT_DELETE_TEAM = "delete_team"
    const val EVENT_ADD_MEMBER = "add_member"
    const val EVENT_UPDATE_MEMBER = "update_member"
    const val EVENT_REMOVE_MEMBER = "remove_member"
    const val EVENT_SEARCH_POKEMON = "search_pokemon"
    const val EVENT_FILTER_POKEMON = "filter_pokemon"
    const val EVENT_OPEN_UPSELL = "open_upsell"
    const val EVENT_CLICK_PURCHASE = "click_purchase"
    const val EVENT_PURCHASE_SUCCESS = "purchase_success"
    const val EVENT_PURCHASE_FAIL = "purchase_fail"
    const val EVENT_RESTORE_PURCHASES = "restore_purchases"

    // Params
    const val PARAM_TEAM_NAME = "team_name"
    const val PARAM_POKEMON_NAME = "pokemon_name"
    const val PARAM_SEARCH_TERM = "search_term"
    const val PARAM_FILTER_TYPE = "filter_type"
    const val PARAM_ERROR_MESSAGE = "error_message"
    const val PARAM_SOURCE = "source"
    const val PARAM_LIMIT_TYPE = "limit_type"
    const val PARAM_FEATURE_NAME = "feature_name"

    const val EVENT_LIMIT_REACHED = "limit_reached"
    const val EVENT_FEATURE_RESTRICTED = "feature_restricted"
    fun logScreenView(screenName: String, screenClass: String = "MainActivity") {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    /**
     * Log a custom event with optional parameters.
     */
    fun logEvent(eventName: String, params: Bundle? = null) {
        firebaseAnalytics.logEvent(eventName, params)
    }

    /**
     * Log simple event with one string parameter.
     */
    fun logEvent(eventName: String, paramName: String, paramValue: String) {
        val bundle = Bundle().apply {
            putString(paramName, paramValue)
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }
    
    // Specific Action Loggers
    
    fun logCreateTeam(teamName: String) {
        logEvent(EVENT_CREATE_TEAM, PARAM_TEAM_NAME, teamName)
    }
    
    fun logDeleteTeam() {
        logEvent(EVENT_DELETE_TEAM)
    }
    
    fun logAddMember(pokemonName: String) {
        logEvent(EVENT_ADD_MEMBER, PARAM_POKEMON_NAME, pokemonName)
    }
    
    fun logUpdateMember(pokemonName: String) {
        logEvent(EVENT_UPDATE_MEMBER, PARAM_POKEMON_NAME, pokemonName)
    }

    fun logSearch(term: String) {
        logEvent(EVENT_SEARCH_POKEMON, PARAM_SEARCH_TERM, term)
    }
    
    fun logPurchaseAttempt(source: String) {
        logEvent(EVENT_CLICK_PURCHASE, PARAM_SOURCE, source)
    }
    
    fun logPurchaseSuccess() {
        logEvent(EVENT_PURCHASE_SUCCESS)
    }
    
    fun logPurchaseFail(error: String) {
        logEvent(EVENT_PURCHASE_FAIL, PARAM_ERROR_MESSAGE, error)
    }

    fun logLimitReached(limitType: String) {
        logEvent(EVENT_LIMIT_REACHED, PARAM_LIMIT_TYPE, limitType)
    }

    fun logFeatureRestricted(featureName: String) {
        logEvent(EVENT_FEATURE_RESTRICTED, PARAM_FEATURE_NAME, featureName)
    }
    fun logRemoveMember(pokemonName: String) {
        logEvent(EVENT_REMOVE_MEMBER, PARAM_POKEMON_NAME, pokemonName)
    }
}
