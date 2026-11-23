package com.en1y.alias.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class Game : ViewModel() { // ViewModel lets the game survive activity recreate
    val settings = GameSetting()
    val teamManager get() = settings.tm

    private var words: List<String> = emptyList()
    private var teamsOrder = mutableListOf<Team>()

    var currentRound by mutableStateOf<Round?>(null)
        private set

    fun start(words: List<String>) {
        this.words = words
        teamsOrder = teamManager.teams.toMutableList()
        currentRound = null
    }

    fun getCurrentTeam(): Team = teamsOrder[0]

    fun createRound(): Round {
        val round = Round(
            words = words,
            gameSetting = settings,
            currentTeam = getCurrentTeam()
        )
        currentRound = round
        return round
    }

    fun endRound() {
        currentRound?.commit()
        teamsOrder.add(teamsOrder.removeAt(0))
    }

    fun gameOver(): Team? {
        val points = mutableListOf<Int>()
        val rounds = mutableSetOf<Int>()
        teamsOrder.forEach { points.add(it.points); rounds.add(it.roundNum) }
        if (rounds.size != 1) return null

        val max = points.max()
        if (max < settings.wordsWinCondition) return null
        if (points.count { it == max } > 1) return null
        return teamsOrder[points.indexOf(max)]
    }

    fun getRound(): Int {
        val rounds = mutableSetOf<Int>()
        teamsOrder.forEach { rounds.add(it.roundNum) }
        var min = Int.MAX_VALUE
        rounds.forEach { min = Math.min(min, it) }
        return min + 1
    }
}
