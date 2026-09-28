package com.ssajudn.bareuang.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssajudn.bareuang.domain.model.RecurringInterval
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.TransactionEntryTemplate
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.common.labelRes
import com.ssajudn.bareuang.utils.CurrencyFormatter

@Composable
internal fun TransactionQuickReuseSection(
    templates: List<TransactionEntryTemplate>,
    recentTransactions: List<Transaction>,
    recurringIntervalsById: Map<String, RecurringInterval>,
    onUseTemplate: (TransactionEntryTemplate) -> Unit,
    onUseTransaction: (Transaction) -> Unit,
    onDeleteTemplate: (String) -> Unit
) {
    if (templates.isEmpty() && recentTransactions.isEmpty()) return
    var showPicker by remember { mutableStateOf(false) }
    FilledTonalButton(
        onClick = { showPicker = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.tx_reuse_title))
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text(stringResource(R.string.tx_reuse_title)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (templates.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.tx_reuse_favorites),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        templates.forEach { template ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = {
                                        showPicker = false
                                        onUseTemplate(template)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = template.name.takeIf { it != template.category.name }
                                                ?: stringResource(template.category.labelRes()),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = templateSubtitle(
                                                type = template.type,
                                                category = template.category.labelRes().let { stringResource(it) },
                                                amount = template.amount,
                                                recurringInterval = template.recurringInterval
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { onDeleteTemplate(template.id) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = stringResource(R.string.tx_favorite_delete),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    if (templates.isNotEmpty() && recentTransactions.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }

                    if (recentTransactions.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.tx_reuse_recent),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        recentTransactions.forEach { transaction ->
                            TextButton(
                                onClick = {
                                    showPicker = false
                                    onUseTransaction(transaction)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = transaction.merchant?.takeIf {
                                                it.isNotBlank() && it != transaction.category.name
                                            }
                                            ?: stringResource(transaction.category.labelRes()),
                                        fontWeight = FontWeight.SemiBold
                                    )
                            val isRecurring = transaction.isRecurringParent ||
                                transaction.recurringInterval != RecurringInterval.NONE ||
                                transaction.parentRecurringId != null
                            val recurringInterval = transaction.recurringInterval.takeIf {
                                it != RecurringInterval.NONE
                            } ?: transaction.parentRecurringId?.let(recurringIntervalsById::get)
                                ?: RecurringInterval.NONE
                            Text(
                                        text = buildString {
                                            append(typeLabel(transaction.type))
                                            append(" · ")
                                            append(CurrencyFormatter.formatRupiah(transaction.amount))
                                            append(" · ")
                                            append(transaction.date)
                                            if (isRecurring) {
                                                append(" · ")
                                                append(stringResource(R.string.tx_badge_recurring))
                                                if (recurringInterval != RecurringInterval.NONE) {
                                                    append(" · ")
                                                    append(stringResource(recurringInterval.labelRes()))
                                                }
                                            }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun templateSubtitle(
    type: TransactionType,
    category: String,
    amount: Long?,
    recurringInterval: RecurringInterval
): String = buildString {
    append(typeLabel(type))
    append(" · ")
    append(category)
    amount?.let {
        append(" · ")
        append(CurrencyFormatter.formatRupiah(it))
    }
    if (recurringInterval != RecurringInterval.NONE) {
        append(" · ")
        append(stringResource(recurringInterval.labelRes()))
    }
}

@Composable
private fun typeLabel(type: TransactionType): String = stringResource(
    when (type) {
        TransactionType.EXPENSE -> R.string.tx_expense
        TransactionType.INCOME -> R.string.tx_income
        TransactionType.TRANSFER -> R.string.tx_transfer
    }
)
