package com.skyba.vision.demo.data.local

import androidx.annotation.StringRes
import com.skyba.vision.demo.R


enum class PhaseSoundOption(@StringRes val labelResId: Int, val fileName: String?) {
    SOUND_1(R.string.sound_1, "sound_1"),
    SOUND_2(R.string.sound_2, "sound_2"),
    SOUND_3(R.string.sound_3, "sound_3"),
    SOUND_4(R.string.sound_4, "sound_4"),
    NONE(R.string.sound_none, null)
}