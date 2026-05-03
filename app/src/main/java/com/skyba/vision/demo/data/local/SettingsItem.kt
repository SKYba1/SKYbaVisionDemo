package com.skyba.vision.demo.data.local

data class SettingsItem(
    val titleRes: Int,
    val desRes: Int,
    val iconRes: Int,
    val contentDescriptionRes: Int,
    val onClick: () -> Unit
)