package com.ssajudn.bareuang.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.Spacing
import com.ssajudn.bareuang.ui.theme.crispBorder

/**
 * A form dialog: title, optional icon, arbitrary content, confirm/dismiss.
 *
 * Uses a custom [Dialog] to provide larger spacing, a wider form factor,
 * and a more refined Material 3 Expressive look than standard AlertDialogs.
 */
@Composable
fun AppFormDialog(
    title: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    confirmButtonText: String = "",
    dismissButtonText: String? = null,
    confirmButtonContainerColor: Color = MaterialTheme.colorScheme.primary,
    confirmButtonContentColor: Color = contentColorForContainer(confirmButtonContainerColor),
    isConfirmEnabled: Boolean = true,
    contentSpacing: androidx.compose.ui.unit.Dp = Spacing.Small,
    content: @Composable ColumnScope.() -> Unit,
) {
    val effConfirm = if (confirmButtonText.isEmpty()) stringResource(com.ssajudn.bareuang.presentation.R.string.dialog_confirm_save) else confirmButtonText
    val effDismiss = dismissButtonText ?: stringResource(com.ssajudn.bareuang.presentation.R.string.dialog_cancel)
    val showDismiss = dismissButtonText != null || true
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .imePadding()
                .heightIn(max = 640.dp)
                .crispBorder(
                    shape = AppShapes.Squircle,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
            shape = AppShapes.Squircle,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(Spacing.Medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(bottom = Spacing.ExtraSmall)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.Medium))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(contentSpacing),
                    content = content
                )

                Spacer(modifier = Modifier.height(Spacing.Medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (showDismiss) {
                        AppTextButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.padding(end = Spacing.Small)
                        ) {
                            Text(effDismiss)
                        }
                    }
                    AppButton(
                        onClick = onConfirm,
                        enabled = isConfirmEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = confirmButtonContainerColor,
                            contentColor = confirmButtonContentColor,
                        )
                    ) {
                        Text(effConfirm)
                    }
                }
            }
        }
    }
}

/**
 * Confirmation dialog for a destructive or otherwise irreversible action.
 */
@Composable
fun AppConfirmDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String? = null,
    message: String? = null,
    confirmButtonText: String? = null,
    dismissButtonText: String? = null,
    icon: ImageVector = Icons.Default.DeleteOutline,
    isDestructive: Boolean = true,
) {
    val effTitle = title ?: stringResource(com.ssajudn.bareuang.presentation.R.string.dialog_default_title)
    val effMessage = message ?: stringResource(com.ssajudn.bareuang.presentation.R.string.dialog_default_message)
    val effConfirm = confirmButtonText ?: stringResource(com.ssajudn.bareuang.presentation.R.string.dialog_confirm_delete)
    val effDismiss = dismissButtonText ?: stringResource(com.ssajudn.bareuang.presentation.R.string.dialog_cancel)
    val containerColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .crispBorder(
                    shape = AppShapes.Squircle,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
            shape = AppShapes.Squircle,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(Spacing.Large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = containerColor,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(bottom = Spacing.Small)
                )

                Text(
                    text = effTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.Medium))

                Text(
                    text = effMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.Large))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    AppTextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.padding(end = Spacing.Small)
                    ) {
                        Text(effDismiss)
                    }
                    AppButton(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = containerColor,
                            contentColor = contentColorForContainer(containerColor),
                        )
                    ) {
                        Text(effConfirm)
                    }
                }
            }
        }
    }
}

/**
 * Picks the matching `on*` role for a container colour.
 */
@Composable
private fun contentColorForContainer(container: Color): Color {
    val scheme = MaterialTheme.colorScheme
    return when (container) {
        scheme.primary -> scheme.onPrimary
        scheme.secondary -> scheme.onSecondary
        scheme.tertiary -> scheme.onTertiary
        scheme.error -> scheme.onError
        scheme.primaryContainer -> scheme.onPrimaryContainer
        scheme.secondaryContainer -> scheme.onSecondaryContainer
        scheme.tertiaryContainer -> scheme.onTertiaryContainer
        scheme.errorContainer -> scheme.onErrorContainer
        else -> scheme.onPrimary
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialDateMillis: Long? = null,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis ?: System.currentTimeMillis()
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    onDismiss()
                }
            ) {
                Text(stringResource(com.ssajudn.bareuang.presentation.R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(com.ssajudn.bareuang.presentation.R.string.common_cancel))
            }
        }
    ) {
        androidx.compose.material3.DatePicker(state = datePickerState)
    }
}
