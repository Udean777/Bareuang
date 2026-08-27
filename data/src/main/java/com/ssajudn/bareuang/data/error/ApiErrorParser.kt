package com.ssajudn.bareuang.data.error

import com.ssajudn.bareuang.domain.error.AppException
import java.io.IOException

/**
 * Data-layer error mapper — jangan bocorkan IOException mentah ke domain/ui.
 * App ini full-offline, jadi sumber error hanya I/O lokal dan pengecualian umum.
 */
object ApiErrorParser {

    fun fromThrowable(e: Throwable): AppException = when (e) {
        is IOException -> AppException.NetworkException(cause = e)
        is AppException -> e
        else -> AppException.UnknownError(cause = e)
    }

    fun message(e: Throwable): String = when (e) {
        is AppException -> e.message ?: "Terjadi kesalahan"
        else -> "Terjadi kesalahan"
    }
}
