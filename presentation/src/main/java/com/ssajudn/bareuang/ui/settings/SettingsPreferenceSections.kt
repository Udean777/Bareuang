package com.ssajudn.bareuang.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tour
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssajudn.bareuang.domain.model.AppCurrency
import com.ssajudn.bareuang.presentation.BuildConfig
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.components.Material3SettingsGroup
import com.ssajudn.bareuang.ui.components.Material3SettingsItem

@Composable
internal fun SettingsWidgetSection(hidden: Boolean, onHiddenChange: (Boolean) -> Unit) {
    Material3SettingsGroup(
        title = stringResource(R.string.settings_widget_title),
        items = listOf(
            Material3SettingsItem(
                title = stringResource(R.string.settings_widget_hide_balance),
                description = stringResource(R.string.settings_widget_hide_balance_desc),
                icon = Icons.Default.VisibilityOff,
                onClick = { onHiddenChange(!hidden) },
                trailingContent = {
                    Switch(checked = hidden, onCheckedChange = onHiddenChange)
                },
            ),
        ),
    )
}

@Composable
internal fun SettingsReminderSection(hour: Int, minute: Int, onOpenTimePicker: () -> Unit) {
    Material3SettingsGroup(
        title = stringResource(R.string.settings_bill_reminder_title),
        items = listOf(
            Material3SettingsItem(
                title = stringResource(R.string.settings_bill_reminder_time),
                description = stringResource(R.string.settings_bill_reminder_time_desc),
                value = "%02d:%02d".format(java.util.Locale.US, hour, minute),
                icon = Icons.Default.NotificationsActive,
                onClick = onOpenTimePicker,
            ),
        ),
    )
}

@Composable
internal fun SettingsLanguageSection(languageCode: String, onOpenLanguagePicker: () -> Unit) {
    Material3SettingsGroup(
        title = stringResource(R.string.language_settings),
        items = listOf(
            Material3SettingsItem(
                title = stringResource(R.string.language_settings),
                description = if (languageCode == "id") {
                    stringResource(R.string.language_indonesian)
                } else {
                    stringResource(R.string.language_english)
                },
                icon = Icons.Default.Language,
                onClick = onOpenLanguagePicker,
            ),
        ),
    )
}

@Composable
internal fun SettingsCurrencySection(currency: AppCurrency, onOpenCurrencyPicker: () -> Unit) {
    Material3SettingsGroup(
        title = stringResource(R.string.currency_settings),
        items = listOf(
            Material3SettingsItem(
                title = stringResource(R.string.currency_settings),
                description = currency.labelRes().let { stringResource(it) },
                icon = Icons.Default.Paid,
                onClick = onOpenCurrencyPicker,
            ),
        ),
    )
}

@Composable
internal fun SettingsSupportSection(
    onReplayTour: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    onDonate: () -> Unit,
    onRate: () -> Unit,
    onShare: () -> Unit,
) {
    Material3SettingsGroup(
        title = stringResource(R.string.settings_support_title),
        items = listOf(
            Material3SettingsItem(
                title = stringResource(R.string.settings_replay_tour_title),
                description = stringResource(R.string.settings_replay_tour_desc),
                icon = Icons.Default.Tour,
                onClick = onReplayTour,
            ),
            Material3SettingsItem(
                title = stringResource(R.string.settings_privacy_title),
                description = stringResource(R.string.settings_privacy_desc),
                icon = Icons.Default.Policy,
                onClick = onOpenPrivacy,
            ),
            Material3SettingsItem(
                title = stringResource(R.string.settings_terms_title),
                description = stringResource(R.string.settings_terms_desc),
                icon = Icons.Default.Policy,
                onClick = onOpenTerms,
            ),
            Material3SettingsItem(
                title = stringResource(R.string.settings_donate_title),
                description = stringResource(R.string.settings_donate_desc),
                icon = Icons.Default.VolunteerActivism,
                onClick = onDonate,
            ),
            Material3SettingsItem(
                title = stringResource(R.string.settings_star_title),
                description = stringResource(R.string.settings_star_desc),
                icon = Icons.Default.Star,
                onClick = onRate,
            ),
            Material3SettingsItem(
                title = stringResource(R.string.settings_share_title),
                description = stringResource(R.string.settings_share_desc),
                icon = Icons.Default.Share,
                onClick = onShare,
            ),
        ),
    )
}

@Composable
internal fun SettingsDangerSection(onReset: () -> Unit) {
    Material3SettingsGroup(
        title = stringResource(R.string.settings_danger_title),
        items = listOf(
            Material3SettingsItem(
                title = stringResource(R.string.settings_reset_local),
                description = stringResource(R.string.settings_danger_desc),
                icon = Icons.AutoMirrored.Filled.Logout,
                isDestructive = true,
                onClick = onReset,
            ),
        ),
    )
}

@Composable
internal fun SettingsFooter() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Bareuang v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.settings_footer_tagline),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun AppCurrency.labelRes(): Int = when (this) {
    AppCurrency.IDR -> R.string.currency_idr
    AppCurrency.USD -> R.string.currency_usd
}
