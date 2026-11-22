package com.en1y.alias.manager

import androidx.compose.runtime.mutableStateListOf
import com.en1y.alias.data.Team
import com.en1y.alias.data.TeamInfo
import com.en1y.alias.logic.MaxTeamsSize

class TeamManager {
    val teams = mutableListOf<Team>()

    constructor() {addTeam(); addTeam()}


    fun removeTeam() {
        val s = teams.size
        if (s > 0) teams.removeAt(teams.lastIndex)
    }

    fun removeTeam(team: Team) {
        if (team in teams) {
            teams.remove(team)
        }
    }

    fun addTeam(): Team? {
        val s = teams.size
        if (s < MaxTeamsSize) {
            var t = Team()
            val teamInfos = mutableSetOf<TeamInfo>()
            teams.forEach { t -> teamInfos.add(t.info) }
            while (true) {
                if (t.info in teamInfos)
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
