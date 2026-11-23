package com.en1y.alias.ui.theme

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------- Colors

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2E2E2E),
    onPrimary = Color(0xFFE0E0E0),
    secondary = Color(0xFF4A4A4A),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF8A8A8A),
    onTertiary = Color(0xFF000000),
    primaryContainer = Color(0xFF3A3A3A),
    onPrimaryContainer = Color(0xFFE0E0E0),
    secondaryContainer = Color(0xFF5C5C5C),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF6E6E6E),
    onTertiaryContainer = Color(0xFFFFFFFF),
    background = Color(0xFF121212),
    onBackground = Color(0xFFEDEDED),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFEDEDED),
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000),
    outline = Color(0xFF8A8A8A),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFC2C2C2),
    onPrimary = Color(0xFF1F1F1F),
    secondary = Color(0xFF9E9E9E),
    onSecondary = Color(0xFF000000),
    tertiary = Color(0xFF757575),
    onTertiary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6D6D6),
    onPrimaryContainer = Color(0xFF1F1F1F),
    secondaryContainer = Color(0xFFB5B5B5),
    onSecondaryContainer = Color(0xFF000000),
    tertiaryContainer = Color(0xFF8F8F8F),
    onTertiaryContainer = Color(0xFF000000),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1C1C),
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF1C1C1C),
    error = Color(0xFFB00020),
    onError = Color(0xFFFFFFFF),
    outline = Color(0xFF757575),
)

/**
 * Colors Material doesn't have a slot for. To add one: add a field here,
 * give it a value in BOTH instances below, read it with `MaterialTheme.aliasColors.<name>`.
 */
@Immutable
data class AliasColors(
    val guessed: Color,
    val skipped: Color,
    val deleted: Color,
)

private val DarkAliasColors = AliasColors(
    guessed = Color(0xFF81C784),
    skipped = Color(0xFFE57373),
    deleted = Color(0xFF9E9E9E),
)

private val LightAliasColors = AliasColors(
    guessed = Color(0xFF2E7D32),
    skipped = Color(0xFFC62828),
    deleted = Color(0xFF616161),
)

private val LocalAliasColors = staticCompositionLocalOf { LightAliasColors }

val MaterialTheme.aliasColors: AliasColors
    @Composable @ReadOnlyComposable get() = LocalAliasColors.current

// ---------------------------------------------------------------- Shapes

val AliasShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
)

// ---------------------------------------------------------------- Theme

@Composable
fun AliasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    // false = no ripple on any Button/Checkbox/Switch/... inside this theme
    ripple: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, shapes = AliasShapes) {
        CompositionLocalProvider(
            LocalAliasColors provides if (darkTheme) DarkAliasColors else LightAliasColors,
            LocalRippleConfiguration provides if (ripple) LocalRippleConfiguration.current else null,
        ) {
            // paints the screen background and makes every Text/Icon default to onBackground
            Surface(color = MaterialTheme.colorScheme.background, content = content)
        }
    }
}

// ---------------------------------------------------------------- Layout defaults

/** Root column of every screen. Top padding leaves room for the camera/status bar. */
fun Modifier.screen() = fillMaxSize().padding(16.dp).padding(top = 30.dp)

/** Scrolls vertically and draws a thin scrollbar that fades while idle. Color defaults to the current text color. */
@Composable
fun Modifier.verticalScrollWithBar(
    state: ScrollState = rememberScrollState(),
    color: Color = LocalContentColor.current,
    width: Dp = 3.dp,
): Modifier {
    val alpha by animateFloatAsState(if (state.isScrollInProgress) 0.8f else 0.3f, label = "scrollbarAlpha")
    return drawWithContent {
        drawContent()
        if (state.maxValue == 0) return@drawWithContent // content fits, no scrollbar
        val w = width.toPx()
        val barHeight = (size.height * size.height / (size.height + state.maxValue)).coerceAtLeast(24.dp.toPx())
        val y = state.value.toFloat() / state.maxValue * (size.height - barHeight)
        // drawn in the padding right of the content so it never covers it; callers need ~10dp free there
        drawRoundRect(color, Offset(size.width + 6.dp.toPx(), y), Size(w, barHeight), CornerRadius(w / 2), alpha = alpha)
    }.verticalScroll(state)
}

