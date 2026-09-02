package com.ssajudn.bareuang.ui.tour

import androidx.lifecycle.ViewModel
import com.ssajudn.bareuang.domain.port.TourPreferencesPort
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Hilt-scoped access to tour persistence for composables outside DI reach. */
@HiltViewModel
class TourViewModel @Inject constructor(
    private val tourPrefs: TourPreferencesPort
) : ViewModel() {
    val isTourCompleted: Boolean
        get() = tourPrefs.isTourCompleted

    fun markTourCompleted() = tourPrefs.markTourCompleted()

    fun resetTour() = tourPrefs.resetTour()
}
