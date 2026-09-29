package com.ssajudn.bareuang.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.ssajudn.bareuang.domain.model.BackupRecordPreview
import com.ssajudn.bareuang.domain.model.BackupRestorePreview
import com.ssajudn.bareuang.domain.model.BackupRestoreSummary
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.ui.common.asString
import com.ssajudn.bareuang.ui.components.AppTextButton

@Composable
internal fun ExportPasswordDialog(isLoading: Boolean, onDismiss: () -> Unit, onContinue: (CharArray) -> Unit) {
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmationVisible by remember { mutableStateOf(false) }
    val canExport = password.length >= 12 && password == confirmation
    val dismiss = {
        password = ""
        confirmation = ""
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) dismiss() },
        title = { Text(stringResource(R.string.settings_backup_password_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.settings_backup_password_desc))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.settings_backup_password)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    isError = password.isNotEmpty() && password.length < 12,
                    trailingIcon = { PasswordVisibilityToggle(passwordVisible) { passwordVisible = !passwordVisible } },
                )
                OutlinedTextField(
                    value = confirmation,
                    onValueChange = { confirmation = it },
                    label = { Text(stringResource(R.string.settings_backup_password_confirm)) },
                    visualTransformation = if (confirmationVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    isError = confirmation.isNotEmpty() && password != confirmation,
                    trailingIcon = { PasswordVisibilityToggle(confirmationVisible) { confirmationVisible = !confirmationVisible } },
                )
            }
        },
        confirmButton = {
            AppTextButton(
                enabled = canExport && !isLoading,
                onClick = {
                    val secret = password.toCharArray()
                    password = ""
                    confirmation = ""
                    onContinue(secret)
                },
            ) { Text(stringResource(R.string.settings_backup_export_continue)) }
        },
        dismissButton = {
            AppTextButton(enabled = !isLoading, onClick = dismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

@Composable
internal fun ImportPasswordDialog(
    isLoading: Boolean,
    error: UiText?,
    onDismiss: () -> Unit,
    onPreview: (CharArray?) -> Unit,
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val dismiss = {
        password = ""
        onDismiss()
    }
    AlertDialog(
        onDismissRequest = { if (!isLoading) dismiss() },
        title = { Text(stringResource(R.string.settings_restore_password_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.settings_restore_password_desc))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.settings_backup_password)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    supportingText = { Text(stringResource(R.string.settings_restore_legacy_hint)) },
                    isError = error != null,
                    trailingIcon = { PasswordVisibilityToggle(passwordVisible) { passwordVisible = !passwordVisible } },
                )
                error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            AppTextButton(
                enabled = !isLoading,
                onClick = {
                    val secret = password.takeIf(String::isNotEmpty)?.toCharArray()
                    password = ""
                    onPreview(secret)
                },
            ) {
                Text(
                    stringResource(
                        if (isLoading) R.string.common_loading else R.string.settings_restore_preview_action,
                    ),
                )
            }
        },
        dismissButton = {
            AppTextButton(enabled = !isLoading, onClick = dismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

@Composable
internal fun RestorePreviewDialog(
    preview: BackupRestorePreview,
    error: UiText?,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val records = listOf(
        stringResource(R.string.settings_restore_wallets) to preview.wallets,
        stringResource(R.string.settings_restore_transactions) to preview.transactions,
        stringResource(R.string.settings_restore_bills) to preview.dueBills,
        stringResource(R.string.settings_restore_budgets) to preview.budgets,
        stringResource(R.string.settings_restore_category_budgets) to preview.categoryBudgets,
        stringResource(R.string.settings_restore_goals) to preview.goals,
    )
    val total = records.sumOf { it.second.total }
    val new = records.sumOf { it.second.new }
    val replaced = records.sumOf { it.second.replaced }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(stringResource(R.string.settings_restore_preview_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BackupPreviewHeader(preview)
                BackupPreviewTotals(total, new, replaced)
                Text(stringResource(R.string.settings_restore_records_heading), style = MaterialTheme.typography.titleSmall)
                records.forEach { (label, counts) -> BackupRecordSummaryLine(label, counts) }
                BackupPreviewNotice(
                    stringResource(R.string.settings_restore_merge_warning),
                    Icons.Filled.Info,
                )
                if (!preview.isEncrypted) {
                    BackupPreviewNotice(
                        stringResource(R.string.settings_restore_unencrypted_warning),
                        Icons.Filled.Warning,
                        isWarning = true,
                    )
                }
                if (preview.hasReceiptReferences) {
                    BackupPreviewNotice(
                        stringResource(R.string.settings_restore_receipt_warning),
                        Icons.Filled.Image,
                    )
                }
                error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            AppTextButton(enabled = !isLoading, onClick = onConfirm) {
                Text(stringResource(if (isLoading) R.string.common_loading else R.string.settings_restore_confirm))
            }
        },
        dismissButton = {
            AppTextButton(enabled = !isLoading, onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

@Composable
internal fun RestoreSummaryDialog(summary: BackupRestoreSummary, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_restore_complete_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.settings_restore_added, summary.added))
                Text(stringResource(R.string.settings_restore_replaced, summary.replaced))
                summary.currencyCode?.let { Text(stringResource(R.string.settings_restore_currency, it)) }
                if (!summary.currencyApplied) {
                    Text(
                        stringResource(R.string.settings_restore_currency_warning),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            AppTextButton(onClick = onDismiss) { Text(stringResource(R.string.common_done)) }
        },
    )
}

@Composable
private fun BackupRecordSummaryLine(label: String, counts: BackupRecordPreview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, style = MaterialTheme.typography.titleSmall)
                Text(counts.total.toString(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.settings_restore_new_count, counts.new), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.settings_restore_replaced_count, counts.replaced), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun BackupPreviewHeader(preview: BackupRestorePreview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                    Icon(Icons.Filled.FileDownload, contentDescription = null, modifier = Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.settings_restore_backup_label), style = MaterialTheme.typography.titleSmall)
                    Text(
                        stringResource(R.string.settings_restore_backup_info, preview.backupDate, preview.appVersion),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                PreviewBadge(
                    text = stringResource(if (preview.isEncrypted) R.string.settings_restore_encrypted else R.string.settings_restore_unencrypted),
                    icon = if (preview.isEncrypted) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    container = if (preview.isEncrypted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
                )
                preview.currencyCode?.let {
                    PreviewBadge(stringResource(R.string.settings_restore_currency, it), Icons.Filled.Paid, MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
                }
            }
        }
    }
}

@Composable
private fun BackupPreviewTotals(total: Int, new: Int, replaced: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PreviewTotalCard(stringResource(R.string.settings_restore_total), total, MaterialTheme.colorScheme.surfaceContainerLow, Modifier.weight(1f))
        PreviewTotalCard(stringResource(R.string.settings_restore_new), new, MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f))
        PreviewTotalCard(stringResource(R.string.settings_restore_replaced_short), replaced, MaterialTheme.colorScheme.tertiaryContainer, Modifier.weight(1f))
    }
}

@Composable
private fun PreviewTotalCard(label: String, value: Int, container: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = container) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value.toString(), style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PreviewBadge(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, container: Color) {
    Surface(shape = RoundedCornerShape(50), color = container) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun BackupPreviewNotice(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isWarning: Boolean = false,
) {
    val container = if (isWarning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val content = if (isWarning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(shape = RoundedCornerShape(12.dp), color = container) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = content)
            Text(text, style = MaterialTheme.typography.bodySmall, color = content)
        }
    }
}

@Composable
private fun PasswordVisibilityToggle(visible: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
            contentDescription = stringResource(if (visible) R.string.settings_password_hide else R.string.settings_password_show),
        )
    }
}
