package com.en1y.alias.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.en1y.alias.R

enum class WordPack(private val titleId: Int, private val descriptionId: Int, private val wordsId: Int) {
    pack1(R.string.name1, R.string.description1, R.array.words1),
    pack2(R.string.name2, R.string.description2, R.array.words2),
    pack3(R.string.name3, R.string.description3, R.array.words3),
    pack4(R.string.name4, R.string.description4, R.array.words4),
    pack5(R.string.name5, R.string.description5, R.array.words5),
    pack6(R.string.name6, R.string.description6, R.array.words6),
    pack7(R.string.name7, R.string.description7, R.array.words7),
    pack8(R.string.name8, R.string.description8, R.array.words8),
    pack9(R.string.name9, R.string.description9, R.array.words9),
    pack10(R.string.name10, R.string.description10, R.array.words10);

    val title: String @Composable get() = stringResource(titleId)
    val description: String @Composable get() = stringResource(descriptionId)
    val words: List<String> @Composable get() = stringArrayResource(wordsId).toMutableList()
    val wordsNum: Int @Composable get() = words.size
}