package com.ssajudn.bareuang.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.components.AmountTextField
import com.ssajudn.bareuang.ui.components.AppButton
import com.ssajudn.bareuang.ui.components.AppTextButton
import com.ssajudn.bareuang.ui.theme.Spacing

@Composable
fun QuickSetupScreen(
    onFinish: () -> Unit,
    viewModel: QuickSetupViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = Spacing.ScreenHorizontal, vertical = 12.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                AppButton(
                    onClick = { viewModel.save(onFinish) },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(if (state.isSaving) R.string.setup_saving else R.string.setup_start),
                        fontWeight = FontWeight.Bold,
                    )
                }
                AppTextButton(
                    onClick = { viewModel.skip(onFinish) },
                    enabled = !state.isSaving,
                ) {
                    Text(stringResource(R.string.setup_skip))
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.ScreenHorizontal)
                .padding(top = 28.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.setup_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.setup_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.setup_wallet_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.setup_wallet_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = state.walletName,
                onValueChange = viewModel::onWalletNameChange,
                label = { Text(stringResource(R.string.setup_wallet_name)) },
                singleLine = true,
                isError = state.hasError && state.walletName.isBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
            AmountTextField(
                value = state.openingBalanceInput,
                onValueChange = viewModel::onOpeningBalanceChange,
                label = stringResource(R.string.setup_wallet_balance),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.setup_budget_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.setup_budget_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AmountTextField(
                value = state.monthlyBudgetInput,
                onValueChange = viewModel::onMonthlyBudgetChange,
                label = stringResource(R.string.setup_budget_amount),
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.hasError) {
                Text(
                    text = stringResource(R.string.setup_save_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
