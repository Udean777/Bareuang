package com.ssajudn.bareuang.data.local

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Persists consent for sending receipt images to the online OCR provider. */
@Singleton
class OcrConsentPreferences @Inject constructor(
    @ApplicationContext context: Context,
) : com.ssajudn.bareuang.domain.port.OcrConsentPort {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override val hasCurrentConsent: Boolean
        get() = prefs.getString(KEY_VERSION, null) == CURRENT_CONSENT_VERSION

    override fun grantCurrentConsent() {
        prefs.edit {
            putString(KEY_VERSION, CURRENT_CONSENT_VERSION)
            putLong(KEY_GRANTED_AT, System.currentTimeMillis())
        }
    }

    override fun revokeConsent() {
        prefs.edit { remove(KEY_VERSION).remove(KEY_GRANTED_AT) }
    }

    companion object {
        // Bump when the provider, purpose, or material privacy disclosure changes.
        const val CURRENT_CONSENT_VERSION = "2026-09-02"
        private const val PREF_NAME = "bareuang_ocr_consent"
        private const val KEY_VERSION = "consent_version"
        private const val KEY_GRANTED_AT = "consent_granted_at"
    }
}
