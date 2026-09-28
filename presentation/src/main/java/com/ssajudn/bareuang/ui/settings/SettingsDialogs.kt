package com.ssajudn.bareuang.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssajudn.bareuang.domain.model.AppCurrency
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.components.AppTextButton
import com.ssajudn.bareuang.utils.LanguageManager

@Composable
internal fun ReminderTimeDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
) {
    val timeState = rememberTimePickerState(initialHour, initialMinute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_bill_reminder_time)) },
        text = { TimePicker(state = timeState) },
        confirmButton = {
            AppTextButton(onClick = { onConfirm(timeState.hour, timeState.minute) }) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            AppTextButton(onClick = onDismiss) { Text(stringResource(R.string.common_close)) }
        },
    )
}

@Composable
internal fun LanguagePickerDialog(currentLanguage: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language_settings)) },
        text = {
            Column {
                LanguageManager.SUPPORTED_LANGUAGES.forEach { (code, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = currentLanguage == code, onClick = { onSelect(code) })
                        Spacer(Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            AppTextButton(onClick = onDismiss) { Text(stringResource(R.string.common_close)) }
        },
    )
}

@Composable
internal fun CurrencyPickerDialog(current: AppCurrency, onSelect: (AppCurrency) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.currency_settings)) },
        text = {
            Column {
                AppCurrency.entries.forEach { currency ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = current == currency, onClick = { onSelect(currency) })
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                if (currency == AppCurrency.IDR) R.string.currency_idr else R.string.currency_usd,
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        },
        confirmButton = {
            AppTextButton(onClick = onDismiss) { Text(stringResource(R.string.common_close)) }
        },
    )
}
