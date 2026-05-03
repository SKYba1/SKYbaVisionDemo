package com.artems_apps.vision_pause.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skyba.vision.demo.data.local.SettingsViewModel
import com.skyba.vision.demo.R
import com.skyba.vision.demo.data.local.PhaseSoundOption
import com.skyba.vision.demo.ui.components.MediaVolumeSlider
import com.skyba.vision.demo.ui.components.MyAppTopAppBar
import com.skyba.vision.demo.ui.components.SettingsRadioButton
import com.skyba.vision.demo.ui.components.getGroupedShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun soundScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel
) {
    val selectedSound by viewModel.phaseSound.collectAsState()
    val options = PhaseSoundOption.entries

    Scaffold(
        topBar = {
            MyAppTopAppBar(R.string.sound_title, onBack = { onBack()
                viewModel.stopPreviewSound()})
        }
    ) { innerPadding ->


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            item {

                Spacer(modifier = Modifier.height(15.dp))

                Row( modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start) {

                    Icon(
                        painter = painterResource(R.drawable.sound_icon),
                        contentDescription = stringResource(R.string.media_volume),
                        modifier = Modifier
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = stringResource(R.string.media_volume),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))

            }

            item {

                Spacer(modifier = Modifier.height(15.dp))

                Surface(modifier = Modifier
                    .height(72.dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MediaVolumeSlider()
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))
            }

            item {

                Spacer(modifier = Modifier.height(15.dp))

                Row( modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start) {

                    Icon(
                        painter = painterResource(R.drawable.notification_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = stringResource(R.string.sound_title_notification),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))

            }

            itemsIndexed(options) { index, option ->

                SettingsRadioButton(
                    item = option,
                    title = stringResource(option.labelResId),
                    isSelected = option == selectedSound,
                    shape = getGroupedShape(index, options.size),
                    onClick = {
                        viewModel.setPhaseSound(option)
                        viewModel.previewSound(option.fileName)
                    }
                )
            }
        }
    }
}