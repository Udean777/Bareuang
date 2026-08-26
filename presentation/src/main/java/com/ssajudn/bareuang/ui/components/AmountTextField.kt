package com.ssajudn.bareuang.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.utils.CurrencyFormatter
import com.ssajudn.bareuang.utils.CurrencyVisualTransformation

private const val MAX_AMOUNT_DIGITS = 12 // Limit up to hundreds of billions

/**
 * Digits-only amount input with Rupiah visual formatting and numeric keyboard.
 * Emits the sanitized (digits-only) value; callers derive the parsed Long.
 *
 * Styling per DESIGN.MD §7 Input Fields:
 * - Default border: 2dp, muted brown-tinted grey (#857461 = outline token)
 * - Focus border: Honey Yellow (#F4A216 = primary-container)
 * - Focus container: very pale warm tint
 *
 * Pass [textStyle] + borderless [colors] for prominent "hero" inputs; omit for
 * standard form fields.
 */
@Composable
fun AmountTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: @Composable () -> Unit = { Text(CurrencyFormatter.formatCurrency(0L)) },
    isError: Boolean = false,
    enabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    colors: androidx.compose.material3.TextFieldColors = bareuangOutlinedTextFieldColors(),
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            onValueChange(input.filter { it.isDigit() }.take(MAX_AMOUNT_DIGITS))
        },
        label = label?.let { { Text(it) } },
        placeholder = placeholder,
        singleLine = true,
        enabled = enabled,
        visualTransformation = CurrencyVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = textStyle,
        isError = isError,
        colors = colors,
        modifier = modifier
    )
}

/**
 * Bareuang-branded OutlinedTextField colors.
 *
 * Default: 2dp border in muted outline brown (#857461).
 * Focused: border transitions to Honey Yellow (primaryContainer) with a subtle warm tint on the
 * container — DESIGN.MD §7 Input Fields spec.
 *
 * Use this as the default for any OutlinedTextField in the app.
 */
@Composable
fun bareuangOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    // --- Unfocused: outlineVariant lebih soft di light mode (#D8C3AD) — outline #857461 terlalu gelap untuk idle ---
    unfocusedBorderColor   = MaterialTheme.colorScheme.outlineVariant,
    unfocusedLabelColor    = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedContainerColor= Color.Transparent,

    // --- Focused ---
    focusedBorderColor     = MaterialTheme.colorScheme.primaryContainer, // Honey Yellow
    focusedLabelColor      = MaterialTheme.colorScheme.primary,
    focusedContainerColor  = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f),

    // --- Error ---
    errorBorderColor       = MaterialTheme.colorScheme.error,
    errorLabelColor        = MaterialTheme.colorScheme.error,
    errorContainerColor    = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f),

    // --- Disabled ---
    disabledBorderColor    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f),
    disabledContainerColor = Color.Transparent,
)
