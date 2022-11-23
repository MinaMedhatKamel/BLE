package com.swensone.mina.blescanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swensone.mina.blescanner.ble.BluetoothLeServiceWrapper
import com.swensone.mina.blescanner.ble.ScanStatus
import com.swensone.mina.blescanner.data.AdvertisementWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


//avoid holding the refrence from the servies inside the viewmodel
@HiltViewModel
class BleViewModel @Inject constructor(
    private val bluetoothLeService: BluetoothLeServiceWrapper
) : ViewModel() {
    val advertisements = bluetoothLeService.advertisements

    val scanStatus = bluetoothLeService.scanStatus

    val isScanRunning: StateFlow<Boolean>
        get() = scanStatus.map { it is ScanStatus.Running }
            .stateIn(scope = viewModelScope, SharingStarted.Eagerly, false)

    fun toggleScan() {
        when (scanStatus.value) {
            is ScanStatus.Running -> stopScan()
            is ScanStatus.Idle -> startScan()
            else -> {
            }
        }
    }

    fun startScan() {
        bluetoothLeService.startScan()
    }

    fun stopScan() {
        bluetoothLeService.stopScan()
    }

}
