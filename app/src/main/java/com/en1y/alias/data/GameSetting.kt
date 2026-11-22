package com.en1y.alias.data

import android.content.Context
import com.en1y.alias.R
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

    fun gameStartupCheck(context: Context): String {
        if (wordPacks.isEmpty())
            return context.getString(R.string.error_no_word_packs)
        if (tm.teams.size < MinTeamsSize)
            return context.getString(R.string.error_too_few_teams, MinTeamsSize)
        if (tm.teams.size > MaxTeamsSize)
            return context.getString(R.string.error_too_many_teams, MaxTeamsSize)
        return ""
    }

}