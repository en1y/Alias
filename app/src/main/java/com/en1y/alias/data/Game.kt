package com.en1y.alias.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class Game(
    val gameSetting: GameSetting,
    val words: List<String>
) {
    val teamManager get() = gameSetting.tm
    // snapshot once: a getter would return a fresh copy on every access, so rotating it did nothing
    val teamsOrder = teamManager.teams.toMutableList()

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
        currentRound?.commit()
        teamsOrder.add(teamsOrder.removeAt(0))
    }

    fun gameOver(): Team? {
        val points = mutableListOf<Int>()
        val rounds = mutableSetOf<Int>()
        teamsOrder.forEach { points.add(it.points); rounds.add(it.roundNum) }
        if (rounds.size != 1) return null // not everyone has played the same number of rounds yet

        val max = points.max()
        if (max < gameSetting.wordsWinCondition) return null
        if (points.count { it == max } > 1) return null // tie: play another round
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

@Composable
fun rememberGame(gameSetting: GameSetting): Game {
    val words = gameSetting.wordPacks.flatMap { it.words }
    return remember(gameSetting, words) {
        Game(gameSetting = gameSetting, words = words)
    }
}