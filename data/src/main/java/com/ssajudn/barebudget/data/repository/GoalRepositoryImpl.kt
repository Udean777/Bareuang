package com.ssajudn.barebudget.data.repository

import com.ssajudn.barebudget.domain.model.CreateGoalRequest
import com.ssajudn.barebudget.domain.model.Goal
import com.ssajudn.barebudget.domain.model.UpdateGoalRequest
import com.ssajudn.barebudget.data.datasource.local.GoalLocalDataSource
import com.ssajudn.barebudget.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val local: GoalLocalDataSource
) : GoalRepository {

    override suspend fun getGoals(): Result<List<Goal>> =
        local.getGoals()

    override suspend fun createGoal(request: CreateGoalRequest): Result<Goal> =
        local.createGoal(request)

    override suspend fun depositToGoal(id: String, amount: Long, walletId: String): Result<Boolean> =
        local.depositToGoal(id, amount, walletId)

    override suspend fun updateGoal(id: String, request: UpdateGoalRequest): Result<Boolean> =
        local.updateGoal(id, request)

    override suspend fun deleteGoal(id: String): Result<Boolean> =
        local.deleteGoal(id)

    override fun observeGoals(): Flow<List<Goal>> =
        local.observeGoals()
}
