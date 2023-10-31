package io.lumstudio.yohub.windows

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.lumstudio.yohub.common.utils.LocalPreferences

@Composable
fun SettingsScreen() {
    val preferencesStore = LocalPreferences.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text("≈‰÷√–≈œ¢")
            Text(preferencesStore.preference.toMap().toString())
        }
    }
}