package com.ssajudn.bareuang.data.repository

import com.ssajudn.bareuang.domain.model.CreateDueBillRequest
import com.ssajudn.bareuang.domain.model.DueBill
import com.ssajudn.bareuang.domain.model.DueBillStatus
import com.ssajudn.bareuang.domain.model.UpdateDueBillRequest
import com.ssajudn.bareuang.data.datasource.local.DueBillLocalDataSource
import com.ssajudn.bareuang.domain.repository.DueBillRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DueBillRepositoryImpl @Inject constructor(
    private val local: DueBillLocalDataSource
) : DueBillRepository {

    override suspend fun getDueBills(status: String?): Result<List<DueBill>> =
        local.getDueBills(status)

    override suspend fun createDueBill(request: CreateDueBillRequest): Result<DueBill> =
        local.createDueBill(request)

    override suspend fun updateDueBill(id: String, request: UpdateDueBillRequest): Result<Boolean> =
        local.updateDueBill(id, request)

    override suspend fun updateDueBillStatus(id: String, status: DueBillStatus, walletId: String?): Result<Boolean> =
        local.updateDueBillStatus(id, status, walletId)

    override suspend fun deleteDueBill(id: String): Result<Boolean> =
        local.deleteDueBill(id)

    override fun observeDueBills(): Flow<List<DueBill>> =
        local.observeDueBills()
}
