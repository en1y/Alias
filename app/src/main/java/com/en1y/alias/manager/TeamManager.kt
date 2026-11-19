package com.en1y.alias.manager

import com.en1y.alias.data.Team
import com.en1y.alias.logic.MaxTeamsSize

class TeamManager {
    private val teams = mutableListOf(Team(), Team())

    fun removeTeam() {
        val s = teams.size
        if (s > 0) teams.removeAt(teams.lastIndex)
    }

    fun addTeam(): Team? {
        val s = teams.size
        if (s < MaxTeamsSize) {
            val t = Team()
            teams.add(t)
            return t
        }
        return null
    }
}
