package io.lumstudio.yohub.runtime

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

val LocalDriver = compositionLocalOf<DriverStore> { error("Not provided.") }

class DriverStore {
    var isInstall by mutableStateOf(false)
}