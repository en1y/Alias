package com.en1y.alias.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.en1y.alias.R
import com.en1y.alias.data.Team
import androidx.compose.foundation.shape.RoundedCornerShape
import com.en1y.alias.ui.theme.AliasIcon
import com.en1y.alias.ui.theme.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import com.en1y.alias.ui.theme.AliasButton
import com.en1y.alias.ui.theme.AliasCheckbox
import com.en1y.alias.ui.theme.AliasRadioButton
import com.en1y.alias.ui.theme.AliasSlider
import com.en1y.alias.ui.theme.AliasSwitch
import com.en1y.alias.ui.theme.AliasTheme
import com.en1y.alias.ui.theme.Overlay
import com.en1y.alias.ui.theme.Panel
import com.en1y.alias.ui.theme.aliasColors
import com.en1y.alias.ui.theme.screen
import com.en1y.alias.ui.theme.secondaryColors
import com.en1y.alias.ui.theme.verticalScrollWithBar
import androidx.core.net.toUri
import com.en1y.alias.data.Game
import com.en1y.alias.data.Round
import com.en1y.alias.data.WordPack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

private var step by mutableIntStateOf(0)

class GameActivity: AppCompatActivity(){
    private val game: Game by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) step = 0
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            var lastBackPress by remember { mutableLongStateOf(0L) }
            BackHandler {
                if (step == 0) {
                    val now = System.currentTimeMillis()
                    if (now - lastBackPress < 2000) finish()
                    else {
                        lastBackPress = now
                        Toast.makeText(this, R.string.press_back_again, Toast.LENGTH_SHORT).show()
                    }
                } else if(step in 10..20) {
                    step = 1
                } else if(step in 100..200) {
                    step = 10
                } else step--
            }

            AliasTheme { Crossfade(step, animationSpec = tween(250), label = "step") { s ->
                when (s) {
                    0 -> TeamSelect(game)
                    1 -> GameSettings(game)
                    2 -> PreRoundScreen(game = game)
                    3 -> RoundScreen(game)
                    4 -> PostRoundScreen(game)
                    10 -> AppSettings()
                    11 -> WordPacksSettings(game)
                    101 -> ChooseLanguageScreen(onNext = {step = 10})
                    102 -> ThemeChoosingScreen()
                }
            } }
        }
    }
}

@Composable
fun TeamIcon(team: Team = Team(), isRemovable: Boolean = true, highlighted: Boolean = false, remove: () -> Unit = {}) {
    Panel(color = if (highlighted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary) { Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Image(painter = team.teamImage, contentDescription = stringResource(R.string.team_image_description, team.teamName), colorFilter = ColorFilter.tint(LocalContentColor.current))
        Text(modifier = Modifier.weight(1f), text = team.teamName)
        Text(text = "%d".format(team.points), style = MaterialTheme.typography.titleMedium)
        if (isRemovable)
            IconButton(onClick = remove) {
                Icon(AliasIcon.Close, stringResource(R.string.remove_team))
            }
    } }
}

@Composable
fun TeamSelect(game: Game = remember { Game() }) {
    val tm = game.teamManager

    AliasTheme {
        Column(
            modifier = Modifier
                .screen(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.teams), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.teams_description), style = MaterialTheme.typography.bodyMedium)

            Panel(
                modifier = Modifier
                    .weight(1f)
                    .verticalScrollWithBar()
                    .padding(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                tm.teams.forEach { t -> key(t) { TeamIcon(team = t, isRemovable = !game.isGameActive, remove = { game.removeTeam(t) }) } }
            }
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
            ) {
                AliasButton(
                    enabled = !game.isGameActive,
                    onClick = {game.removeTeam()}) {
                    Text(stringResource(R.string.remove_team))
                }
                AliasButton(
                    onClick = {game.addTeam()}) {
                    Text(stringResource(R.string.add_team))
                }
            }
            AliasButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {step = 1}
            ) {
                Text(text = stringResource(R.string.continue_button))
            }
        }
    }
}

