package com.ssajudn.bareuang.ui.budget

import com.ssajudn.bareuang.ui.theme.IncomeAccent
import com.ssajudn.bareuang.ui.theme.ExpenseAccent
import com.ssajudn.bareuang.ui.theme.BudgetWarningAccent
import com.ssajudn.bareuang.ui.common.OperationState
import com.ssajudn.bareuang.ui.common.UiEffect
import com.ssajudn.bareuang.ui.common.asString
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.Spacing
import com.ssajudn.bareuang.ui.theme.crispBorder
import com.ssajudn.bareuang.ui.tour.tourAnchor
import com.ssajudn.bareuang.utils.CurrencyFormatter
import com.ssajudn.bareuang.ui.components.AmountTextField

import com.ssajudn.bareuang.ui.components.AppButton
import com.ssajudn.bareuang.ui.components.BaruangPrimaryButton
import com.ssajudn.bareuang.ui.components.AppIconButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BudgetScreen(
    onNavigateBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val operation by viewModel.operation.collectAsStateWithLifecycle()
    val isOperationLoading = operation is OperationState.Loading
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is UiEffect.ShowSnackbarRes -> snackbarHostState.showSnackbar(effect.uiText.asString(context))
                is UiEffect.Navigate -> {}
                is UiEffect.PopBackStack -> {}
            }
        }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.budget_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    AppIconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                BaruangPrimaryButton(
                    onClick = { viewModel.saveBudget() },
                    enabled = !uiState.isLoading && uiState.parsedAmount > 0 && !uiState.isLocked && !isOperationLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(52.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = if (uiState.isLocked) stringResource(R.string.budget_locked_btn) else stringResource(R.string.budget_save_btn),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Spacing.ScreenHorizontal)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Explanatory Card (M3 ElevatedCard)
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .tourAnchor("budget_explainer")
                    .crispBorder(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.budget_runway_banner_title),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.budget_runway_banner_desc),
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (uiState.isLocked) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .crispBorder(
                            shape = AppShapes.Squircle,
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
                        ),
                    shape = AppShapes.Squircle,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.budget_locked_title),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.budget_locked_desc, CurrencyFormatter.formatRupiah(uiState.currentLimit)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Amount Input — bear peek when amount entered
            Box(modifier = Modifier.fillMaxWidth()) {
                com.ssajudn.bareuang.ui.components.BearPeek(
                    visible = uiState.parsedAmount > 0,
                    modifier = Modifier.align(Alignment.TopEnd).offset(y = (-10).dp, x = 4.dp),
                    size = 38.dp
                )
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (uiState.parsedAmount > 0) 12.dp else 0.dp)
                        .tourAnchor("budget_input_amount"),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.budget_input_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AmountTextField(
                        value = uiState.rawAmount,
                        onValueChange = { if (!uiState.isLocked) viewModel.onAmountChange(it) },
                        enabled = !uiState.isLocked,
                        placeholder = {
                            Text(
                                "Rp 0",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            )
                        },
                        textStyle = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                }
            }

            // Quick Selection Presets
            Text(
                text = stringResource(R.string.budget_presets_label),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(2_000_000L, 3_500_000L, 5_000_000L).forEach { preset ->
                    SuggestionChip(
                        onClick = { if (!uiState.isLocked) viewModel.onAmountChange(preset.toString()) },
                        enabled = !uiState.isLocked,
                        label = {
                            Text(
                                CurrencyFormatter.formatCompact(preset),
                                fontWeight = FontWeight.Medium
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )

            // CATEGORY BUDGETS SECTION
            var showCategoryDialog by remember { mutableStateOf(false) }
            var editingCategoryBudget by remember { mutableStateOf<com.ssajudn.bareuang.domain.model.CategoryBudget?>(null) }
            var categoryToDelete by remember { mutableStateOf<com.ssajudn.bareuang.domain.model.CategoryBudget?>(null) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.budget_category_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.budget_category_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(
                    onClick = {
                        editingCategoryBudget = null
                        showCategoryDialog = true
                    },
                    shape = AppShapes.Pill,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.common_add), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            // Allocation Summary
            if (uiState.categoryBudgets.isNotEmpty()) {
                Surface(
                    shape = AppShapes.Squircle,
                    color = if (uiState.isOverAllocated) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.isOverAllocated) Icons.Default.Warning else Icons.Default.PieChart,
                            contentDescription = null,
                            tint = if (uiState.isOverAllocated) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (uiState.isOverAllocated) {
                                stringResource(R.string.budget_category_over_warning)
                            } else {
                                stringResource(
                                    R.string.budget_category_allocated,
                                    CurrencyFormatter.formatCompact(uiState.totalAllocatedCategory),
                                    if (uiState.currentLimit > 0) CurrencyFormatter.formatCompact(uiState.currentLimit) else "unlimited"
                                )
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (uiState.isOverAllocated) FontWeight.Bold else FontWeight.Medium,
                                color = if (uiState.isOverAllocated) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Category list
            if (uiState.categoryBudgets.isEmpty()) {
                Surface(
                    shape = AppShapes.Squircle,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.budget_category_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                uiState.categoryBudgets.forEach { catBudget ->
                    CategoryBudgetCard(
                        categoryBudget = catBudget,
                        onEdit = {
                            editingCategoryBudget = catBudget
                            showCategoryDialog = true
                        },
                        onDelete = {
                            categoryToDelete = catBudget
                        }
                    )
                }
            }

            // Category Form Dialog
            if (showCategoryDialog) {
                SetCategoryBudgetDialog(
                    initialBudget = editingCategoryBudget,
                    existingCategories = uiState.categoryBudgets.map { it.category }.toSet(),
                    onDismiss = { showCategoryDialog = false },
                    onConfirm = { category, limit ->
                        viewModel.setCategoryBudget(category, limit)
                        showCategoryDialog = false
                    }
                )
            }

            // Category Delete Confirmation Dialog
            if (categoryToDelete != null) {
                com.ssajudn.bareuang.ui.components.AppConfirmDialog(
                    title = stringResource(R.string.budget_category_delete),
                    message = stringResource(R.string.budget_category_delete_confirm, categoryToDelete!!.category.displayName),
                    confirmButtonText = stringResource(R.string.common_delete),
                    dismissButtonText = stringResource(R.string.common_cancel),
                    onConfirm = {
                        viewModel.deleteCategoryBudget(categoryToDelete!!.category)
                        categoryToDelete = null
                    },
                    onDismissRequest = { categoryToDelete = null }
                )
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun CategoryBudgetCard(
    categoryBudget: com.ssajudn.bareuang.domain.model.CategoryBudget,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val category = categoryBudget.category
    val catColors = com.ssajudn.bareuang.ui.theme.categoryColors
    val progress = categoryBudget.progressPercentage
    val progressColor = when {
        categoryBudget.isOverspent -> MaterialTheme.colorScheme.error
        categoryBudget.isWarning -> BudgetWarningAccent
        else -> MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Squircle,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = catColors.container(category),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = com.ssajudn.bareuang.ui.components.getCategoryIcon(category),
                                contentDescription = null,
                                tint = catColors.onContainer(category),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${CurrencyFormatter.formatCompact(categoryBudget.spentAmount)} / ${CurrencyFormatter.formatCompact(categoryBudget.limitAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = progressColor,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    AppIconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    AppIconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetCategoryBudgetDialog(
    initialBudget: com.ssajudn.bareuang.domain.model.CategoryBudget?,
    existingCategories: Set<com.ssajudn.bareuang.domain.model.TransactionCategory>,
    onDismiss: () -> Unit,
    onConfirm: (com.ssajudn.bareuang.domain.model.TransactionCategory, Long) -> Unit
) {
    val expenseCategories = com.ssajudn.bareuang.domain.model.TransactionCategory.entries.filter {
        it != com.ssajudn.bareuang.domain.model.TransactionCategory.TRANSFER &&
        it != com.ssajudn.bareuang.domain.model.TransactionCategory.SALARY &&
        it != com.ssajudn.bareuang.domain.model.TransactionCategory.BONUS &&
        it != com.ssajudn.bareuang.domain.model.TransactionCategory.INVESTMENT
    }

    val availableCategories = if (initialBudget != null) {
        listOf(initialBudget.category)
    } else {
        expenseCategories.filter { it !in existingCategories }.ifEmpty { expenseCategories }
    }

    var selectedCategory by remember { mutableStateOf(initialBudget?.category ?: availableCategories.firstOrNull() ?: expenseCategories.first()) }
    var rawAmount by remember { mutableStateOf(initialBudget?.limitAmount?.toString() ?: "") }
    var parsedAmount by remember { mutableStateOf(initialBudget?.limitAmount ?: 0L) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    com.ssajudn.bareuang.ui.components.AppFormDialog(
        title = if (initialBudget != null) stringResource(R.string.budget_category_edit) else stringResource(R.string.budget_category_add),
        icon = Icons.Default.Category,
        iconTint = MaterialTheme.colorScheme.primary,
        confirmButtonText = stringResource(R.string.common_save),
        isConfirmEnabled = parsedAmount > 0,
        onDismissRequest = onDismiss,
        onConfirm = { onConfirm(selectedCategory, parsedAmount) }
    ) {
        if (initialBudget == null) {
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.budget_category_select)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    availableCategories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.displayName) },
                            leadingIcon = {
                                Icon(
                                    imageVector = com.ssajudn.bareuang.ui.components.getCategoryIcon(cat),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                selectedCategory = cat
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        AmountTextField(
            value = rawAmount,
            onValueChange = { input ->
                rawAmount = input
                parsedAmount = input.toLongOrNull() ?: 0L
            },
            label = stringResource(R.string.budget_category_limit),
            modifier = Modifier.fillMaxWidth()
        )
    }
}