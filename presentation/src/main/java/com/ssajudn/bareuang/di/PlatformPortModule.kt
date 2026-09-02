package com.ssajudn.bareuang.di

import com.ssajudn.bareuang.domain.port.NetworkMonitorPort
import com.ssajudn.bareuang.utils.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PlatformPortModule {
    @Binds abstract fun bindNetworkMonitor(impl: NetworkMonitor): NetworkMonitorPort
}
