package com.en1y.alias.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.material3.ColorScheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.en1y.alias.R
import kotlinx.coroutines.launch

// ---------------------------------------------------------------- Colors

/** Builds a scheme from 5 flat colors; text colors are the accent nearly black or nearly white, whichever reads better. */
private fun scheme(dark: Boolean, bg: Long, panel: Long, card: Long, accent: Long, highlight: Long): ColorScheme {
    val (b, p, k, a, h) = listOf(bg, panel, card, accent, highlight).map { Color(it) }
    val darkInk = lerp(a, Color.Black, 0.85f)
    val lightInk = lerp(a, Color.White, 0.9f)
    fun on(c: Color) = if (c.luminance() > 0.179f) darkInk else lightInk
    return (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = p, onPrimary = on(p),
        primaryContainer = k, onPrimaryContainer = on(k),
        secondary = a, onSecondary = on(a),
        secondaryContainer = lerp(p, a, 0.35f), onSecondaryContainer = on(p),
        tertiary = h, onTertiary = on(h),
        background = b, onBackground = on(b),
        surface = b, onSurface = on(b),
        outline = lerp(on(b), b, 0.5f),
    )
}

enum class Palette(@param:StringRes val title: Int, val light: ColorScheme, val dark: ColorScheme) {
    Peach(
        R.string.palette_peach,
        scheme(false, 0xFFFFF6F0, 0xFFFFE3D2, 0xFFFFFFFF, 0xFFF7A47E, 0xFFE2703F),
        scheme(true, 0xFF1C1310, 0xFF33241E, 0xFF45312A, 0xFFE89470, 0xFFFFC9AE)
    ),
    Purple(
        R.string.palette_purple,
        scheme(false, 0xFFF8F5FF, 0xFFEBE3FF, 0xFFFFFFFF, 0xFF9B7BFF, 0xFF6B4CE6),
        scheme(true, 0xFF15112B, 0xFF262046, 0xFF342B5E, 0xFF9B7BFF, 0xFFD4C6FF)
    ),
    Blue(
        R.string.palette_blue,
        scheme(false, 0xFFF2F8FF, 0xFFDCEBFF, 0xFFFFFFFF, 0xFF6AA8FF, 0xFF2D6FD6),
        scheme(true, 0xFF0E1626, 0xFF1B2A44, 0xFF253858, 0xFF5B9BF5, 0xFFB8D6FF)
    ),
    Red(
        R.string.palette_red,
        scheme(false, 0xFFFFF4F4, 0xFFFFDDDD, 0xFFFFFFFF, 0xFFF47C7C, 0xFFC93A3A),
        scheme(true, 0xFF1E0F10, 0xFF3A1D1F, 0xFF4C2729, 0xFFE86A6A, 0xFFFFC2C2)
    ),
    Yellow(
        R.string.palette_yellow,
        scheme(false, 0xFFFFFBEB, 0xFFFFF0C2, 0xFFFFFFFF, 0xFFFFCC4D, 0xFFE0A100),
        scheme(true, 0xFF1A160A, 0xFF332B12, 0xFF453A1A, 0xFFF2C14E, 0xFFFFE39A)
    ),
    BlackAndWhite(
        R.string.palette_black_white,
        scheme(false, 0xFFFFFFFF, 0xFFE8E8E8, 0xFF000000, 0xFF000000, 0xFF7A7A7A),
        scheme(true, 0xFF000000, 0xFF1C1C1C, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF8A8A8A)
    ),
}

enum class ThemeMode { Light, System, Dark }

/** The chosen theme, kept in the "settings" preferences. Call [load] before setContent. */
object ThemeSettings {
    var mode by mutableStateOf(ThemeMode.System); private set
    var palette by mutableStateOf(Palette.Peach); private set
    private var prefs: SharedPreferences? = null

    fun load(context: Context) {
        val p = context.getSharedPreferences("settings", Context.MODE_PRIVATE).also { prefs = it }
        mode = ThemeMode.entries.find { it.name == p.getString("themeMode", null) } ?: ThemeMode.System
        palette = Palette.entries.find { it.name == p.getString("palette", null) } ?: Palette.Peach
    }

    fun update(mode: ThemeMode = this.mode, palette: Palette = this.palette) {
        this.mode = mode
        this.palette = palette
        prefs?.edit { putString("themeMode", mode.name); putString("palette", palette.name) }
    }

    @Composable
    fun isDark() = when (mode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }
}

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
    darkTheme: Boolean = ThemeSettings.isDark(),
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

        darkTheme -> ThemeSettings.palette.dark
        else -> ThemeSettings.palette.light
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, shapes = AliasShapes) {
        CompositionLocalProvider(
            LocalAliasColors provides if (darkTheme) DarkAliasColors else LightAliasColors,
            LocalRippleConfiguration provides if (ripple) LocalRippleConfiguration.current else null,
            LocalDensity provides LocalDensity.current.let { Density(it.density, it.fontScale.coerceAtMost(1.3f)) },
        ) {
            // paints the screen background and makes every Text/Icon default to onBackground
            Surface(color = MaterialTheme.colorScheme.background, content = content)
        }
    }
}

// ---------------------------------------------------------------- Layout defaults

