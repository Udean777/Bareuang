package com.ssajudn.bareuang.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TopLevelNavigationTest {

    @Test
    fun `primary navigation has five destinations and transfer is a secondary route`() {
        assertEquals(5, TopLevelRoutes.size)
        assertTrue(Screen.Dashboard.route in TopLevelRoutes)
        assertTrue(Screen.AllTransactions.route in TopLevelRoutes)
        assertTrue(Screen.DueBills.route in TopLevelRoutes)
        assertTrue(Screen.Goals.route in TopLevelRoutes)
        assertTrue(Screen.Analytics.route in TopLevelRoutes)
        assertFalse(Screen.Transfer.route in TopLevelRoutes)
    }
}
