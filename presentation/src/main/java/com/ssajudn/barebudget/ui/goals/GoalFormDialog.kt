package com.ssajudn.barebudget.ui.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssajudn.barebudget.domain.model.Goal
import com.ssajudn.barebudget.presentation.R
import com.ssajudn.barebudget.ui.components.AmountTextField
import com.ssajudn.barebudget.ui.components.AppDatePickerDialog
import com.ssajudn.barebudget.ui.components.AppFormDialog
import com.ssajudn.barebudget.ui.components.bareuangOutlinedTextFieldColors
import com.ssajudn.barebudget.utils.DateUtils

@Composable
fun GoalFormDialog(
    initialGoal: Goal? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetAmount: Long, targetDate: String, colorHex: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialGoal?.name ?: "") }
    var rawAmount by remember { mutableStateOf(initialGoal?.targetAmount?.toString() ?: "") }
    var parsedAmount by remember { mutableStateOf(initialGoal?.targetAmount ?: 0L) }
    var targetDateIso by remember { mutableStateOf(initialGoal?.targetDate ?: "") }
    var selectedColorHex by remember { mutableStateOf(initialGoal?.colorHex ?: presetGoalColors[0]) }
    var notes by remember { mutableStateOf(initialGoal?.notes ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank() && parsedAmount > 0

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDateMillis = if (targetDateIso.isNotBlank()) DateUtils.parseIsoToMillis(targetDateIso) else null,
            onDateSelected = { millis ->
                targetDateIso = DateUtils.formatMillisToIso(millis)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    AppFormDialog(
        title = if (initialGoal != null) stringResource(R.string.goals_form_edit) else stringResource(R.string.goals_form_new),
        icon = Icons.Default.Payments,
        iconTint = MaterialTheme.colorScheme.primary,
        confirmButtonText = if (initialGoal != null) stringResource(R.string.goals_save_changes) else stringResource(R.string.goals_create),
        isConfirmEnabled = isFormValid,
        onDismissRequest = onDismiss,
        onConfirm = {
            onConfirm(name, parsedAmount, targetDateIso, selectedColorHex, notes)
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.goals_name_label)) },
            placeholder = { Text(stringResource(R.string.goals_name_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = bareuangOutlinedTextFieldColors()
        )

        Spacer(modifier = Modifier.height(8.dp))

        AmountTextField(
            value = rawAmount,
            onValueChange = { input ->
                rawAmount = input
                parsedAmount = input.toLongOrNull() ?: 0L
            },
            label = stringResource(R.string.goals_amount_label),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = if (targetDateIso.isNotBlank()) DateUtils.formatDisplayDate(targetDateIso) else "",
            onValueChange = {},
            label = { Text(stringResource(R.string.goals_date_label)) },
            placeholder = { Text(stringResource(R.string.goals_date_hint)) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.goals_date_hint))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            colors = bareuangOutlinedTextFieldColors()
        )

        Spacer(modifier = Modifier.height(10.dp))

        GoalColorRow(
            selectedColorHex = selectedColorHex,
            onSelectColor = { selectedColorHex = it }
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.goals_notes_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = bareuangOutlinedTextFieldColors()
        )
    }
}
