package com.ssajudn.bareuang.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.net.toUri
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.common.UiEffect
import com.ssajudn.bareuang.ui.common.asString
import com.ssajudn.bareuang.ui.components.AppConfirmDialog
import com.ssajudn.bareuang.ui.components.FeatureTopAppBar
import com.ssajudn.bareuang.utils.LanguageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onReplayTour: () -> Unit = {},
    onNavigateToImport: () -> Unit = {},
    onNavigateToOcr: () -> Unit = {},
    onLocalDataReset: () -> Unit,
) {
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val backupViewModel: SettingsBackupViewModel = hiltViewModel()
    val backupState by backupViewModel.uiState.collectAsStateWithLifecycle()
    val operation by backupViewModel.operation.collectAsStateWithLifecycle()
    val isLoading = operation is com.ssajudn.bareuang.ui.common.OperationState.Loading
    val darkMode by settingsViewModel.darkMode.collectAsStateWithLifecycle()
    val widgetHideBalance by settingsViewModel.widgetHideBalance.collectAsStateWithLifecycle()
    val currency by settingsViewModel.currency.collectAsStateWithLifecycle()

    var languageCode by remember(context) {
        mutableStateOf(LanguageManager.getCurrentLanguageCode(context))
    }
    var reminderHour by remember(settingsViewModel) { mutableStateOf(settingsViewModel.reminderHour) }
    var reminderMinute by remember(settingsViewModel) { mutableStateOf(settingsViewModel.reminderMinute) }
    var showReminderTimeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showExportPasswordDialog by remember { mutableStateOf(false) }
    var showImportPasswordDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var pendingExportPassword by remember { mutableStateOf<CharArray?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(Unit) {
        onDispose {
            pendingExportPassword?.fill('\u0000')
        }
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        val password = pendingExportPassword
        pendingExportPassword = null
        if (uri != null && password != null) {
            backupViewModel.exportBackup(uri, password)
        } else {
            password?.fill('\u0000')
        }
    }
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            backupViewModel.clearRestoreError()
            showImportPasswordDialog = true
        }
    }

    LaunchedEffect(Unit) {
        backupViewModel.effect.collect { effect ->
            when (effect) {
                is UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is UiEffect.ShowSnackbarRes -> snackbarHostState.showSnackbar(effect.uiText.asString(context))
                is UiEffect.Navigate -> Unit
                is UiEffect.PopBackStack -> onLocalDataReset()
            }
        }
    }
    LaunchedEffect(backupState.isLocalDataReset) {
        if (backupState.isLocalDataReset) onLocalDataReset()
    }
    LaunchedEffect(backupState.restorePreview?.id) {
        if (backupState.restorePreview != null) {
            showImportPasswordDialog = false
            pendingImportUri = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FeatureTopAppBar(
                titleRes = R.string.settings_title,
                onNavigateBack = onNavigateBack,
                backEnabled = !isLoading,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            SettingsBackupSection(
                onNavigateToImport = onNavigateToImport,
                onNavigateToOcr = onNavigateToOcr,
                onExportBackup = { showExportPasswordDialog = true },
                onImportBackup = {
                    importBackupLauncher.launch(
                        arrayOf(
                            "application/vnd.bareuang.backup",
                            "application/octet-stream",
                            "application/json",
                            "text/plain",
                            "*/*",
                        ),
                    )
                },
            )
            AppearanceSettingsGroup(darkMode, settingsViewModel::setDarkMode)
            SettingsWidgetSection(widgetHideBalance, settingsViewModel::setHideBalance)
            SettingsReminderSection(reminderHour, reminderMinute) { showReminderTimeDialog = true }
            SettingsLanguageSection(languageCode) { showLanguageDialog = true }
            SettingsCurrencySection(currency) { showCurrencyDialog = true }
            SettingsSupportSection(
                onReplayTour = {
                    settingsViewModel.resetTour()
                    onReplayTour()
                },
                onOpenPrivacy = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, "https://bareuang.app/privacy".toUri()))
                },
                onDonate = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, "https://trakteer.id/ssajudn".toUri()))
                },
                onRate = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, "https://github.com/Udean777/Bareuang".toUri()))
                },
                onShare = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.settings_share_message))
                    }
                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.settings_share_chooser)))
                },
            )
            SettingsDangerSection { showResetConfirmDialog = true }
            SettingsFooter()
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showReminderTimeDialog) {
        ReminderTimeDialog(
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            onDismiss = { showReminderTimeDialog = false },
            onConfirm = { hour, minute ->
                reminderHour = hour
                reminderMinute = minute
                settingsViewModel.setReminderTime(hour, minute)
                showReminderTimeDialog = false
            },
        )
    }
    if (showLanguageDialog) {
        LanguagePickerDialog(
            currentLanguage = languageCode,
            onSelect = { selected ->
                languageCode = selected
                LanguageManager.setLanguage(context, selected)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false },
        )
    }
    if (showCurrencyDialog) {
        CurrencyPickerDialog(
            current = currency,
            onSelect = { selected ->
                settingsViewModel.setCurrency(selected)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false },
        )
    }
    if (showResetConfirmDialog) {
        AppConfirmDialog(
            title = stringResource(R.string.settings_dialog_reset_title),
            message = stringResource(R.string.settings_dialog_reset_message),
            confirmButtonText = stringResource(R.string.settings_dialog_reset_confirm),
            onDismissRequest = { showResetConfirmDialog = false },
            onConfirm = {
                showResetConfirmDialog = false
                backupViewModel.resetLocalData()
            },
        )
    }
    if (showExportPasswordDialog) {
        ExportPasswordDialog(
            isLoading = isLoading,
            onDismiss = { showExportPasswordDialog = false },
            onContinue = { password ->
                pendingExportPassword = password
                showExportPasswordDialog = false
                exportBackupLauncher.launch(
                    "Bareuang_Backup_${java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())}.bareuang",
                )
            },
        )
    }
    val importUri = pendingImportUri
    if (showImportPasswordDialog && importUri != null) {
        ImportPasswordDialog(
            isLoading = isLoading,
            error = backupState.restoreError,
            onDismiss = {
                showImportPasswordDialog = false
                pendingImportUri = null
                backupViewModel.clearRestoreError()
            },
            onPreview = { password -> backupViewModel.previewBackup(importUri, password) },
        )
    }
    backupState.restorePreview?.let { preview ->
        RestorePreviewDialog(
            preview = preview,
            error = backupState.restoreError,
            isLoading = isLoading,
            onConfirm = { backupViewModel.confirmRestore(preview.id) },
            onDismiss = { backupViewModel.discardRestore(preview.id) },
        )
    }
    backupState.restoreSummary?.let { summary ->
        RestoreSummaryDialog(summary, backupViewModel::dismissRestoreSummary)
    }
}
