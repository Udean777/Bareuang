package com.ssajudn.bareuang.data.datasource.local

import com.ssajudn.bareuang.data.local.room.AppDatabase
import com.ssajudn.bareuang.data.local.room.LocalGoalEntity
import com.ssajudn.bareuang.data.local.room.LocalTransactionEntity
import com.ssajudn.bareuang.domain.model.CreateGoalRequest
import com.ssajudn.bareuang.domain.model.Goal
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.UpdateGoalRequest
import com.ssajudn.bareuang.data.service.WalletBalanceService
import com.ssajudn.bareuang.domain.utils.DateUtils
import com.ssajudn.bareuang.data.error.ApiErrorParser
import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.error.GoalOperationReason
import com.ssajudn.bareuang.domain.error.WalletOperationReason
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalLocalDataSource @Inject constructor(
    private val db: AppDatabase,
    private val balanceService: WalletBalanceService
) {

    suspend fun getGoals(): Result<List<Goal>> = withContext(Dispatchers.IO) {
        try {
            Result.success(db.goalDao().getAllGoals().map { it.toGoal() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun createGoal(request: CreateGoalRequest): Result<Goal> = withContext(Dispatchers.IO) {
        try {
            if (request.name.isBlank()) return@withContext Result.failure(AppException.GoalOperationException(GoalOperationReason.INVALID_NAME))
            if (request.targetAmount <= 0) return@withContext Result.failure(AppException.GoalOperationException(GoalOperationReason.INVALID_TARGET))
            val localGoal = Goal(
                id = UUID.randomUUID().toString(),
                name = request.name.trim(),
                targetAmount = request.targetAmount,
                currentAmount = 0L,
                targetDate = request.targetDate,
                colorHex = request.colorHex,
                notes = request.notes
            )
            db.goalDao().insertGoal(LocalGoalEntity.fromGoal(localGoal, isSynced = false))
            Result.success(localGoal)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return@withContext Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun depositToGoal(id: String, amount: Long, walletId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                if (amount == 0L || amount == Long.MIN_VALUE) return@withContext Result.failure(AppException.GoalOperationException(GoalOperationReason.INVALID_AMOUNT))
                if (walletId.isBlank()) return@withContext Result.failure(AppException.GoalOperationException(GoalOperationReason.WALLET_REQUIRED))
                val block: () -> Unit = {
                    val goal = db.goalDao().getGoalById(id)
                        ?: throw AppException.GoalOperationException(GoalOperationReason.GOAL_NOT_FOUND)
                    if (amount < 0 && goal.currentAmount < -amount) {
                        throw AppException.GoalOperationException(GoalOperationReason.INSUFFICIENT_GOAL_BALANCE)
                    }
                    val wallet = db.walletDao().getWalletById(walletId)
                        ?: throw AppException.WalletOperationException(WalletOperationReason.NOT_FOUND)
                    if (amount > 0 && wallet.balance < amount) {
                        throw AppException.InsufficientBalanceException(wallet.balance, amount)
                    }
                    db.goalDao().depositToGoal(id, amount)
                    val isDeposit = amount > 0
                    balanceService.add(walletId, -amount)
                    val goalEntity = db.goalDao().getGoalById(id)
                    val goalName = goalEntity?.name ?: "Tabungan"
                    val absAmount = kotlin.math.abs(amount)
                    val txType = if (isDeposit) TransactionType.EXPENSE else TransactionType.INCOME
                    val merchantName = if (isDeposit) "Tabungan: $goalName" else "Penarikan: $goalName"
                    val localTx = Transaction(
                        id = UUID.randomUUID().toString(),
                        amount = absAmount,
                        type = txType,
                        category = TransactionCategory.OTHER,
                        merchant = merchantName,
                        date = DateUtils.getCurrentDateISO(),
                        notes = if (isDeposit) "Setor ke tabungan $goalName" else "Penarikan dari tabungan $goalName",
                        walletId = walletId
                    )
                    db.transactionDao().insertTransaction(
                        LocalTransactionEntity.fromTransaction(localTx, isSynced = false)
                    )
                }
                db.runInTransaction { block() }
                Result.success(true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

    suspend fun updateGoal(id: String, request: UpdateGoalRequest): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                if (request.name.isBlank()) return@withContext Result.failure(AppException.GoalOperationException(GoalOperationReason.INVALID_NAME))
                if (request.targetAmount <= 0) return@withContext Result.failure(AppException.GoalOperationException(GoalOperationReason.INVALID_TARGET))
                db.goalDao().updateGoal(
                    id = id,
                    name = request.name.trim(),
                    targetAmount = request.targetAmount,
                    targetDate = request.targetDate.ifBlank { null },
                    colorHex = request.colorHex,
                    notes = request.notes,
                    isSynced = false
                )
                Result.success(true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

    suspend fun deleteGoal(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            db.goalDao().deleteGoal(id)
            Result.success(true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    fun observeGoals(): Flow<List<Goal>> =
        db.goalDao().observeAllGoals().map { list -> list.map { it.toGoal() } }
}
