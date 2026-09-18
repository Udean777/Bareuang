package com.ssajudn.bareuang.data.service

import com.ssajudn.bareuang.domain.model.ParsedReceipt
import com.ssajudn.bareuang.domain.port.ReceiptOcrPort
import javax.inject.Singleton
import javax.inject.Inject

@Singleton
class DisabledReceiptOcrService @Inject constructor() : ReceiptOcrPort {

    override val isAvailable: Boolean = false

    override suspend fun parseReceiptImage(uri: String): Result<ParsedReceipt> =
        Result.failure(IllegalStateException("OCR is disabled in release builds"))
}
