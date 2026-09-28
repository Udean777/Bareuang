package com.ssajudn.bareuang.ui.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssajudn.bareuang.presentation.R

@Composable
internal fun TransactionFavoriteOptions(
    saveAsFavorite: Boolean,
    includeAmount: Boolean,
    onSaveAsFavoriteChange: (Boolean) -> Unit,
    onIncludeAmountChange: (Boolean) -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = saveAsFavorite,
                    onCheckedChange = onSaveAsFavoriteChange
                )
                Text(
                    text = stringResource(R.string.tx_favorite_save_after_success),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSaveAsFavoriteChange(!saveAsFavorite) }
                )
            }
            if (saveAsFavorite) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = includeAmount,
                        onCheckedChange = onIncludeAmountChange
                    )
                    Text(
                        text = stringResource(R.string.tx_favorite_include_amount),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onIncludeAmountChange(!includeAmount) }
                    )
                }
            }
        }
    }
}