@Composable
fun ThemeChoosingScreen(modifier: Modifier = Modifier) {
    AliasTheme {
        Column(
            modifier = Modifier
                .screen(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            AliasButton(onClick = {step = 10}) {
                Icon(AliasIcon.Back, null)
            }
            Text(stringResource(R.string.theme), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.theme_description), style = MaterialTheme.typography.bodyMedium)
            // TODO: Implement theme color changing
        }
    }
}

@Composable
fun AppSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    AliasTheme {
        Column(
            modifier = Modifier
                .screen()
                .verticalScrollWithBar(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            AliasButton(onClick = {step = 1}) {
                Icon(AliasIcon.Back, null)
            }
            Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineSmall)

            Panel(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(stringResource(R.string.language))
                    AliasButton(onClick = {step = 101}, colors = ButtonDefaults.secondaryColors()) {
                        Icon(AliasIcon.Forward, null)
                    }
                }
                HorizontalDivider(thickness = 2.dp, color = LocalContentColor.current)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(stringResource(R.string.choose_theme))
                    AliasButton(onClick = {step = 102}, colors = ButtonDefaults.secondaryColors()) {
                        Icon(AliasIcon.Forward, null)
                    }
                }
            }
            Text(stringResource(R.string.external_links), style = MaterialTheme.typography.titleMedium)
            Panel(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(stringResource(R.string.github_page))
                    AliasButton(onClick = {
                        val urlIntent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/en1y/Alias".toUri()
                        )
                        context.startActivity(urlIntent)
                    }, colors = ButtonDefaults.secondaryColors()) {
                        Icon(AliasIcon.Forward, null)
                    }
                }
                HorizontalDivider(thickness = 2.dp, color = LocalContentColor.current)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(stringResource(R.string.creator_page))
                    AliasButton(onClick = {
                        val urlIntent = Intent(
                            Intent.ACTION_VIEW,
                            ("https://github.com/en1y").toUri()
                        )
                        context.startActivity(urlIntent)
                    }, colors = ButtonDefaults.secondaryColors()) {
                        Icon(AliasIcon.Forward, null)
                    }
                }
            }

        }
    }
}

@Composable
fun WordPacksSettings(
    game: Game = remember { Game() },
    modifier: Modifier = Modifier
){
    val s = game.settings
    val res = LocalContext.current.resources

    AliasTheme {
        Column(
            modifier = Modifier
                .screen()
                .verticalScrollWithBar(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            AliasButton(onClick = {step = 1}) {
                Icon(AliasIcon.Back, null)
            }
            Text(stringResource(R.string.word_packs), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.word_packs_description), style = MaterialTheme.typography.bodyMedium)
            @Composable fun WordPackElement(
                wordPack: WordPack,
                name: String,
                description: String,
                wordCount: Int
            ) {
                val selected = wordPack in s.wordPacks
                Panel(Modifier.clip(MaterialTheme.shapes.medium)
                    .toggleable(selected, role = Role.Checkbox) { if (it) s.wordPacks += wordPack else s.wordPacks -= wordPack }
                    .border(2.dp, if (selected) MaterialTheme.colorScheme.tertiary else Color.Transparent, MaterialTheme.shapes.medium).padding(16.dp)) { Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ){
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ){
                            Text(name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                            Text(pluralStringResource(R.plurals.words_count, wordCount, wordCount), style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                        }
                        Text(description, style = MaterialTheme.typography.bodyMedium)
                    }
                    AliasCheckbox(checked = selected, onCheckedChange = null)
                } }
            }
            WordPack.entries.forEach {
                WordPackElement(it, it.title, it.description, remember(it) { it.words(res).size })
            }
        }
    }
}

