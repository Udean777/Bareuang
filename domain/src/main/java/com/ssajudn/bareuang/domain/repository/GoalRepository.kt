package com.ssajudn.bareuang.domain.repository

import com.ssajudn.bareuang.domain.model.CreateGoalRequest
import com.ssajudn.bareuang.domain.model.Goal
import com.ssajudn.bareuang.domain.model.UpdateGoalRequest
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    suspend fun getGoals(): Result<List<Goal>>
    suspend fun createGoal(request: CreateGoalRequest): Result<Goal>
    suspend fun depositToGoal(id: String, amount: Long, walletId: String): Result<Boolean>
    suspend fun updateGoal(id: String, request: UpdateGoalRequest): Result<Boolean>
    suspend fun deleteGoal(id: String): Result<Boolean>
    fun observeGoals(): Flow<List<Goal>>
}
