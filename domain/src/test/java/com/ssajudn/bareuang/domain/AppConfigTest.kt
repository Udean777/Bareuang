package com.ssajudn.bareuang.domain

import org.junit.Test

import org.junit.Assert.assertFalse

class AppConfigTest {
    @Test
    fun defaultWalletConfiguration_isPresent() {
        assertFalse(AppConfig.DEFAULT_WALLET_NAME.isBlank())
    }
}