@Composable
fun GameSettings(
    game: Game = remember { Game() }
) {
    val s = game.settings

    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AliasTheme { Box {
        Column(
            modifier = Modifier
                .screen()
                .verticalScrollWithBar(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Panel { Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(modifier = Modifier.weight(1f), text = stringResource(R.string.game_settings), style = MaterialTheme.typography.titleLarge)
                AliasButton(onClick = {showDialog = true}, colors = ButtonDefaults.secondaryColors()) {
                    Icon(AliasIcon.Reset, stringResource(R.string.reset))
                }
                AliasButton(onClick = {step = 10}, colors = ButtonDefaults.secondaryColors()) {
                    Icon(AliasIcon.Settings, stringResource(R.string.settings))
                }
            } }
            AliasButton(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {step = 11}
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column (
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ){
                        Text(stringResource(R.string.word_packs), style = MaterialTheme.typography.titleMedium)
                        if (s.wordPacks.isEmpty())
                            Text(stringResource(R.string.choose_word_packs), style = MaterialTheme.typography.bodyMedium)
                        else
                            Text(stringResource(R.string.selected_word_packs, s.wordPacks.size), style = MaterialTheme.typography.bodyMedium)
                    }
                    Icon(AliasIcon.Forward, null)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(stringResource(R.string.teams), style = MaterialTheme.typography.titleMedium)
                    AliasButton(onClick = {step = 0}) {
                        Text(stringResource(R.string.edit))
                    }
                }
                Column (
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)

                ){
                    s.tm.teams.forEach { t -> key(t) { TeamIcon(team = t, isRemovable = false)} }
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ){
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.round_length))
                    Text(stringResource(R.string.seconds_short, s.timePerRound), style = MaterialTheme.typography.titleMedium)
                }
                AliasSlider(
                    value = s.timePerRound.toFloat(),
                    onValueChange = {s.timePerRound = it.roundToInt()},
                    steps = 6,
                    valueRange = 30f..240f)

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.words_to_win))
                    Text(pluralStringResource(R.plurals.words_count, s.wordsWinCondition, s.wordsWinCondition), style = MaterialTheme.typography.titleMedium)
                }
                AliasSlider(
                    value = s.wordsWinCondition.toFloat(),
                    onValueChange = {s.wordsWinCondition = it.roundToInt()},
                    steps = 8,
                    valueRange = 25f..250f)
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ){
                Text(stringResource(R.string.rules), style = MaterialTheme.typography.titleMedium)
                Panel(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ){
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Column(
                            modifier = Modifier
                                .weight(1f)
                        ){
                            Text(stringResource(R.string.last_word), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.last_word_description), style = MaterialTheme.typography.bodyMedium)
                        }
                        AliasSwitch(checked = s.lastWordCanBeGuessed, onCheckedChange = {s.lastWordCanBeGuessed = it})
                    }
                    HorizontalDivider(thickness = 2.dp, color = LocalContentColor.current)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Column(Modifier.weight(1f)){
                            Text(stringResource(R.string.skipped_word), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.skipped_word_description), style = MaterialTheme.typography.bodyMedium)
                        }
                        AliasSwitch(checked = s.skippedWordSubtractsPoints, onCheckedChange = {s.skippedWordSubtractsPoints = it})
                    }
                }
            }
            var errorLabel by remember { mutableStateOf("") }
            var errorVisible by remember { mutableStateOf(false) }
            if (errorVisible)
                Text(errorLabel, color = MaterialTheme.colorScheme.error)
            AliasButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (s.canStartUpGame()) {
                        game.start(s.wordPacks.flatMap { it.words(context.resources) })
                        step = 2
                    } else {
                        errorLabel = s.gameStartupCheck(context)
                        errorVisible = true
                    }
                }
            ) {
                Text(text = stringResource(R.string.start_game))
            }
        }
        NewGameDialog(showDialog, { showDialog = false }) {
            game.reset(s.wordPacks.flatMap { it.words(context.resources) })
            showDialog = false
        }
    } }
}

