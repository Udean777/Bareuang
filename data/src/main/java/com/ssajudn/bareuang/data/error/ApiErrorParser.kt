package com.ssajudn.bareuang.data.error

import com.ssajudn.bareuang.domain.error.AppException
import java.io.IOException

/**
 * Data-layer error mapper — jangan bocorkan IOException mentah ke domain/ui.
 * Core app tetap offline-first; sumber error juga dapat berasal dari OCR online
 * opsional dan pengecualian I/O lokal.
 */
object ApiErrorParser {

    fun fromThrowable(e: Throwable): AppException = when (e) {
        is IOException -> AppException.NetworkException(cause = e)
        is AppException -> e
        is IllegalArgumentException, is IllegalStateException -> AppException.DataException(e.message, e)
        else -> AppException.UnknownError(cause = e)
    }

    fun message(e: Throwable): String = when (e) {
        is AppException -> e.message ?: "Terjadi kesalahan"
        else -> "Terjadi kesalahan"
    }
}
