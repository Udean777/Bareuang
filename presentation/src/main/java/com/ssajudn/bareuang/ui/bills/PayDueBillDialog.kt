package com.ssajudn.bareuang.ui.bills

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
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
import com.ssajudn.bareuang.domain.model.DueBill
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.components.AppFormDialog
import com.ssajudn.bareuang.ui.components.WalletDropdown
import com.ssajudn.bareuang.utils.CurrencyFormatter
import com.ssajudn.bareuang.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayDueBillDialog(
    bill: DueBill,
    wallets: List<Wallet>,
    onDismiss: () -> Unit,
    onConfirm: (walletId: String) -> Unit
) {
    var selectedWallet by remember(wallets) { mutableStateOf(wallets.firstOrNull()) }
    val isInsufficient = selectedWallet != null && selectedWallet!!.balance < bill.totalAmount

    AppFormDialog(
        title = stringResource(R.string.bills_pay_title),
        icon = Icons.Default.AccountBalanceWallet,
        iconTint = MaterialTheme.colorScheme.primary,
        confirmButtonText = stringResource(R.string.bills_pay_btn),
        isConfirmEnabled = selectedWallet?.id != null && !isInsufficient,
        onDismissRequest = onDismiss,
        onConfirm = {
            selectedWallet?.id?.let { onConfirm(it) }
        }
    ) {
        // Bill Info Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = bill.providerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = CurrencyFormatter.formatRupiah(bill.totalAmount),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.bills_due_prefix, DateUtils.formatDisplayDate(bill.dueDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = stringResource(R.string.bills_wallet_choose),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        WalletDropdown(
            wallets = wallets,
            selectedWalletId = selectedWallet?.id,
            label = stringResource(R.string.bills_wallet_label),
            onSelected = { selectedWallet = it }
        )

        if (isInsufficient) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.bills_error_insufficient),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.bills_auto_deduct),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