@Composable
fun BoxScope.NewGameDialog(visible: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) = Overlay(visible, onDismiss) {
    Text(stringResource(R.string.new_game_title), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
    Text(stringResource(R.string.new_game_description), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
    AliasButton(onConfirm, Modifier.fillMaxWidth(), colors = ButtonDefaults.secondaryColors()) {
        Text(stringResource(R.string.new_game))
    }
    AliasButton(onDismiss, Modifier.fillMaxWidth(), colors = ButtonDefaults.secondaryColors()) {
        Text(stringResource(R.string.cancel))
    }
}

@Composable
fun PreRoundScreen(
    game: Game = remember { Game().apply { start(listOf("a")) } }
) {
    val t = game.gameOver()
    val team: Team = remember { game.getCurrentTeam() }
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    AliasTheme { Box {
        Column(
            modifier = Modifier
                .screen()
                .verticalScrollWithBar(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp), Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    AliasButton({
                        showDialog = true
                    }) {
                        Text(stringResource(R.string.end_game))
                    }
                    Text(stringResource(R.string.round_number, game.getRound()))
                }
                Image(painter = team.teamImage, contentDescription = stringResource(R.string.team_image_description, team.teamName), modifier = Modifier.size(96.dp))

                if (t != null) {
                    Text(stringResource(R.string.team_won, t.teamName), style = MaterialTheme.typography.headlineSmall)
                } else {
                    Text(stringResource(R.string.next_team), style = MaterialTheme.typography.bodyMedium)
                    Text(team.teamName, style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.hand_phone), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(stringResource(R.string.standings), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.to_points, game.settings.wordsWinCondition), style = MaterialTheme.typography.bodyMedium)
                }
                Column (
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)

                ){
                    game.teamManager.teams.forEach { key(it) { TeamIcon(team = it, isRemovable = false, highlighted = it == (t ?: team))} }
                }
                AliasButton(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), onClick = {
                    if (t == null) { game.createRound(); step = 3 }
                    else { game.isGameActive = false; step = 1 }
                }) {
                    if (t == null) Text(stringResource(R.string.start_round))
                    else Text(stringResource(R.string.back_to_menu))
                }
            }
        }
        NewGameDialog(showDialog, { showDialog = false }) {
            game.reset(game.settings.wordPacks.flatMap { it.words(context.resources) })
            showDialog = false
            step = 1
        }
    } }
}

