package com.en1y.alias.data

class Round(
    val words: List<String>,
    val gameSetting: GameSetting,
    val currentTeam: Team
) {
    var roundWords = mutableMapOf<String, GuessStatus>()
    var currentWord = "word"
    var nextWord = generateWord()

    fun countPoints(): Int{
        var c = 0
        for (i in roundWords.values){
            if (i == GuessStatus.Guessed) c++
            else if (i == GuessStatus.Skipped && gameSetting.skippedWordSubtractsPoints) c--
        }
        return c
    }

    fun updateByWord(word: String, guessStatus: GuessStatus) {
        roundWords[word] = guessStatus
    }

    fun end() {
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
        if (gameSetting.lastWordCanBeGuessed && teamWhichGuessed != currentTeam && guessed) {
            teamWhichGuessed.addPoints(1)
        } else {
            roundWords[currentWord] = if (guessed) GuessStatus.Guessed else GuessStatus.Skipped
        }
        nextWord()
    }

    private fun nextWord() {
        currentWord = nextWord
        nextWord = generateWord()
    }

    private fun generateWord(): String {
        var w = words.random()
        while (w in roundWords.keys) {
            w = words.random()
        }
        return w
    }

    enum class GuessStatus{Guessed, Skipped, Deleted}
}