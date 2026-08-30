package com.ssajudn.bareuang.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.ssajudn.bareuang.data.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class AiParsedReceipt(
    val merchant: String,
    val date: String,
    val total: Long,
    val category: String,
    val items: List<String>,
    val rawText: String,
)

private data class ProxyResponse(
    @SerializedName("merchant") val merchant: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("total") val total: Long?,
    @SerializedName("category") val category: String?,
    @SerializedName("items") val items: List<String>?,
    @SerializedName("raw_text") val rawText: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("detail") val detail: String?,
)

@Singleton
class ReceiptAiService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun parseReceiptImage(uri: Uri): Result<AiParsedReceipt> = withContext(Dispatchers.IO) {
        runCatching {
            val base64 = encodeImage(uri)
            val reqJson = gson.toJson(mapOf("image_base64" to base64))
            val req = Request.Builder()
                .url(BuildConfig.PARSE_RECEIPT_URL)
                .post(reqJson.toRequestBody("application/json".toMediaType()))
                .build()
            val resp = client.newCall(req).execute()
            val bodyStr = resp.body?.string() ?: ""
            if (!resp.isSuccessful) {
                val err = runCatching { gson.fromJson(bodyStr, ProxyResponse::class.java)?.error }.getOrNull()
                throw IllegalStateException(err ?: "Gagal memproses struk (${resp.code})")
            }
            val pr = gson.fromJson(bodyStr, ProxyResponse::class.java)
                ?: throw IllegalStateException("Respons tidak valid")
            if (pr.error != null) throw IllegalStateException(pr.error)
            AiParsedReceipt(
                merchant = pr.merchant?.trim() ?: "",
                date = pr.date?.trim() ?: "",
                total = pr.total ?: 0L,
                category = pr.category?.trim() ?: "OTHER",
                items = pr.items ?: emptyList(),
                rawText = pr.rawText?.trim() ?: "",
            )
        }.recoverCatching { e ->
            android.util.Log.e("ReceiptAiService", "parse failed", e)
            throw IllegalStateException(e.message ?: "Gagal memproses struk. Cek koneksi internet.")
        }
    }

    private fun encodeImage(uri: Uri): String {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Gagal membuka gambar")
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(input, null, opts)
        input.close()
        // Re-open for actual decode
        val input2 = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Gagal membuka gambar")
        // Downscale to ~1024px max side to keep body < 3MB and reduce cost (vision resizes to 800 anyway)
        val maxSide = 1024
        val sample = run {
            val w = opts.outWidth.takeIf { it > 0 } ?: maxSide
            val h = opts.outHeight.takeIf { it > 0 } ?: maxSide
            val max = maxOf(w, h)
            var s = 1
            while (max / s > maxSide) s *= 2
            s
        }
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
        var bmp = BitmapFactory.decodeStream(input2, null, decodeOpts)
            ?: throw IllegalStateException("Gagal decode gambar")
        input2.close()

        // Further scale if still > maxSide
        val scale = maxSide.toFloat() / maxOf(bmp.width, bmp.height).toFloat()
        if (scale < 1f) {
            val nw = (bmp.width * scale).toInt()
            val nh = (bmp.height * scale).toInt()
            val scaled = Bitmap.createScaledBitmap(bmp, nw, nh, true)
            if (scaled !== bmp) bmp.recycle()
            bmp = scaled
        }
        val out = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, out)
        bmp.recycle()
        val bytes = out.toByteArray()
        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return "data:image/jpeg;base64,$b64"
    }
}
