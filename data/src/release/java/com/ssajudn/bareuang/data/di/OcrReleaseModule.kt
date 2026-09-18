package com.ssajudn.bareuang.data.di

import com.ssajudn.bareuang.data.service.DisabledReceiptOcrService
import com.ssajudn.bareuang.domain.port.ReceiptOcrPort
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class OcrReleaseModule {
    @Binds
    abstract fun bindReceiptOcr(
        implementation: DisabledReceiptOcrService,
    ): ReceiptOcrPort
}
