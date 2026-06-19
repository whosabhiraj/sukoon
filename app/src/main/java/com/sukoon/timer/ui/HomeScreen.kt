package com.sukoon.timer.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukoon.timer.model.TimerProfile
import com.sukoon.timer.timer.TimerEngine
import com.sukoon.timer.ui.theme.BabyPink
import com.sukoon.timer.ui.theme.ClaudePeach
import com.sukoon.timer.ui.theme.Ink
import com.sukoon.timer.ui.theme.InkSoft
import com.sukoon.timer.ui.theme.Lavender
import com.sukoon.timer.ui.theme.LavenderDeep
import com.sukoon.timer.ui.theme.LavenderSoft
import com.sukoon.timer.ui.theme.PeachDeep
import com.sukoon.timer.ui.theme.PinkSoft
import com.sukoon.timer.ui.theme.Sage
import com.sukoon.timer.ui.theme.Sunflower
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(vm: TimerEngine) {
    Box(Modifier.fillMaxSize()) {
        AmbientBackground(
            top = LavenderSoft,
            bottom = PinkSoft,
            blobs = listOf(Lavender, BabyPink, Sage),
        )

        Column(
            Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(28.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "Sukoon",
                    style = MaterialTheme.typography.displayLarge.copy(
                        brush = Brush.linearGradient(listOf(LavenderDeep, PeachDeep)),
                    ),
                )
                Spacer(Modifier.size(10.dp))
                Text(
                    "✿",
                    fontSize = 32.sp,
                    color = Sunflower,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "This too shall pass.",
                style = MaterialTheme.typography.bodyLarge,
                color = Ink,
            )
            Spacer(Modifier.height(26.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                itemsIndexed(vm.profiles, key = { _, p -> p.id }) { index, profile ->
                    Entrance(index) {
                        ProfileCard(
                            profile = profile,
                            onStart = { vm.start(profile) },
                            onEdit = { vm.editProfile(profile) },
                            onDelete = { vm.deleteProfile(profile) },
                        )
                    }
                }
            }

            SoftButton(
                text = "New routine",
                background = Lavender,
                textColor = Color.White,
                leading = Icons.Rounded.Add,
                onClick = { vm.newProfile() },
                modifier = Modifier.padding(vertical = 14.dp),
            )
        }
    }
}

/** Staggered slide-up + fade-in for list items. */
@Composable
private fun Entrance(index: Int, content: @Composable () -> Unit) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 70L)
        anim.animateTo(1f, tween(440, easing = FastOutSlowInEasing))
    }
    Box(
        Modifier.graphicsLayer {
            alpha = anim.value
            translationY = (1f - anim.value) * 44f
        },
    ) { content() }
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
        color = Color.White.copy(alpha = 0.72f),
    ) {
        Row(
            Modifier.padding(start = 18.dp, end = 8.dp, top = 18.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Lavender, ClaudePeach))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = "Start", tint = Color.White)
            }
            Spacer(Modifier.size(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    profile.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    profile.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft,
                )
            }
            RoundIconButton(
                icon = Icons.Rounded.Edit, contentDescription = "Edit ${profile.name}",
                background = Color.Transparent, tint = InkSoft, onClick = onEdit, size = 44.dp,
            )
            RoundIconButton(
                icon = Icons.Rounded.DeleteOutline, contentDescription = "Delete ${profile.name}",
                background = Color.Transparent, tint = InkSoft, onClick = onDelete, size = 44.dp,
            )
        }
    }
}
