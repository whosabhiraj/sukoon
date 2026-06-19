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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sukoon.timer.model.TimerProfile
import com.sukoon.timer.model.fullClock
import com.sukoon.timer.timer.TimerEngine
import com.sukoon.timer.ui.theme.BabyPink
import com.sukoon.timer.ui.theme.ClaudePeach
import com.sukoon.timer.ui.theme.Ink
import com.sukoon.timer.ui.theme.InkSoft
import com.sukoon.timer.ui.theme.Lavender
import com.sukoon.timer.ui.theme.LavenderSoft
import com.sukoon.timer.ui.theme.PeachSoft
import com.sukoon.timer.ui.theme.Sage

@Composable
fun EditScreen(vm: TimerEngine, existing: TimerProfile?) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var rounds by remember { mutableIntStateOf(existing?.rounds ?: 8) }
    var work by remember { mutableIntStateOf(existing?.workSeconds ?: 120) }
    var rest by remember { mutableIntStateOf(existing?.restSeconds ?: 30) }
    var leadIn by remember { mutableIntStateOf(existing?.leadInSeconds ?: 5) }
    var cues by remember { mutableStateOf(existing?.countdownCues ?: true) }

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
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundIconButton(
                    icon = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    background = Color.White.copy(alpha = 0.7f),
                    tint = Ink,
                    onClick = { vm.goHome() },
                    size = 46.dp,
                )
                Spacer(Modifier.size(14.dp))
                Text(
                    if (existing == null) "New routine" else "Edit routine",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                )
            }

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Spacer(Modifier.height(2.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Routine name") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White.copy(alpha = 0.85f),
                        focusedIndicatorColor = Lavender,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = Lavender,
                        cursorColor = Lavender,
                    ),
                )

                Stepper(
                    label = "Number of rounds",
                    valueText = "$rounds",
                    accent = Sage,
                    onMinus = { if (rounds > 1) rounds-- },
                    onPlus = { if (rounds < 30) rounds++ },
                )
                Stepper(
                    label = "Work — each round",
                    valueText = fullClock(work),
                    accent = Sage,
                    onMinus = { if (work > 10) work -= 10 },
                    onPlus = { if (work < 900) work += 10 },
                )
                Stepper(
                    label = "Rest — between rounds",
                    valueText = if (rest == 0) "Off" else fullClock(rest),
                    accent = Lavender,
                    onMinus = { if (rest > 0) rest -= 5 },
                    onPlus = { if (rest < 600) rest += 5 },
                )
                Stepper(
                    label = "Get ready — before start",
                    valueText = if (leadIn == 0) "Off" else "${leadIn}s",
                    accent = ClaudePeach,
                    onMinus = { if (leadIn > 0) leadIn -= 5 },
                    onPlus = { if (leadIn < 30) leadIn += 5 },
                )

                // Countdown pips toggle
                Surface(
                    Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = Color.White.copy(alpha = 0.92f),
                ) {
                    Row(
                        Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Countdown pips",
                                style = MaterialTheme.typography.titleMedium,
                                color = Ink,
                            )
                            Text(
                                "Soft ticks for the last 3 seconds",
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkSoft,
                            )
                        }
                        Switch(
                            checked = cues,
                            onCheckedChange = { cues = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Sage,
                                uncheckedTrackColor = LavenderSoft,
                            ),
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
            }

            // Actions
            Column(
                Modifier.padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SoftButton(
                    text = "Start now",
                    background = Sage,
                    textColor = Color.White,
                    leading = Icons.Rounded.PlayArrow,
                    onClick = { vm.start(build()) },
                )
                SoftButton(
                    text = "Save routine",
                    background = Lavender,
                    textColor = Color.White,
                    onClick = { vm.saveProfile(build()) },
                )
            }
        }
    }
}
