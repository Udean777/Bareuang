package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.port.LocalDataResetPort
import com.ssajudn.bareuang.domain.port.OnboardingStatePort
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/** Wipes local records before resetting onboarding, so onboarding never hides a failed wipe. */
class ResetLocalDataUseCase @Inject constructor(
    private val dataResetter: LocalDataResetPort,
    private val onboardingState: OnboardingStatePort,
) {
    suspend operator fun invoke(): Result<Unit> = try {
        dataResetter.wipe()
        onboardingState.resetOnboarding()
        Result.success(Unit)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(AppException.DataException(cause = error))
    }
}
