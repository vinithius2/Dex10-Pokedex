package com.vinithius.poke10x.datasource.response

import com.google.gson.annotations.SerializedName

data class DamageRelations(
    @SerializedName("double_damage_from")
    val effective_damage_from: List<Default>,
    @SerializedName("double_damage_to")
    val effective_damage_to: List<Default>?,
    @SerializedName("half_damage_from")
    val ineffective_damage_from: List<Default>,
    @SerializedName("half_damage_to")
    val ineffective_damage_to: List<Default>,
    val no_damage_from: List<Default>,
    val no_damage_to: List<Default>
)
