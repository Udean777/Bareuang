package com.ssajudn.bareuang.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssajudn.bareuang.ui.components.AppConfirmDialog
import com.ssajudn.bareuang.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.presentation.BuildConfig
import com.ssajudn.bareuang.domain.AppConfig
import com.ssajudn.bareuang.ui.common.OperationState
import com.ssajudn.bareuang.ui.common.UiEffect
import com.ssajudn.bareuang.ui.common.asString
import com.ssajudn.bareuang.ui.components.AppIconButton
import com.ssajudn.bareuang.ui.components.AppTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onReplayTour: () -> Unit = {},
    onSignOutSuccess: () -> Unit
) {
    val context = LocalContext.current
    // viewModel { } rather than remember { }: a ViewModel created with remember is
    // not lifecycle-scoped, so it was destroyed and recreated on every
    // configuration change and its viewModelScope was not managed by the framework.
    val viewModel: SettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val operation by viewModel.operation.collectAsStateWithLifecycle()
    val isOperationLoading = operation is OperationState.Loading

    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) {
            onSignOutSuccess()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is UiEffect.ShowSnackbarRes -> snackbarHostState.showSnackbar(effect.uiText.asString(context))
                is UiEffect.Navigate -> {}
                is UiEffect.PopBackStack -> onSignOutSuccess()
            }
        }
    }

    val exportBackupLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.exportBackup(it) }
    }

    val importBackupLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.importBackup(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    AppIconButton(enabled = !isOperationLoading, onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(com.ssajudn.bareuang.presentation.R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. OFFLINE BACKUP & RESTORE GROUP
            com.ssajudn.bareuang.ui.components.Material3SettingsGroup(
                title = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_backup_title),
                items = listOf(
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_export_title),
                        description = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_export_desc),
                        icon = Icons.Default.FileDownload,
                        onClick = {
                            val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                            exportBackupLauncher.launch("Bareuang_Backup_$timeStamp.json")
                        }
                    ),
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_import_title),
                        description = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_import_desc),
                        icon = Icons.Default.FileUpload,
                        onClick = {
                            importBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                        }
                    )
                )
            )

            AppearanceSettingsGroup(
                darkMode = darkMode,
                onDarkModeChange = viewModel::setDarkMode,
            )

            // 3b. WIDGET
            val widgetHideBalance by viewModel.widgetHideBalance.collectAsStateWithLifecycle()
            com.ssajudn.bareuang.ui.components.Material3SettingsGroup(
                title = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_widget_title),
                items = listOf(
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_widget_hide_balance),
                        description = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_widget_hide_balance_desc),
                        icon = Icons.Default.VisibilityOff,
                        onClick = { viewModel.setHideBalance(!widgetHideBalance) },
                        trailingContent = {
                            Switch(
                                checked = widgetHideBalance,
                                onCheckedChange = { viewModel.setHideBalance(it) }
                            )
                        }
                    )
                )
            )

            // 3b. BILL REMINDER TIME
            val billReminderPrefs = remember { com.ssajudn.bareuang.data.notification.BillReminderPrefs(context) }
            val billReminderScheduler = remember { com.ssajudn.bareuang.data.notification.BillReminderScheduler(context) }
            var reminderHour by remember { mutableIntStateOf(billReminderPrefs.reminderHour()) }
            var reminderMinute by remember { mutableIntStateOf(billReminderPrefs.reminderMinute()) }
            var showReminderTimeDialog by remember { mutableStateOf(false) }
            com.ssajudn.bareuang.ui.components.Material3SettingsGroup(
                title = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_bill_reminder_title),
                items = listOf(
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_bill_reminder_time),
                        description = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_bill_reminder_time_desc),
                        value = String.format(java.util.Locale.US, "%02d:%02d", reminderHour, reminderMinute),
                        icon = Icons.Default.NotificationsActive,
                        onClick = { showReminderTimeDialog = true }
                    )
                )
            )

            if (showReminderTimeDialog) {
                val timeState = rememberTimePickerState(
                    initialHour = reminderHour,
                    initialMinute = reminderMinute,
                    is24Hour = true
                )
                AlertDialog(
                    onDismissRequest = { showReminderTimeDialog = false },
                    title = {
                        Text(text = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_bill_reminder_time))
                    },
                    text = { TimePicker(state = timeState) },
                    confirmButton = {
                        AppTextButton(onClick = {
                            reminderHour = timeState.hour
                            reminderMinute = timeState.minute
                            billReminderPrefs.setReminderTime(timeState.hour, timeState.minute)
                            billReminderScheduler.scheduleDailyAt(timeState.hour, timeState.minute)
                            showReminderTimeDialog = false
                        }) {
                            Text(stringResource(com.ssajudn.bareuang.presentation.R.string.common_save))
                        }
                    },
                    dismissButton = {
                        AppTextButton(onClick = { showReminderTimeDialog = false }) {
                            Text(stringResource(com.ssajudn.bareuang.presentation.R.string.common_close))
                        }
                    }
                )
            }

            // 4. LANGUAGE SETTINGS
            var currentLanguage by remember { mutableStateOf(com.ssajudn.bareuang.utils.LanguageManager.getCurrentLanguageCode(context)) }
            var showLanguageDialog by remember { mutableStateOf(false) }

            com.ssajudn.bareuang.ui.components.Material3SettingsGroup(
                title = androidx.compose.ui.res.stringResource(com.ssajudn.bareuang.presentation.R.string.language_settings),
                items = listOf(
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = androidx.compose.ui.res.stringResource(com.ssajudn.bareuang.presentation.R.string.language_settings),
                        description = if (currentLanguage == "id") {
                            androidx.compose.ui.res.stringResource(com.ssajudn.bareuang.presentation.R.string.language_indonesian)
                        } else {
                            androidx.compose.ui.res.stringResource(com.ssajudn.bareuang.presentation.R.string.language_english)
                        },
                        icon = Icons.Default.Language,
                        onClick = { showLanguageDialog = true }
                    )
                )
            )

            if (showLanguageDialog) {
                AlertDialog(
                    onDismissRequest = { showLanguageDialog = false },
                    title = {
                        Text(text = androidx.compose.ui.res.stringResource(com.ssajudn.bareuang.presentation.R.string.language_settings))
                    },
                    text = {
                        Column {
                            com.ssajudn.bareuang.utils.LanguageManager.SUPPORTED_LANGUAGES.forEach { (code, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (currentLanguage == code),
                                        onClick = {
                                            currentLanguage = code
                                            com.ssajudn.bareuang.utils.LanguageManager.setLanguage(context, code)
                                            showLanguageDialog = false
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        AppTextButton(onClick = { showLanguageDialog = false }) {
                            Text(stringResource(com.ssajudn.bareuang.presentation.R.string.common_close))
                        }
                    }
                )
            }

            // 4. SUPPORT & APPRECIATION GROUP
            val shareMessage = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_share_message)
            val shareChooser = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_share_chooser)
            com.ssajudn.bareuang.ui.components.Material3SettingsGroup(
                title = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_support_title),
                items = listOf(
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_replay_tour_title),
                        description = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_replay_tour_desc),
                        icon = Icons.Default.Tour,
                        onClick = {
                            viewModel.resetTour()
                            onReplayTour()
                        }
                    ),
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_donate_title),
                        description = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_donate_desc),
                        icon = Icons.Default.VolunteerActivism,
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://trakteer.id/ssajudn")
                            )
                            context.startActivity(intent)
                        }
                    ),
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_star_title),
                        description = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_star_desc),
                        icon = Icons.Default.Star,
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://github.com/Udean777/Bare-Budget")
                            )
                            context.startActivity(intent)
                        }
                    ),
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_share_title),
                        description = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_share_desc),
                        icon = Icons.Default.Share,
                        onClick = {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, shareChooser))
                        }
                    )
                )
            )

            // 5. DANGER ZONE GROUP
            com.ssajudn.bareuang.ui.components.Material3SettingsGroup(
                title = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_danger_title),
                items = listOf(
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_reset_local),
                        description = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_danger_desc),
                        icon = Icons.AutoMirrored.Filled.Logout,
                        isDestructive = true,
                        onClick = { showSignOutConfirmDialog = true }
                    )
                )
            )

            // Minimalist Footer Versioning
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bareuang v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_footer_tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showSignOutConfirmDialog) {
        AppConfirmDialog(
            title = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_dialog_reset_title),
            message = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_dialog_reset_message),
            confirmButtonText = stringResource(com.ssajudn.bareuang.presentation.R.string.settings_dialog_reset_confirm),
            onDismissRequest = { showSignOutConfirmDialog = false },
            onConfirm = {
                showSignOutConfirmDialog = false
                viewModel.signOut()
            }
        )
    }
}