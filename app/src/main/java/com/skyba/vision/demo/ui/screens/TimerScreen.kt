package com.artems_apps.vision_pause.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skyba.vision.demo.R
import com.skyba.vision.demo.data.local.SettingsViewModel
import com.skyba.vision.demo.data.local.services.TimerService


@Composable
fun timerScreen(viewModel: SettingsViewModel) {
    val context = LocalContext.current

    val timerValue by TimerService.timerLiveData.observeAsState("00:00")
    val phase by TimerService.phaseLiveData.observeAsState(TimerService.TimerPhase.IDLE)


    LaunchedEffect(phase) {
        viewModel.currentPhase.value = phase
    }



    Column(modifier = Modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            shape = RoundedCornerShape(
                topStart = 15.dp, topEnd = 15.dp,
                bottomStart = 5.dp, bottomEnd = 5.dp
            ),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {

            Column(modifier = Modifier
                .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = timerValue,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 40.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))
            }

        }

        Spacer(modifier = Modifier.height(5.dp))


        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 15.dp),
            shape = RoundedCornerShape(
                topStart = 5.dp, topEnd = 5.dp,
                bottomStart = 15.dp, bottomEnd = 15.dp
            ),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {

            Column(modifier = Modifier
                .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {

                Spacer(modifier = Modifier.weight(1f))

                when (phase) {
                    TimerService.TimerPhase.IDLE -> {

                        PrimaryLayout (viewModel, phase)
                    }

                    else -> {
                        TriangleLayout(viewModel, phase)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}


fun getIconResId(phase: TimerService.TimerPhase) : Int {
    return when (phase) {
        TimerService.TimerPhase.IDLE -> R.drawable.play_button_icon
        TimerService.TimerPhase.PAUSE -> R.drawable.play_button_icon
        else -> R.drawable.pause_button_icon
    }
}

@Composable
fun PrimaryLayout (viewModel: SettingsViewModel, phase: TimerService.TimerPhase) {

    val context = LocalContext.current

    Button(
        onClick = {
            when (phase) {
                TimerService.TimerPhase.IDLE -> { context.startService(viewModel.buildStartIntent(context)) }

                TimerService.TimerPhase.PAUSE -> {
                    context.startService(
                        Intent(context, TimerService::class.java).apply {
                            action = TimerService.TimerAction.RESUME.name
                        }
                    )
                }
                else -> {
                    context.startService(
                        Intent(context, TimerService::class.java).apply {
                            action = TimerService.TimerAction.PAUSE.name
                        }
                    )
                }
            }
        },
        modifier = Modifier
            .width(200.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(50.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.start_button),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun TriangleLayout(viewModel: SettingsViewModel, phase: TimerService.TimerPhase) {

    val context = LocalContext.current

        Button(
            onClick = {
                context.startService(
                    Intent(context, TimerService::class.java).apply {
                        action = TimerService.TimerAction.SKIP.name
                    }
                )
            },
            modifier = Modifier
                .width(100.dp)
                .height(48.dp)
                .clip(shape = RoundedCornerShape(50.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {

            Icon(
                painter = painterResource(R.drawable.skip_button_icon),
                contentDescription = stringResource(R.string.skip_button),
                modifier = Modifier
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(50.dp))

    Button(
        onClick = {
            when (phase) {
                TimerService.TimerPhase.IDLE -> { context.startService(viewModel.buildStartIntent(context)) }

                TimerService.TimerPhase.PAUSE -> {
                    context.startService(
                        Intent(context, TimerService::class.java).apply {
                            action = TimerService.TimerAction.RESUME.name
                        }
                    )
                }
                else -> {
                    context.startService(
                        Intent(context, TimerService::class.java).apply {
                            action = TimerService.TimerAction.PAUSE.name
                        }
                    )
                }
            }
        },
        modifier = Modifier
            .width(72.dp)
            .height(72.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            painter = painterResource(id = getIconResId(phase)),
            contentDescription = stringResource(R.string.continue_button),
            modifier = Modifier
                .size(30.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }

    Spacer(modifier = Modifier.height(50.dp))

    Row(){
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                context.startService(viewModel.buildRestartIntent(context))
            },
            modifier = Modifier
                .width(100.dp)
                .height(48.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.restart_button_icon),
                contentDescription = stringResource(R.string.restart_button),
                modifier = Modifier
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.weight(1f))


        Button(
            onClick = {
                Intent(context, TimerService::class.java).also {
                    it.action = TimerService.TimerAction.STOP.name
                    context.startService(it)
                }
            },
            modifier = Modifier
                .width(100.dp)
                .height(48.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {

            Icon(
                painter = painterResource(R.drawable.stop_button_icon),
                contentDescription = stringResource(R.string.cd_stop_button),
                modifier = Modifier
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.surface
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}




