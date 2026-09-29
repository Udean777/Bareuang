package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.port.LocalDataResetPort
import com.ssajudn.bareuang.domain.port.OnboardingStatePort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class ResetLocalDataUseCaseTest {
    private val dataResetter = mockk<LocalDataResetPort>()
    private val onboardingState = mockk<OnboardingStatePort>(relaxed = true)
    private val useCase = ResetLocalDataUseCase(dataResetter, onboardingState)

    @Test
    fun `resets onboarding only after local data is wiped`() = runTest {
        coEvery { dataResetter.wipe() } returns Unit

        assertTrue(useCase().isSuccess)

        coVerifySequence {
            dataResetter.wipe()
            onboardingState.resetOnboarding()
        }
    }

    @Test
    fun `does not reset onboarding when wiping local data fails`() = runTest {
        coEvery { dataResetter.wipe() } throws IllegalStateException("wipe failed")

        val result = useCase()

        assertTrue(result.exceptionOrNull() is AppException.DataException)
        coVerify(exactly = 0) { onboardingState.resetOnboarding() }
    }

    @Test(expected = CancellationException::class)
    fun `rethrows cancellation`() = runTest {
        coEvery { dataResetter.wipe() } throws CancellationException("cancelled")

        useCase()
    }
}
