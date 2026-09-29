package com.ssajudn.bareuang.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.components.Material3SettingsGroup
import com.ssajudn.bareuang.ui.components.Material3SettingsItem

@Composable
internal fun SettingsBackupSection(
    onNavigateToImport: () -> Unit,
    onNavigateToOcr: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
) {
    Material3SettingsGroup(
        title = stringResource(R.string.settings_backup_title),
        items = listOf(
            Material3SettingsItem(
                title = stringResource(R.string.settings_import_mutasi_title),
                description = stringResource(R.string.settings_import_mutasi_desc),
                icon = Icons.Default.UploadFile,
                onClick = onNavigateToImport,
            ),
            Material3SettingsItem(
                title = stringResource(R.string.settings_ocr_title),
                description = stringResource(R.string.settings_ocr_desc),
                icon = Icons.Default.DocumentScanner,
                onClick = onNavigateToOcr,
            ),
            Material3SettingsItem(
                title = stringResource(R.string.settings_export_title),
                description = stringResource(R.string.settings_export_desc),
                icon = Icons.Default.FileDownload,
                onClick = onExportBackup,
            ),
            Material3SettingsItem(
                title = stringResource(R.string.settings_import_title),
                description = stringResource(R.string.settings_import_desc),
                icon = Icons.Default.FileUpload,
                onClick = onImportBackup,
            ),
        ),
    )
}
