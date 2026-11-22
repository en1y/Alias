package com.en1y.alias.ui.activities

import android.os.Bundle
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.en1y.alias.R
import com.en1y.alias.data.Team
import com.en1y.alias.manager.TeamManager
import com.en1y.alias.ui.theme.AliasTheme
import kotlin.math.round

class GameActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var step by rememberSaveable { mutableIntStateOf(0) }
            val tm = remember { TeamManager() }

            var lastBackPress by remember { mutableLongStateOf(0L) }
            BackHandler {
                if (step == 0) {
                    val now = System.currentTimeMillis()
                    if (now - lastBackPress < 2000) finish()
                    else {
                        lastBackPress = now
                        Toast.makeText(this, R.string.press_back_again, Toast.LENGTH_SHORT).show()
                    }
                } else step--
            }

            when (step) {
                0 -> TeamSelect(onNext = {step = 1}, tm = tm)
                1 -> GameSettings(onNext = {step = 1}, tm = tm)
            }
        }
    }
}

@Composable
fun TeamIcon(team: Team = Team(), isRemovable: Boolean = true, remove: () -> Unit = {}) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
            .padding(15.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Image(painter = team.teamImage, contentDescription = "Team %s image".format(team.teamName))
        Text(modifier = Modifier.weight(1f),
            text = team.teamName,
            color = MaterialTheme.colorScheme.onSecondary)
        Text(text = "%d".format(team.points), color = MaterialTheme.colorScheme.onSecondary)
        if (isRemovable)
            Button(
                onClick = remove,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )){
                Text("x")
            }
    }
}

@Composable
fun TeamSelect(modifier: Modifier = Modifier, tm: TeamManager = remember { TeamManager() }, onNext: () -> Unit = {}) {
    val scrollState = rememberScrollState()

    AliasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(all = 16.dp)
                .padding(top = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Teams", color = MaterialTheme.colorScheme.onBackground)
            Text("Add and remove teams to prepare for your game", color = MaterialTheme.colorScheme.onBackground)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary)
                    .verticalScrollbar(scrollState, MaterialTheme.colorScheme.onPrimary)
                    .padding(all = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                tm.teams.forEach { t -> key(t) { TeamIcon(team = t, remove = { tm.removeTeam(t) }) } }
            }
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
            ) {
                Button(
                    onClick = {tm.removeTeam()},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )) {
                    Text("Remove team")
                }
                Button(
                    onClick = {tm.addTeam()},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )) {
                    Text("Add team")
                }
            }
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = stringResource(R.string.continue_button))
            }
        }
    }
}

@Composable
fun GameSettings(modifier: Modifier = Modifier, tm: TeamManager = remember { TeamManager() }, onNext: () -> Unit = {}) {
    var roundLengthSliderPosition by remember { mutableFloatStateOf(60f) }
    var wordsRequiredForWinningSliderPosition by remember { mutableFloatStateOf(100f) }
    var lastWordSetting by remember { mutableStateOf(false) }
    var skippedWordSetting by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    AliasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollbar(scrollState, MaterialTheme.colorScheme.onBackground)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
                .padding(all = 16.dp)
                .padding(top = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(modifier = Modifier.padding(end = 10.dp), text = "Game Settings", color = MaterialTheme.colorScheme.onBackground)
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Reset")
                }
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Settings")
                }
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column (
//                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ){
                        Text("Word packs")
                        Text("Choose word packs")
                    }
                    Column (
                        verticalArrangement = Arrangement.Center
                    ){
                        Text(">")
                    }
                }
            }
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text("Teams", color = MaterialTheme.colorScheme.onBackground)
                    Button({}) {
                        Text("Edit")
                    }
                }
                Column (
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)

                ){
                    tm.teams.forEach { t -> key(t) { TeamIcon(team = t, isRemovable = false)} }
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ){
                Text("Round length", color = MaterialTheme.colorScheme.onBackground)
                Slider(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    value = roundLengthSliderPosition,
                    onValueChange = {roundLengthSliderPosition = it},
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    steps = 6,
                    valueRange = 30f..240f)
                Text("%.0fs".format(roundLengthSliderPosition), color = MaterialTheme.colorScheme.onBackground)

                Text("Words required for winning", color = MaterialTheme.colorScheme.onBackground)
                Slider(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    value = wordsRequiredForWinningSliderPosition,
                    onValueChange = {wordsRequiredForWinningSliderPosition = it},
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    steps = 8,
                    valueRange = 25f..250f)
                Text("%.0f words".format(wordsRequiredForWinningSliderPosition), color = MaterialTheme.colorScheme.onBackground)
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ){
                Text("Rules", color = MaterialTheme.colorScheme.onBackground)
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ){
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 30.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Column(
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        ){
                            Text("Last word", color = MaterialTheme.colorScheme.onPrimary)
                            Text("Any team can guess the last word after the time runs out", color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Switch(checked = lastWordSetting, onCheckedChange = {lastWordSetting = it})
                    }
                    HorizontalDivider(thickness = 2.dp)
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 30.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Column(){
                            Text("Skipped word", color = MaterialTheme.colorScheme.onPrimary)
                            Text("-1 point for each skipped word", color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Switch(checked = skippedWordSetting, onCheckedChange = {skippedWordSetting = it})
                    }
                }
            }
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = "Start game")
            }
        }
    }
}