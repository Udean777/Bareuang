package com.ssajudn.bareuang.domain.repository

import com.ssajudn.bareuang.domain.model.CreateWalletRequest
import com.ssajudn.bareuang.domain.model.Wallet
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    suspend fun getWallets(): Result<List<Wallet>>
    suspend fun createWallet(request: CreateWalletRequest): Result<Wallet>
    suspend fun updateWallet(wallet: Wallet): Result<Unit>
    suspend fun deleteWallet(id: String): Result<Boolean>
    fun observeWallets(): Flow<List<Wallet>>
}
