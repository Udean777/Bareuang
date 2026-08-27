package com.ssajudn.bareuang.data.service

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OcrService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    suspend fun recognizeFromUri(uri: Uri): Result<String> = runCatching {
        try {
            val image = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                InputImage.fromFilePath(context, uri)
            }
            val result = recognizer.process(image).await()
            // Reconstruct layout-aware text from blocks/lines using bounding boxes
            val corrected = reconstructLayout(result)
            if (corrected.isNotBlank() && corrected.lines().size >= 3) corrected else result.text
        } catch (e: Exception) {
            // ponytail: hide system NPE / GMS details from user, log raw for debugging
            android.util.Log.e("OcrService", "recognize failed", e)
            throw IllegalStateException(OCR_GENERIC_ERROR)
        }
    }

    companion object {
        // Keep generic — never leak system exception to UI
        const val OCR_GENERIC_ERROR = "Gagal memproses gambar. Coba lagi dengan foto yang lebih jelas."
    }

    private fun reconstructLayout(text: com.google.mlkit.vision.text.Text): String {
        // Collect all lines with their bounding boxes
        data class LineBox(val text: String, val top: Int, val left: Int, val bottom: Int, val height: Int)
        val lines = mutableListOf<LineBox>()
        for (block in text.textBlocks) {
            for (line in block.lines) {
                val box = line.boundingBox ?: block.boundingBox ?: continue
                lines.add(LineBox(line.text, box.top, box.left, box.bottom, box.height()))
            }
        }
        if (lines.isEmpty()) return text.text
        // Sort by vertical position
        lines.sortWith(compareBy({ it.top }, { it.left }))
        // Group into rows: lines with overlapping vertical range belong to same row
        val rows = mutableListOf<MutableList<LineBox>>()
        for (lb in lines) {
            val lastRow = rows.lastOrNull()
            if (lastRow == null) {
                rows.add(mutableListOf(lb))
            } else {
                val rowTop = lastRow.minOf { it.top }
                val rowBottom = lastRow.maxOf { it.bottom }
                val rowCenter = (rowTop + rowBottom) / 2
                val lineCenter = (lb.top + lb.bottom) / 2
                val avgHeight = lastRow.map { it.height }.average()
                // Same row if vertical centers within 40% of line height
                if (kotlin.math.abs(lineCenter - rowCenter) < avgHeight * 0.6) {
                    lastRow.add(lb)
                } else {
                    rows.add(mutableListOf(lb))
                }
            }
        }
        // For each row, sort by left and join with spacing based on gap
        val rowStrings = rows.map { row ->
            row.sortBy { it.left }
            // If row has 2+ elements far apart, treat as columns: join with 2 spaces
            if (row.size == 1) row[0].text
            else {
                // Estimate if gap is large (e.g. qty vs price)
                val sb = StringBuilder(row[0].text)
                for (i in 1 until row.size) {
                    val gap = row[i].left - (row[i - 1].left + row[i - 1].text.length * 8) // heuristic
                    val sep = if (gap > 40) "    " else " "
                    sb.append(sep).append(row[i].text)
                }
                sb.toString()
            }
        }
        // Post-process: merge orphan quantity lines like "1x 2.000" standalone
        return rowStrings.joinToString("\n")
    }

    suspend fun recognizeFromPath(path: String): Result<String> = recognizeFromUri(Uri.parse(path))
}
