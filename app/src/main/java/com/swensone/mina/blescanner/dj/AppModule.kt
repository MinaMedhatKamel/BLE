package com.swensone.mina.blescanner.dj

import android.content.Context
import com.swensone.mina.blescanner.ble.BluetoothLeServiceWrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideEpcServiceWrapper(
        @ApplicationContext applicationContext: Context,
    ): BluetoothLeServiceWrapper {
        return BluetoothLeServiceWrapper(
            applicationContext
        )
    }
}