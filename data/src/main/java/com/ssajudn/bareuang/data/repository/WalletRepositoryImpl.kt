package com.ssajudn.bareuang.data.repository

import com.ssajudn.bareuang.domain.model.CreateWalletRequest
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.data.datasource.local.WalletLocalDataSource
import com.ssajudn.bareuang.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val local: WalletLocalDataSource
) : WalletRepository {

    override suspend fun getWallets(): Result<List<Wallet>> =
        local.getWallets()

    override suspend fun createWallet(request: CreateWalletRequest): Result<Wallet> =
        local.createWallet(request)

    override suspend fun updateWallet(wallet: Wallet): Result<Unit> =
        local.updateWallet(wallet)

    override suspend fun deleteWallet(id: String): Result<Boolean> =
        local.deleteWallet(id)

    override fun observeWallets(): Flow<List<Wallet>> =
        local.observeWallets()
}
