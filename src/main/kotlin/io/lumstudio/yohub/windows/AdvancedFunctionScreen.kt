package io.lumstudio.yohub.windows

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.component.ScrollbarContainer
import com.konyaco.fluent.component.rememberScrollbarAdapter
import io.lumstudio.yohub.ui.component.AdbFlowFeature
import io.lumstudio.yohub.windows.navigation.AdvancedFunctionPage

@Composable
fun AdvancedFunctionScreen(advancedFunctionPage: AdvancedFunctionPage) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val scrollState = rememberScrollState()
        ScrollbarContainer(
            adapter = rememberScrollbarAdapter(scrollState),
        ) {
            Column(
                modifier = Modifier.fillMaxHeight().verticalScroll(scrollState)
                    .padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
            ) {
                AdbFlowFeature(advancedFunctionPage)
            }
        }
    }
}