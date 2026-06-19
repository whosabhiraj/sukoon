package com.sukoon.timer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sukoon.timer.model.TimerProfile
import com.sukoon.timer.timer.TimerEngine
import com.sukoon.timer.ui.theme.BabyPink
import com.sukoon.timer.ui.theme.ClaudePeach
import com.sukoon.timer.ui.theme.Ink
import com.sukoon.timer.ui.theme.InkSoft
import com.sukoon.timer.ui.theme.Lavender
import com.sukoon.timer.ui.theme.LavenderSoft
import com.sukoon.timer.ui.theme.PinkSoft
import com.sukoon.timer.ui.theme.Sage

@Composable
fun HomeScreen(vm: TimerEngine) {
    Box(Modifier.fillMaxSize()) {
        AmbientBackground(
            top = LavenderSoft,
            bottom = PinkSoft,
            blobs = listOf(Lavender, BabyPink, Sage),
        )

        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 22.dp),
        ) {
            Spacer(Modifier.height(20.dp))
            Text(
                "Sukoon",
                style = MaterialTheme.typography.displayLarge,
                color = Ink,
            )
            Text(
                "a calm moment, kept in time",
                style = MaterialTheme.typography.bodyLarge,
                color = InkSoft,
            )
            Spacer(Modifier.height(22.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(vm.profiles, key = { it.id }) { profile ->
                    ProfileCard(
                        profile = profile,
                        onStart = { vm.start(profile) },
                        onEdit = { vm.editProfile(profile) },
                        onDelete = { vm.deleteProfile(profile) },
                    )
                }
            }

            SoftButton(
                text = "New routine",
                background = Lavender,
                textColor = Color.White,
                leading = Icons.Rounded.Add,
                onClick = { vm.newProfile() },
                modifier = Modifier.padding(vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun ProfileCard(
    profile: TimerProfile,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onStart),
        shape = MaterialTheme.shapes.large,
        color = Color.White.copy(alpha = 0.82f),
    ) {
        Row(
            Modifier.padding(start = 18.dp, end = 10.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // a soft "play" pebble that hints the card is tappable to begin
            Box(
                Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .then(
                        Modifier.background(
                            Brush.linearGradient(listOf(Lavender, ClaudePeach)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = "Start", tint = Color.White)
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    profile.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    profile.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft,
                )
            }
            RoundIconButton(
                icon = Icons.Rounded.Edit,
                contentDescription = "Edit ${profile.name}",
                background = Color.Transparent,
                tint = InkSoft,
                onClick = onEdit,
                size = 44.dp,
            )
            RoundIconButton(
                icon = Icons.Rounded.DeleteOutline,
                contentDescription = "Delete ${profile.name}",
                background = Color.Transparent,
                tint = InkSoft,
                onClick = onDelete,
                size = 44.dp,
            )
        }
    }
}