@Composable
fun CountdownTimer(initialSeconds: Int = 60, isRunning: Boolean = false, onFinish: () -> Unit = {}) {
    var remainingSeconds by remember { mutableIntStateOf(initialSeconds) }

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L.milliseconds)
            remainingSeconds--
        } else if (isRunning && remainingSeconds == 0) {
            onFinish()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%d:%02d".format(remainingSeconds / 60, remainingSeconds % 60),
            color = if (remainingSeconds < 10) MaterialTheme.aliasColors.skipped else Color.Unspecified,
            style = MaterialTheme.typography.displayMedium
        )
        val rawProgress = if (initialSeconds > 0) {
            remainingSeconds.toFloat() / initialSeconds.toFloat()
        } else {
            0f
        }

        val animatedProgress by animateFloatAsState(
            targetValue = rawProgress,
            animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
            label = "TimerProgress"
        )

        LinearProgressIndicator(
            progress = { animatedProgress },
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

/** Text whose leading arrow is drawn bold in [color]. */
@Composable
private fun ArrowHint(text: String, color: Color, modifier: Modifier = Modifier, style: TextStyle = LocalTextStyle.current) = Text(
    buildAnnotatedString { withStyle(SpanStyle(color, fontWeight = FontWeight.Bold)) { append(text.take(1)) }; append(text.drop(1)) },
    modifier, style = style
)

@Composable @Preview
fun RoundScreen(
    game: Game = remember { Game().apply { start(listOf("a")); createRound() } }
) {
    val round = game.currentRound ?: return

    @Composable
    fun WordCard(modifier: Modifier, word: String = stringResource(R.string.word), counter: Int = 0, color: Color = MaterialTheme.colorScheme.primaryContainer) {
        Box(modifier.fillMaxWidth(0.85f).aspectRatio(1.5f)) { Panel(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            color = color,
            shape = RoundedCornerShape(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(counter.toString(), style = MaterialTheme.typography.titleMedium)
                ArrowHint(stringResource(R.string.guessed_hint), MaterialTheme.aliasColors.guessed, style = MaterialTheme.typography.titleMedium)
            }
            Text(word, Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineLarge)
            ArrowHint(stringResource(R.string.skipped_hint), MaterialTheme.aliasColors.skipped, Modifier.align(Alignment.End), MaterialTheme.typography.titleMedium)
        } }
    }

    var isRunning by remember { mutableStateOf(false) }
    var started by remember { mutableStateOf(false) }
    var showLastWordGuessedByOtherTeams by remember { mutableStateOf(false) }
    var showExit by remember { mutableStateOf(false) }

    BackHandler {
        if (started && isRunning) {
            showExit = true
            isRunning = false
        }
    }

    val skip: () -> Unit = { round.skipped(); if (!isRunning) { step = 4; round.end() } }
    val guess: () -> Unit = {
        if (!isRunning && round.gameSetting.lastWordCanBeGuessed) showLastWordGuessedByOtherTeams = true
        else { round.guessed(); if (!isRunning) { round.end(); step = 4 } }
    }

    val drag = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val scope = rememberCoroutineScope()
    val limit = with(LocalDensity.current) { 120.dp.toPx() }
    val progress = (drag.value.y / limit).coerceIn(-1f, 1f)
    fun fling(up: Boolean, from: Offset = Offset(0f, if (up) -1f else 1f)) = scope.launch {
        if (drag.isRunning) drag.snapTo(Offset.Zero)
        // a newer tap or drag cancels this animation, the word still has to count
        try { drag.animateTo(from * (3 * limit / from.getDistance()), tween(200)) }
        finally { if (up) guess() else skip() }
        drag.snapTo(Offset.Zero)
    }

    AliasTheme {
        Box {
        Column(
            modifier = Modifier.screen(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp), Alignment.CenterVertically) {
                AliasButton({
                    showExit = true
                    isRunning = false
                }) {
                    Icon(AliasIcon.Back, null)
                }
                Text(round.currentTeam.teamName, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.points_short, round.countPoints()))
            }
            CountdownTimer(round.gameSetting.timePerRound, isRunning = isRunning, onFinish = {isRunning = false})
            if (started && !isRunning)
                Text(stringResource(R.string.last_word), Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.titleMedium)
            val c = MaterialTheme.aliasColors
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                key(round.nextWord) {
                    val appear = remember { Animatable(0f) }
                    LaunchedEffect(Unit) { appear.animateTo(1f, tween(350)) }
                    WordCard(Modifier.graphicsLayer {
                        scaleX = 0.92f + 0.08f * abs(progress); scaleY = scaleX
                        translationY = 12.dp.toPx() * (1 - abs(progress))
                        alpha = appear.value
                    }, if (started) round.nextWord else "", round.counter + 1)
                }
                WordCard(
                    Modifier
                        .pointerInput(started) {
                            if (started) detectDragGestures(
                                onDragStart = { scope.launch { drag.snapTo(Offset.Zero) } },
                                onDragEnd = {
                                    val v = drag.value
                                    if (abs(v.y) > limit / 2) fling(v.y < 0, v)
                                    else scope.launch { drag.animateTo(Offset.Zero) }
                                },
                                onDragCancel = { scope.launch { drag.animateTo(Offset.Zero) } }
                            ) { change, amount -> change.consume(); scope.launch { drag.snapTo(drag.value + amount) } }
                        }
                        .graphicsLayer {
                            translationX = drag.value.x
                            translationY = drag.value.y
                            rotationZ = drag.value.x / limit * 6f
                            alpha = 1f - ((drag.value.getDistance() - 1.5f * limit) / limit).coerceIn(0f, 1f)
                        },
                    if (started) round.currentWord else stringResource(R.string.word),
                    round.counter,
                    lerp(MaterialTheme.colorScheme.primaryContainer, if (progress < 0) c.guessed else c.skipped, abs(progress) * 0.7f)
                )
            }
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp), Alignment.CenterVertically) {
                AliasButton({ fling(false) }, Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.skipped, contentColor = MaterialTheme.colorScheme.surface)) {
                    Text(stringResource(R.string.skip), style = MaterialTheme.typography.titleMedium)
                }
                Text(round.countPoints().toString(), Modifier.widthIn(min = 48.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineMedium)
                AliasButton({ fling(true) }, Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.guessed, contentColor = MaterialTheme.colorScheme.surface)) {
                    Text(stringResource(R.string.guessed), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        Overlay(!started, alignment = Alignment.BottomCenter) {
            Text(stringResource(R.string.how_to_play), style = MaterialTheme.typography.headlineSmall)
            ArrowHint(stringResource(R.string.pull_up_hint), MaterialTheme.aliasColors.guessed, Modifier.fillMaxWidth())
            ArrowHint(stringResource(R.string.pull_down_hint), MaterialTheme.aliasColors.skipped, Modifier.fillMaxWidth())
            AliasButton({ started = true; isRunning = true; round.start() }, Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.secondaryColors()) {
                Text(stringResource(R.string.start))
            }
        }
        Overlay(showLastWordGuessedByOtherTeams) {
            Text(stringResource(R.string.choose_guessing_team), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            var t: Team? by remember { mutableStateOf(round.currentTeam) }
            (game.teamManager.teams + null).forEach {
                Row(Modifier.fillMaxWidth().selectable(t == it, role = Role.RadioButton) { t = it }, verticalAlignment = Alignment.CenterVertically) {
                    AliasRadioButton(selected = t == it, onClick = null)
                    Text(it?.teamName ?: stringResource(R.string.no_team_guessed))
                }
            }
            AliasButton({ t?.addPoints(1); step = 4; round.end() }, Modifier.fillMaxWidth(), colors = ButtonDefaults.secondaryColors()) {
                Text(stringResource(R.string.continue_button))
            }
        }
        Overlay(showExit, { showExit = false; isRunning = true }) {
            Text(stringResource(R.string.end_round_confirm), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                AliasButton({ showExit = false; isRunning = true }, Modifier.weight(1f), colors = ButtonDefaults.secondaryColors()) {
                    Text(stringResource(R.string.cancel))
                }
                AliasButton({ round.end(); showExit = false; step = 4 }, Modifier.weight(1f), colors = ButtonDefaults.secondaryColors()) {
                    Text(stringResource(R.string.confirm))
                }
            }
        }
    } }
}

