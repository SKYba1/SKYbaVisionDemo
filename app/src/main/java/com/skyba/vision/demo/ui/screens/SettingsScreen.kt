package com.artems_apps.vision_pause.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skyba.vision.demo.R
import com.skyba.vision.demo.data.local.SettingsItem
import com.skyba.vision.demo.ui.components.SettingsButton
import com.skyba.vision.demo.ui.components.getGroupedShape

@Composable
fun settingsScreen(
    onTimeClick: () -> Unit,
    onSoundClick: () -> Unit
) {

    val uriHandler = LocalUriHandler.current

    val settingsOptions = listOf(

        SettingsItem(
            R.string.duration_title, R.string.duration_description,
            R.drawable.sandglass_icon, R.string.cd_volume_settings_icon,
            onTimeClick
        ),
        SettingsItem(R.string.sound_title, R.string.sound_description,
            R.drawable.sound_icon, R.string.cd_volume_settings_icon,
            onSoundClick)
    )

    LazyColumn(modifier = Modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            Spacer(modifier = Modifier.height(15.dp))
            Text(
                text = stringResource(R.string.timer_settings_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
            )
            Spacer(modifier = Modifier.height(15.dp))
        }
        itemsIndexed(settingsOptions) { index, item ->

            val shape = getGroupedShape(index, settingsOptions.size)

            SettingsButton(
                item = item,
                shape = shape
            )
        }
    }
}