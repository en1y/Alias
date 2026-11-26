package com.en1y.alias.data

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.en1y.alias.R

enum class WordPack(private val titleId: Int, private val descriptionId: Int, private val wordsId: Int) {
    Pack1(R.string.name1, R.string.description1, R.array.words1),
    Pack2(R.string.name2, R.string.description2, R.array.words2),
    Pack3(R.string.name3, R.string.description3, R.array.words3),
    Pack4(R.string.name4, R.string.description4, R.array.words4),
    Pack5(R.string.name5, R.string.description5, R.array.words5),
    Pack6(R.string.name6, R.string.description6, R.array.words6);

    val title: String @Composable get() = stringResource(titleId)
    val description: String @Composable get() = stringResource(descriptionId)
    fun words(res: Resources) = res.getStringArray(wordsId).toList()
}