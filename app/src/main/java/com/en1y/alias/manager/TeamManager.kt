package com.en1y.alias.manager

import com.en1y.alias.data.Team
import com.en1y.alias.logic.MaxTeamsSize

class TeamManager {
    private val teams = mutableListOf(Team(), Team())

    fun removeTeam() {
        val s = teams.size
        if (s > 0) teams.removeAt(teams.lastIndex)
    }

    fun removeTeam(id: Int) {
        val s = teams.size
        if (-1 < id && id < s) {
            teams.removeAt(id)
        }
    }

    fun addTeam(): Team? {
        val s = teams.size
        if (s < MaxTeamsSize) {
            var t = Team()
            while (true) {
                if (t in teams)
                    t = Team()
                else {
                    teams.add(t)
                    return t
                }
            }
        }
        return null
    }
}
