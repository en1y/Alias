package com.en1y.alias.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.en1y.alias.R

/** All app icons. To add one: drop a vector into res/drawable and add an entry here. */
enum class AliasIcon(@param:DrawableRes val res: Int) {
    Back(R.drawable.ic_chevron_left),
    Forward(R.drawable.ic_chevron_right),
    Settings(R.drawable.ic_settings),
    Reset(R.drawable.ic_reset),
    Check(R.drawable.ic_check),
    Close(R.drawable.ic_close),
    Delete(R.drawable.ic_delete),
}

/** `Icon(AliasIcon.Back, null)`, tinted with the surrounding text color by default. */
@Composable
fun Icon(
    icon: AliasIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) = Icon(painterResource(icon.res), contentDescription, modifier, tint)
