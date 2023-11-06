package io.lumstudio.yohub.windows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(homePage: HomePage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            PageNav.values().toList().filter { it.page.title != null && it.page.isNavigation }.onEach {
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        .clickable {
                            homePage.karavel?.navigate(it.page)
                            selectPage.value = it.page
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${it.page.title}")
                        it.page.subtitle?.apply {
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(this, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f))
                        }
                    }
                }
            }
        }
    }
}