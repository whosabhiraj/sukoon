package com.sukoon.timer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sukoon.timer.model.TimerProfile
import com.sukoon.timer.model.fullClock
import com.sukoon.timer.timer.TimerEngine
import com.sukoon.timer.ui.theme.BabyPink
import com.sukoon.timer.ui.theme.ClaudePeach
import com.sukoon.timer.ui.theme.Ink
import com.sukoon.timer.ui.theme.InkSoft
import com.sukoon.timer.ui.theme.Lavender
import com.sukoon.timer.ui.theme.LavenderDeep
import com.sukoon.timer.ui.theme.LavenderSoft
import com.sukoon.timer.ui.theme.PeachSoft

private enum class EditField { Rounds, Work, Rest, LeadIn }

private val Frost = Color.White.copy(alpha = 0.72f)

@Composable
fun EditScreen(vm: TimerEngine, existing: TimerProfile?) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var rounds by remember { mutableIntStateOf(existing?.rounds ?: 8) }
    var work by remember { mutableIntStateOf(existing?.workSeconds ?: 120) }
    var rest by remember { mutableIntStateOf(existing?.restSeconds ?: 30) }
    var leadIn by remember { mutableIntStateOf(existing?.leadInSeconds ?: 5) }
    var cues by remember { mutableStateOf(existing?.countdownCues ?: true) }
    var editing by remember { mutableStateOf<EditField?>(null) }

    fun build() = TimerProfile(
        id = existing?.id ?: vm.newId(),
        name = name.trim().ifBlank { "My routine" },
        rounds = rounds,
        workSeconds = work,
        restSeconds = rest,
        leadInSeconds = leadIn,
        countdownCues = cues,
    )

    Box(Modifier.fillMaxSize()) {
        AmbientBackground(
            top = LavenderSoft,
            bottom = PeachSoft,
            blobs = listOf(Lavender, ClaudePeach, BabyPink),
        )

        Column(
            Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 22.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundIconButton(
                    icon = Icons.Rounded.ArrowBack, contentDescription = "Back",
                    background = Frost, tint = Ink,
                    onClick = { vm.goHome() }, size = 48.dp,
                )
                Spacer(Modifier.size(14.dp))
                Text(
                    if (existing == null) "New routine" else "Edit routine",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                )
            }

            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Spacer(Modifier.height(2.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Routine name") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )

                Stepper(
                    label = "Number of rounds", valueText = "$rounds",
                    accent = Lavender, valueColor = Ink, container = Frost,
                    onMinus = { if (rounds > 1) rounds-- }, onPlus = { if (rounds < 30) rounds++ },
                    onValueClick = { editing = EditField.Rounds },
                )
                Stepper(
                    label = "Work — each round", valueText = fullClock(work),
                    accent = Lavender, valueColor = Ink, container = Frost,
                    onMinus = { if (work > 10) work -= 10 }, onPlus = { if (work < 900) work += 10 },
                    onValueClick = { editing = EditField.Work },
                )
                Stepper(
                    label = "Rest — between rounds",
                    valueText = if (rest == 0) "Off" else fullClock(rest),
                    accent = Lavender, valueColor = Ink, container = Frost,
                    onMinus = { if (rest > 0) rest -= 5 }, onPlus = { if (rest < 600) rest += 5 },
                    onValueClick = { editing = EditField.Rest },
                )
                Stepper(
                    label = "Get ready — before start",
                    valueText = if (leadIn == 0) "Off" else "${leadIn}s",
                    accent = Lavender, valueColor = Ink, container = Frost,
                    onMinus = { if (leadIn > 0) leadIn -= 5 }, onPlus = { if (leadIn < 30) leadIn += 5 },
                    onValueClick = { editing = EditField.LeadIn },
                )

                Surface(
                    Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = Frost,
                ) {
                    Row(
                        Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "3, 2, 1 countdown beeps",
                                style = MaterialTheme.typography.titleMedium, color = Ink,
                            )
                            Text(
                                "Play a soft beep in the last 3 seconds of each timer",
                                style = MaterialTheme.typography.bodyMedium, color = InkSoft,
                            )
                        }
                        Switch(
                            checked = cues,
                            onCheckedChange = { cues = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Lavender,
                                uncheckedTrackColor = LavenderSoft,
                            ),
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
            }

            Column(
                Modifier.padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SoftButton(
                    text = "Start now", background = Lavender, textColor = Color.White,
                    leading = Icons.Rounded.PlayArrow, onClick = { vm.start(build()) },
                )
                SoftButton(
                    text = "Save routine", background = Frost, textColor = Ink,
                    onClick = { vm.saveProfile(build()) },
                )
            }
        }

        when (editing) {
            EditField.Rounds -> NumberInputDialog(
                "Number of rounds", "rounds", rounds, 1, 99,
                onDismiss = { editing = null },
            ) { rounds = it; editing = null }
            EditField.Work -> NumberInputDialog(
                "Work — each round", "seconds", work, 1, 3600,
                onDismiss = { editing = null },
            ) { work = it; editing = null }
            EditField.Rest -> NumberInputDialog(
                "Rest — between rounds", "seconds", rest, 0, 3600,
                onDismiss = { editing = null },
            ) { rest = it; editing = null }
            EditField.LeadIn -> NumberInputDialog(
                "Get ready", "seconds", leadIn, 0, 600,
                onDismiss = { editing = null },
            ) { leadIn = it; editing = null }
            null -> Unit
        }
    }
}

@Composable
private fun fieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White.copy(alpha = 0.85f),
    focusedIndicatorColor = Lavender,
    unfocusedIndicatorColor = Color.Transparent,
    focusedLabelColor = LavenderDeep,
    cursorColor = Lavender,
)

@Composable
private fun NumberInputDialog(
    title: String,
    unit: String,
    current: Int,
    min: Int,
    max: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var text by remember { mutableStateOf(current.toString()) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    fun commit() = onConfirm((text.toIntOrNull() ?: current).coerceIn(min, max))

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(title, style = MaterialTheme.typography.titleLarge, color = Ink) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { s -> text = s.filter { it.isDigit() }.take(5) },
                singleLine = true,
                suffix = { Text(unit, color = InkSoft) },
                textStyle = MaterialTheme.typography.headlineMedium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
                colors = fieldColors(),
            )
        },
        confirmButton = {
            TextButton(onClick = { commit() }) {
                Text("Done", color = LavenderDeep, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = InkSoft, style = MaterialTheme.typography.labelLarge)
            }
        },
    )
}
