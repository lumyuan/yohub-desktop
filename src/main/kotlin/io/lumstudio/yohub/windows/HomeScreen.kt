package io.lumstudio.yohub.windows

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class HomeTooltip(
    val title: String,
    val subtitle: String? = null,
)

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column {
            val tooltips = arrayOf(
                HomeTooltip("镜像文件提取", "点击右侧【Payload文件提取】")
            )
            tooltips.onEach {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(it.title)
                    it.subtitle?.apply {
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(this, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f))
                    }
                }
            }
        }
    }
}