@Composable @Preview
fun PostRoundScreen(
    game: Game = remember { Game() }
) {

    AliasTheme {
        Column(
            modifier = Modifier
                .screen(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            val round = game.currentRound
            if (round != null) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp), Alignment.CenterVertically) {
                        AliasButton({}) {
                            Icon(AliasIcon.Back, null)
                        }
                        Image(
                            painter = round.currentTeam.teamImage,
                            contentDescription = stringResource(R.string.team_image_description, round.currentTeam.teamName)
                        )
                        Text(round.currentTeam.teamName, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.round_number, game.getRound()))
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.round_review), style = MaterialTheme.typography.headlineSmall)
                            Text(stringResource(R.string.review_guesses), style = MaterialTheme.typography.bodyMedium)
                        }
                        // roundWords is a state map, so this recomputes whenever a status is toggled
                        val points = round.countPoints()
                        Text("${if (points > 0) "+" else ""}$points", style = MaterialTheme.typography.headlineSmall)
                    }
                    Column(Modifier.weight(1f).verticalScrollWithBar(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        round.roundWords.forEach { (string, status) ->
                            Panel(Modifier.padding(start = 16.dp)) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(string, Modifier.weight(1f))
                                val c = MaterialTheme.aliasColors
                                val surface = MaterialTheme.colorScheme.surface
                                @Composable fun fill(s: Round.GuessStatus, color: Color) = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (status == s) color else Color.Transparent,
                                    contentColor = if (status == s) surface else LocalContentColor.current
                                )
                                IconButton({
                                    round.updateByWord(string, Round.GuessStatus.Guessed)
                                }, colors = fill(Round.GuessStatus.Guessed, c.guessed)) { Icon(AliasIcon.Check, stringResource(R.string.guessed)) }
                                IconButton({
                                    round.updateByWord(string, Round.GuessStatus.Skipped)
                                }, colors = fill(Round.GuessStatus.Skipped, c.skipped)) { Icon(AliasIcon.Close, stringResource(R.string.skip)) }
                                IconButton({
                                    round.updateByWord(string, Round.GuessStatus.Deleted)
                                }, colors = fill(Round.GuessStatus.Deleted, c.deleted)) { Icon(AliasIcon.Delete, null) }
                            } }

                        }
                    }
                    AliasButton({
                        game.endRound()
                        step = 2
                    }, Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.next))
                    }
                }
            }
            else {
                Text(stringResource(R.string.game_error))
            }
        }
    }
}