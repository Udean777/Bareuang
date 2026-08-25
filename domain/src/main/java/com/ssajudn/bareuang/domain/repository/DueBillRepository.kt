package com.ssajudn.bareuang.domain.repository

import com.ssajudn.bareuang.domain.model.CreateDueBillRequest
import com.ssajudn.bareuang.domain.model.DueBill
import com.ssajudn.bareuang.domain.model.DueBillStatus
import com.ssajudn.bareuang.domain.model.UpdateDueBillRequest
import kotlinx.coroutines.flow.Flow

interface DueBillRepository {
    suspend fun getDueBills(status: String? = null): Result<List<DueBill>>
    suspend fun createDueBill(request: CreateDueBillRequest): Result<DueBill>
    suspend fun updateDueBill(id: String, request: UpdateDueBillRequest): Result<Boolean>
    suspend fun updateDueBillStatus(id: String, status: DueBillStatus, walletId: String? = null): Result<Boolean>
    suspend fun deleteDueBill(id: String): Result<Boolean>
    fun observeDueBills(): Flow<List<DueBill>>
}
