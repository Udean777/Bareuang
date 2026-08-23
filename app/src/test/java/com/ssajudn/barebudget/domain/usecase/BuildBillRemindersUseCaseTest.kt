package com.ssajudn.barebudget.domain.usecase

import com.ssajudn.barebudget.domain.model.DueBill
import com.ssajudn.barebudget.domain.model.DueBillStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildBillRemindersUseCaseTest {

    private val useCase = BuildBillRemindersUseCase()

    private fun bill(id: String, name: String = "PLN", amount: Long = 500_000L, dueDate: String = "2026-01-10") =
        DueBill(
            id = id,
            providerName = name,
            totalAmount = amount,
            dueDate = dueDate,
            status = DueBillStatus.UNPAID
        )

    @Test
    fun `urgency mapping is correct`() {
        assertEquals(BillReminderUrgency.OVERDUE, BuildBillRemindersUseCase.urgencyFor(-5))
        assertEquals(BillReminderUrgency.OVERDUE, BuildBillRemindersUseCase.urgencyFor(-1))
        assertEquals(BillReminderUrgency.TODAY, BuildBillRemindersUseCase.urgencyFor(0))
        assertEquals(BillReminderUrgency.TOMORROW, BuildBillRemindersUseCase.urgencyFor(1))
        assertEquals(BillReminderUrgency.SOON, BuildBillRemindersUseCase.urgencyFor(3))
        assertNull(BuildBillRemindersUseCase.urgencyFor(4))
        assertNull(BuildBillRemindersUseCase.urgencyFor(30))
    }

    @Test
    fun `bills within window produce reminders`() {
        val reminders = useCase(
            listOf(bill("a"), bill("b", name = "WiFi")),
            daysLeftOf = { it.id!!.hashCode().mod(2).toLong() } // 0 atau 1 hari
        )
        assertTrue(reminders.all { it.urgency == BillReminderUrgency.TODAY || it.urgency == BillReminderUrgency.TOMORROW })
        assertEquals(2, reminders.size)
    }

    @Test
    fun `bills far from due date are excluded`() {
        val reminders = useCase(listOf(bill("far")), daysLeftOf = { 10 })
        assertTrue(reminders.isEmpty())
    }

    @Test
    fun `bill without id is skipped safely`() {
        val noId = bill("x").copy(id = null)
        val reminders = useCase(listOf(noId), daysLeftOf = { 0 })
        assertTrue(reminders.isEmpty())
    }

    @Test
    fun `reminder carries bill data for notification`() {
        val reminders = useCase(listOf(bill("pln-1", name = "PLN", amount = 250_000L)), daysLeftOf = { 2 })
        val r = reminders.single()
        assertEquals("pln-1", r.billId)
        assertEquals("PLN", r.providerName)
        assertEquals(250_000L, r.amount)
        assertEquals(BillReminderUrgency.SOON, r.urgency)
    }
}
