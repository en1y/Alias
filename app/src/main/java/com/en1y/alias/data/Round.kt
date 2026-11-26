package com.en1y.alias.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.en1y.alias.logic.AllWordsGuessed

class Round(
    val words: List<List<String>>,
    val gameSetting: GameSetting,
    val currentTeam: Team
) {
    val allWords = words.flatten()
    var roundWords by mutableStateOf<Map<String, GuessStatus>>(emptyMap())
        private set
    var currentWord by mutableStateOf(allWords.random())
    var nextWord by mutableStateOf(generateWord())
    var counter by mutableIntStateOf(0)

    private var guessable = false

    fun countPoints(): Int {
        var c = 0
        for (i in roundWords.values) {
            if (i == GuessStatus.Guessed) c++
            else if (i == GuessStatus.Skipped && gameSetting.skippedWordSubtractsPoints) c--
        }
        return c
    }

    fun updateByWord(word: String, guessStatus: GuessStatus) {
        roundWords += word to guessStatus
    }

    fun start() {
        generateWord()
        guessable = true
    }

    fun end() {
        guessable = false
    }

    fun commit() {
        currentTeam.addPoints(countPoints())
        currentTeam.addRoundPlayed()
    }

    fun guessed(teamWhichGuessed: Team = currentTeam) {
        guess(true, teamWhichGuessed)
    }

    fun skipped() {
        guess(false)
    }

    private fun guess(guessed: Boolean = false, teamWhichGuessed: Team = currentTeam) {
        if (guessable) {
            if (gameSetting.lastWordCanBeGuessed && teamWhichGuessed != currentTeam && guessed) {
                teamWhichGuessed.addPoints(1)
            } else {
                roundWords += currentWord to if (guessed) GuessStatus.Guessed else GuessStatus.Skipped
            }
            counter++
            nextWord()
        }
    }

    private fun nextWord() {
        currentWord = nextWord
        nextWord = generateWord()
    }

    private fun generateWord(): String {
        return (allWords - roundWords.keys - currentWord).randomOrNull() ?: AllWordsGuessed
    }

    enum class GuessStatus { Guessed, Skipped, Deleted }
}