package com.en1y.alias.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class Game(
    val gameSetting: GameSetting,
    val words: List<String>
) {
    val teamManager get() = gameSetting.tm
    val teamsOrder get() = teamManager.teams.toMutableList()

    var currentRound: Round? = null
        private set

    fun getCurrentTeam(): Team = teamsOrder[0]

    fun createRound(): Round {
        val round = Round(
            words = words,
            gameSetting = gameSetting,
            currentTeam = getCurrentTeam()
        )
        currentRound = round
        return round
    }

    fun endRound() {
        currentRound?.end()
        teamsOrder.add(teamsOrder.removeAt(0))
    }

    fun gameOver(): Team? {
        val points = mutableListOf<Int>()
        val rounds = mutableSetOf<Int>()
        teamsOrder.forEach { points.add(it.points); rounds.add(it.roundNum) }
        if (points.size != setOf(points).size) return null
        if (rounds.size != 1) return null

        var max = Int.MIN_VALUE
        points.forEach { max = Math.max(max, it) }
        return teamsOrder[points.indexOf(max)]
    }

    fun getRound(): Int {
        val rounds = mutableSetOf<Int>()
        teamsOrder.forEach { rounds.add(it.roundNum) }
        var min = Int.MAX_VALUE
        rounds.forEach { min = Math.min(min, it) }
        return min
    }
}

@Composable
fun rememberGame(gameSetting: GameSetting): Game {
    val words = gameSetting.wordPacks.flatMap { it.words }
    return remember(gameSetting, words) {
        Game(gameSetting = gameSetting, words = words)
    }
}