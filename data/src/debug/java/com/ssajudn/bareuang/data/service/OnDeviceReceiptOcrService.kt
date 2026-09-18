package com.ssajudn.bareuang.data.service

import android.content.Context
import androidx.core.net.toUri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.ssajudn.bareuang.domain.model.ParsedReceipt
import com.ssajudn.bareuang.domain.port.ReceiptOcrPort
import com.ssajudn.bareuang.domain.utils.ReceiptParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnDeviceReceiptOcrService @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReceiptOcrPort {

    override val isAvailable: Boolean = true

    private val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS,
    )

    override suspend fun parseReceiptImage(uri: String): Result<ParsedReceipt> = runCatching {
        val image = InputImage.fromFilePath(context, uri.toUri())
        val text = recognizer.process(image).await().text
        ReceiptParser.parse(text)
    }
}
