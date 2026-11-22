package com.en1y.alias.data

import com.en1y.alias.logic.MaxTeamsSize
import com.en1y.alias.logic.MinTeamsSize
import com.en1y.alias.manager.TeamManager

class GameSetting(
    val wordPacks: List<WordPack>,
    val tm: TeamManager,
    val timePerRound: Int,
    val wordsWinCondition: Int,
    val lastWordCanBeGuessed: Boolean,
    val skippedWordSubtractsPoints: Boolean) {

    fun canStartUpGame(): Boolean {
        return wordPacks.isNotEmpty() && tm.teams.size in MinTeamsSize..MaxTeamsSize
    }

    fun gameStartupCheck(): String {
        if (wordPacks.isEmpty())
            return "At least one word pack must be selected"
        if (tm.teams.size < MinTeamsSize)
            return "Number of teams should be at least %d".format(MinTeamsSize)
        if (tm.teams.size < MaxTeamsSize)
            return "Number of teams can not be over %d".format(MaxTeamsSize)
        return ""
    }

}