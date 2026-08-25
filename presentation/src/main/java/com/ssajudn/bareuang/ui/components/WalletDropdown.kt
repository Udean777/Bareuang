package com.ssajudn.bareuang.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.utils.CurrencyFormatter

/**
 * Shared wallet picker (ExposedDropdownMenuBox + balance display).
 * Replaces five near-identical copies across AddTransactionScreen,
 * PayDueBillDialog and DepositGoalDialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDropdown(
    wallets: List<Wallet>,
    selectedWalletId: String?,
    label: String,
    modifier: Modifier = Modifier,
    emptyText: String = stringResource(R.string.tx_choose_wallet),
    onSelected: (Wallet) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = wallets.find { it.id == selectedWalletId }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected?.let { "${it.name} (${CurrencyFormatter.formatRupiah(it.balance)})" } ?: emptyText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            wallets.forEach { wallet ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = wallet.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.goals_balance_prefix, CurrencyFormatter.formatRupiah(wallet.balance)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onSelected(wallet)
                        expanded = false
                    }
                )
            }
        }
    }
}
