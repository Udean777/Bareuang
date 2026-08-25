package com.ssajudn.bareuang.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.domain.model.CategorySummary
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.usecase.CalculateSavageStreakUseCase
import com.ssajudn.bareuang.domain.usecase.GetCashflowAnalyticsUseCase
import com.ssajudn.bareuang.domain.usecase.GetDashboardSummaryUseCase
import com.ssajudn.bareuang.domain.usecase.GetNetWorthAnalyticsUseCase
import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.domain.error.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.CashflowDataPoint
import com.ssajudn.bareuang.domain.model.NetWorthDataPoint

enum class AnalyticsTab(val title: String) {
    CASHFLOW("Arus Kas"),
    NET_WORTH("Kekayaan"),
    CATEGORIES("Kategori")
}

data class CategoryBreakdownItem(
    val category: TransactionCategory,
    val totalAmount: Long,
    val transactionCount: Long,
    val percentage: Float // 0.0f - 1.0f
)

sealed interface AnalyticsUiState {
    object Loading : AnalyticsUiState
    data class Success(
        val totalSpent: Long,
        val totalIncome: Long,
        val netWorth: Long,
        val monthlyBudget: Long,
        val dailyAverage: Long,
        val topSpendingCategory: CategoryBreakdownItem?,
        val categories: List<CategoryBreakdownItem>,
        val savageStreakDays: Int,
        val cashflowTrend: List<CashflowDataPoint>,
        val netWorthTrend: List<NetWorthDataPoint>,
        val selectedTab: AnalyticsTab = AnalyticsTab.CASHFLOW
    ) : AnalyticsUiState

    data class Error(val message: String) : AnalyticsUiState
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getDashboardSummary: GetDashboardSummaryUseCase,
    private val transactionRepository: TransactionRepository,
    private val getCashflow: GetCashflowAnalyticsUseCase,
    private val getNetWorth: GetNetWorthAnalyticsUseCase,
    private val calculateSavageStreak: CalculateSavageStreakUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadAnalyticsData()
    }

    fun selectTab(tab: AnalyticsTab) {
        val current = _uiState.value
        if (current is AnalyticsUiState.Success) {
            _uiState.value = current.copy(selectedTab = tab)
        }
    }

    fun loadAnalyticsData(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isPullToRefresh) {
                _isRefreshing.value = true
            } else if (_uiState.value !is AnalyticsUiState.Success) {
                _uiState.value = AnalyticsUiState.Loading
            }

            val summaryResult = getDashboardSummary()
            val transactionsResult = transactionRepository.getTransactions(limit = 100)
            val cashflowResult = getCashflow()
            val netWorthResult = getNetWorth()

            if (summaryResult.isSuccess) {
                val summary = summaryResult.getOrNull()!!
                val transactions = transactionsResult.getOrDefault(emptyList())
                val cashflow = cashflowResult.getOrDefault(emptyList())
                val netWorthTrend = netWorthResult.getOrDefault(emptyList())

                val totalSpent = summary.totalSpent
                val totalIncome = cashflow.lastOrNull()?.income ?: 0L
                val netWorth = summary.netWorth
                val topCategoriesRaw = summary.topCategories ?: emptyList()

                val breakdownItems = topCategoriesRaw.map { catSummary ->
                    val pct = if (totalSpent > 0) (catSummary.total.toFloat() / totalSpent.toFloat()) else 0f
                    CategoryBreakdownItem(
                        category = catSummary.category,
                        totalAmount = catSummary.total,
                        transactionCount = catSummary.count,
                        percentage = pct
                    )
                }.sortedByDescending { it.totalAmount }

                val topCat = breakdownItems.firstOrNull()
                val streak = calculateSavageStreak(transactions)
                val prevTab = (_uiState.value as? AnalyticsUiState.Success)?.selectedTab ?: AnalyticsTab.CASHFLOW

                _uiState.value = AnalyticsUiState.Success(
                    totalSpent = totalSpent,
                    totalIncome = totalIncome,
                    netWorth = netWorth,
                    monthlyBudget = summary.monthlyBudget,
                    dailyAverage = summary.averageDailySpend,
                    topSpendingCategory = topCat,
                    categories = breakdownItems,
                    savageStreakDays = streak,
                    cashflowTrend = cashflow,
                    netWorthTrend = netWorthTrend,
                    selectedTab = prevTab
                )
                _isRefreshing.value = false
            } else {
                _isRefreshing.value = false
                if (_uiState.value !is AnalyticsUiState.Success) {
                    val ex = summaryResult.exceptionOrNull()
                    _uiState.value = AnalyticsUiState.Error(
                        (ex as? AppException)?.userMessage() ?: ex?.localizedMessage ?: "Failed to load analytics"
                    )
                }
            }
        }
    }
}
