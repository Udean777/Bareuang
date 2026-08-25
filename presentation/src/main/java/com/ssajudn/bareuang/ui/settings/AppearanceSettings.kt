package com.ssajudn.bareuang.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.theme.Spacing
import com.ssajudn.bareuang.domain.model.AppThemeDarkMode

@Composable
fun AppearanceSettingsGroup(
    darkMode: AppThemeDarkMode,
    onDarkModeChange: (AppThemeDarkMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.appearance_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(
                start = Spacing.ScreenHorizontal,
                bottom = Spacing.Small,
            ),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            Column(
                modifier = Modifier.padding(vertical = Spacing.Small, horizontal = Spacing.Medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.Small),
            ) {
                Text(
                    text = stringResource(R.string.appearance_dark_mode),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    DarkModeOptions.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = darkMode == option.mode,
                            onClick = { onDarkModeChange(option.mode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = DarkModeOptions.size,
                            ),
                            label = {
                                Text(
                                    text = option.label,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

private data class DarkModeOption(val mode: AppThemeDarkMode, val label: String)

private val DarkModeOptions = listOf(
    DarkModeOption(AppThemeDarkMode.FollowSystem, "Sistem"),
    DarkModeOption(AppThemeDarkMode.Light, "Terang"),
    DarkModeOption(AppThemeDarkMode.Dark, "Gelap"),
)