/** Root column of every screen. Insets keep content clear of the status bar, camera cutout and nav bar. */
fun Modifier.screen() = fillMaxSize()
    .safeDrawingPadding()
    .padding(16.dp)

/** Scrolls vertically and draws a thin scrollbar that fades while idle. Color defaults to the current text color. */
@Composable
fun Modifier.verticalScrollWithBar(
    state: ScrollState = rememberScrollState(),
    color: Color = LocalContentColor.current,
    width: Dp = 3.dp,
) = scrollBar(state, color, width).verticalScroll(state)

/** Just the bar, for lists that scroll on their own: LazyColumn(Modifier.scrollBar(listState), listState). */
@Composable
fun Modifier.scrollBar(
    state: ScrollableState,
    color: Color = LocalContentColor.current,
    width: Dp = 3.dp,
    dragging: Boolean = false,
): Modifier {
    val alpha by animateFloatAsState(if (dragging || state.isScrollInProgress) 0.8f else 0.3f, label = "scrollbarAlpha")
    return drawWithContent {
        drawContent()
        val (y, barHeight) = thumb(state, size.height) ?: return@drawWithContent // content fits, no scrollbar
        val w = (if (dragging) width * 2 else width).toPx()
        // drawn in the padding right of the content so it never covers it
        drawRoundRect(
            color,
            Offset(size.width + 7.5.dp.toPx() - w / 2, y),
            Size(w, barHeight),
            CornerRadius(w / 2),
            alpha = alpha
        )
    }
}

/** Top and height of the [scrollBar] line on a track [trackHeight] px tall, or null when the content fits. */
private fun Density.thumb(state: ScrollableState, trackHeight: Float): Pair<Float, Float>? {
    val s = state.scrollIndicatorState ?: return null
    val max = s.contentSize - s.viewportSize
    if (max <= 0) return null
    val barHeight = (trackHeight * s.viewportSize / s.contentSize).coerceAtLeast(24.dp.toPx())
    return (s.scrollOffset.toFloat() / max).coerceIn(0f, 1f) * (trackHeight - barHeight) to barHeight
}

/**
 * Lets you grab the [scrollBar] line of a lazy list and drag it. Put it in a Box together with the list:
 * it covers the line in the padding right of the list. [onDrag] reports when a drag starts and ends.
 */
@Composable
fun BoxScope.ScrollBarHandle(state: LazyListState, onDrag: (Boolean) -> Unit) {
    val scope = rememberCoroutineScope()
    Box(
        Modifier
            .align(Alignment.TopEnd)
            .offset(x = 18.dp)
            .width(24.dp)
            .fillMaxHeight()
            .pointerInput(state) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val (top, bar) = thumb(state, size.height.toFloat()) ?: return@awaitEachGesture
                    if (down.position.y - top !in -12.dp.toPx()..bar + 12.dp.toPx()) return@awaitEachGesture // missed the line
                    val grab = down.position.y - top // keeps the line under the finger where it was grabbed
                    onDrag(true)
                    drag(down.id) { change ->
                        change.consume()
                        val s = state.scrollIndicatorState ?: return@drag
                        val f = ((change.position.y - grab) / (size.height - bar)).coerceIn(0f, 1f)
                        val rowHeight = s.contentSize.toFloat() / state.layoutInfo.totalItemsCount
                        scope.launch { state.scrollToItem((f * (s.contentSize - s.viewportSize) / rowHeight).toInt()) }
                    }
                    onDrag(false)
                }
            })
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
            Modifier
                .fillMaxWidth()
                .background(color, shape)
                .then(modifier),
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
        Box(
            Modifier
                .fillMaxSize()
                .background(scrim)
                .pointerInput(Unit) { detectTapGestures { onDismiss() } }
                .navigationBarsPadding(), alignment) {
            Box(
                Modifier
                    .padding(16.dp)
                    .animateEnterExit(slideInVertically { it / 3 }, slideOutVertically { it / 3 })
                    .pointerInput(Unit) { detectTapGestures { } }) {
                Panel(
                    Modifier.padding(24.dp),
                    shape = MaterialTheme.shapes.large,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = content
                )
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
    colors: ButtonColors = ButtonDefaults.secondaryColors(),
    pressScale: Float = 0.95f,
    shape: Shape = ButtonDefaults.shape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
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
        contentPadding = contentPadding,
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
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.secondary,
        activeTrackColor = MaterialTheme.colorScheme.secondary,
        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
    ),
) = Slider(
    value, onValueChange, modifier.padding(horizontal = 10.dp), steps = steps, valueRange = valueRange, colors = colors,
    onValueChangeFinished = onValueChangeFinished,
    thumb = {
        Box(
            Modifier
                .size(24.dp)
                .background(colors.thumbColor, CircleShape)
        )
    },
    track = {
        SliderDefaults.Track(
            it,
            modifier = Modifier.height(6.dp),
            colors = colors,
            drawStopIndicator = null,
            thumbTrackGapSize = 0.dp
        )
    },
)

@Composable
fun AliasSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    colors: SwitchColors = SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.onSecondary,
        checkedTrackColor = MaterialTheme.colorScheme.secondary,
        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
        uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer,
        uncheckedBorderColor = MaterialTheme.colorScheme.outline,
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