/** A colored block (primary by default). Text/Icons inside automatically get the matching "on" color. */
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = MaterialTheme.shapes.medium,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    CompositionLocalProvider(LocalContentColor provides contentColorFor(color)) {
        Column(
            Modifier.fillMaxWidth().background(color, shape).then(modifier),
            verticalArrangement,
            horizontalAlignment,
            content
        )
    }
}

/** Dims and blocks the screen behind it while [visible]; the card rises and fades in. Back and tapping outside call [onDismiss]. */
@Composable
fun BoxScope.Overlay(
    visible: Boolean,
    onDismiss: () -> Unit = {},
    scrim: Color = Color.Black.copy(alpha = 0.6f),
    alignment: Alignment = Alignment.Center,
    content: @Composable ColumnScope.() -> Unit
) {
    BackHandler(visible, onDismiss)
    val state = remember { MutableTransitionState(visible) }.apply { targetState = visible }
    AnimatedVisibility(state, Modifier.matchParentSize(), enter = fadeIn(), exit = fadeOut()) {
        Box(Modifier.fillMaxSize().background(scrim).pointerInput(Unit) { detectTapGestures { onDismiss() } }.navigationBarsPadding(), alignment) {
            Box(Modifier.padding(16.dp).animateEnterExit(slideInVertically { it / 3 }, slideOutVertically { it / 3 }).pointerInput(Unit) { detectTapGestures { } }) {
                Panel(Modifier.padding(24.dp), shape = MaterialTheme.shapes.large, verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally, content = content)
            }
        }
    }
}

// ---------------------------------------------------------------- Component defaults

/** Use inside a primary Panel so the button doesn't blend in. */
@Composable
fun ButtonDefaults.secondaryColors() = buttonColors(
    containerColor = MaterialTheme.colorScheme.secondary,
    contentColor = MaterialTheme.colorScheme.onSecondary
)

/** Button that shrinks while pressed. pressScale = 1f turns the animation off. */
@Composable
fun AliasButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    pressScale: Float = 0.95f,
    shape: Shape = ButtonDefaults.shape,
    content: @Composable RowScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) pressScale else 1f, label = "pressScale")
    Button(
        onClick = onClick,
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        enabled = enabled,
        colors = colors,
        shape = shape,
        interactionSource = interaction,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliasSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    colors: SliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.secondary,
        activeTrackColor = MaterialTheme.colorScheme.secondary,
        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
    ),
) = Slider(
    value, onValueChange, modifier.padding(horizontal = 10.dp), steps = steps, valueRange = valueRange, colors = colors,
    thumb = { Box(Modifier.size(24.dp).background(colors.thumbColor, CircleShape)) },
    track = { SliderDefaults.Track(it, modifier = Modifier.height(6.dp), colors = colors, drawStopIndicator = null, thumbTrackGapSize = 0.dp) },
)

@Composable
fun AliasSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    colors: SwitchColors = SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.onSecondary,
        checkedTrackColor = MaterialTheme.colorScheme.secondary,
        uncheckedThumbColor = MaterialTheme.colorScheme.tertiary,
        uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer,
        uncheckedBorderColor = MaterialTheme.colorScheme.tertiary,
    ),
) = Switch(checked, onCheckedChange, modifier, colors = colors)

@Composable
fun AliasCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    colors: CheckboxColors = CheckboxDefaults.colors(
        checkedColor = MaterialTheme.colorScheme.secondary,
        checkmarkColor = MaterialTheme.colorScheme.onSecondary,
        uncheckedColor = LocalContentColor.current,
    ),
) = Checkbox(checked, onCheckedChange, modifier, colors = colors)

@Composable
fun AliasRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    colors: RadioButtonColors = RadioButtonDefaults.colors(
        selectedColor = MaterialTheme.colorScheme.secondary,
        unselectedColor = LocalContentColor.current,
    ),
) = RadioButton(selected, onClick, modifier, colors = colors)
