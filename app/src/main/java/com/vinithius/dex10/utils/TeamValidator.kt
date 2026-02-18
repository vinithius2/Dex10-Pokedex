package com.vinithius.dex10.utils

import com.vinithius.dex10.datasource.database.TeamMemberEntity
import com.vinithius.dex10.datasource.database.TeamWithMembers
import com.vinithius.dex10.datasource.model.EVs

object TeamValidator {

    sealed class ValidationIssue {
        data class DuplicateSpecies(val pokemonId: Int, val positions: List<Int>) : ValidationIssue()
        data class DuplicateItem(val item: String, val positions: List<Int>) : ValidationIssue()
        data class InvalidEVs(val pokemonId: Int, val total: Int, val position: Int) : ValidationIssue()
    }

    /**
     * Validates a complete team against competitive rules (Species Clause, Item Clause).
     */
    fun validateTeam(teamWithMembers: TeamWithMembers): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        val members = teamWithMembers.members

        // 1. Species Clause
        val speciesGroups = members.groupBy { it.pokemonId }
        speciesGroups.forEach { (pokemonId, occurrences) ->
            if (occurrences.size > 1) {
                issues.add(ValidationIssue.DuplicateSpecies(pokemonId, occurrences.map { it.position }))
            }
        }

        // 2. Item Clause
        val itemsGroups = members.filter { 
            val normalized = it.item?.lowercase()?.trim()
            !normalized.isNullOrBlank() && normalized != "none"
        }.groupBy { it.item!!.lowercase().trim() }
        
        itemsGroups.forEach { (item, occurrences) ->
            if (occurrences.size > 1) {
                issues.add(ValidationIssue.DuplicateItem(item, occurrences.map { it.position }))
            }
        }

        return issues
    }

    /**
     * Validates a single member's stats.
     */
    fun validateMember(member: TeamMemberEntity): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        val evs = EVs.fromJson(member.evs)
        
        if (evs.total > 510) {
            issues.add(ValidationIssue.InvalidEVs(member.pokemonId, evs.total, member.position))
        }
        
        return issues
    }
}
