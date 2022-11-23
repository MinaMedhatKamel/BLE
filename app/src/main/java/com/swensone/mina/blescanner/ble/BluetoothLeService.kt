package com.swensone.mina.blescanner.ble

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.juul.kable.DiscoveredService
import com.juul.kable.Scanner
import com.swensone.mina.blescanner.cancelChildren
import com.swensone.mina.blescanner.childScope
import com.swensone.mina.blescanner.data.AdvertisementWrapper
import com.swensone.mina.blescanner.data.Device
import com.swensone.mina.blescanner.permission.hasAndroidSPermission
import com.swensone.mina.blescanner.permission.hasLocationAndConnectPermissions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import kotlin.collections.List
import kotlin.collections.emptyList
import kotlin.collections.hashMapOf
import kotlin.collections.listOf
import kotlin.collections.set
import kotlin.collections.toList

/**
 * @ScanFailure sealed calss for the failure options
 */
sealed class ScanFailure {
    object BluetoothNotEnabled : ScanFailure()
    object PermissionsMissing : ScanFailure()
    object AndroidSPermissionsMissing : ScanFailure()
    data class OtherFailure(val message: CharSequence) : ScanFailure()
}
/**
 * @ScanFailure sealed class for the scanning status.
 */
sealed class ScanStatus {
    object Idle : ScanStatus()
    object Running : ScanStatus()
    data class Failed(val failure: ScanFailure) : ScanStatus()

    override fun toString(): String {
        return when (this) {
            is Idle -> "Idle"
            is Running -> "Running"
            else -> "Failed"
        }
    }
}
/**
 * @ConnectState sealed class for the Connect State.
 */
sealed class ConnectState {
    object Idle : ConnectState()
    object PeripheralConnecting : ConnectState()
    object PeripheralConnected : ConnectState()

    override fun toString(): String {
        return when (this) {
            is Idle -> "Idle"
            is PeripheralConnecting -> "Connecting"
            is PeripheralConnected -> "Connected"
        }
    }
}

private val SCAN_DURATION_MILLIS = TimeUnit.SECONDS.toMillis(10)


/**
 * @BluetoothLeService background service running using courotines for handling the Asyncrounous operations
 * wrap the scanner from the com.juul.kable I decided to use it to take the advantages of the scopes.
 */

class BluetoothLeService : LifecycleService() {
    private val _job = SupervisorJob()
    private val _scope = CoroutineScope(Dispatchers.IO + _job)
    private val _scanScope = _scope.childScope()

    private val _scanner = Scanner()

    private val _foundDevices = hashMapOf<String, AdvertisementWrapper>()

    private val _scanStatus = MutableStateFlow<ScanStatus>(ScanStatus.Idle)
    val scanStatus = _scanStatus.asStateFlow()

    private val _advertisements =
        MutableStateFlow<List<AdvertisementWrapper>>(emptyList())
    val advertisements = _advertisements.asStateFlow()

    private val _isBluetoothEnabled: Boolean
        get() = BluetoothAdapter.getDefaultAdapter().isEnabled

    /**
     * fun to start scanning in the background
     */
    fun startScan() {
        when {
            _scanStatus.value == ScanStatus.Running -> return
            !hasLocationAndConnectPermissions -> _scanStatus.value =
                ScanStatus.Failed(ScanFailure.PermissionsMissing)
            !_isBluetoothEnabled -> _scanStatus.value =
                ScanStatus.Failed(ScanFailure.BluetoothNotEnabled)
            ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) && !hasAndroidSPermission) -> _scanStatus.value =
                ScanStatus.Failed(ScanFailure.AndroidSPermissionsMissing)
            else -> {
                _scanStatus.value = ScanStatus.Running

                _scanScope.launch {
                    withTimeoutOrNull(SCAN_DURATION_MILLIS) {
                        _scanner
                            .advertisements
                            .catch { cause ->
                                _scanStatus.value =
                                    ScanStatus.Failed(
                                        ScanFailure.OtherFailure(
                                            cause.message ?: "Unknown error"
                                        )
                                    )
                            }
                            .collect { advertisement ->
                                _foundDevices[advertisement.address] =
                                    AdvertisementWrapper(advertisement)
                                _advertisements.value = _foundDevices.values.toList()
                                Log.i("BluetoothLeService", advertisement.toString())
                            }
                    }
                }.invokeOnCompletion {

                    Log.i("BluetoothLeService", "SCAN IS STOPPING ${it}")
                    _scanStatus.value = ScanStatus.Idle
                }
            }
        }
    }

    /**
     * fun to stop scanning.
     */
    fun stopScan() {
        _scanScope.cancelChildren()
    }


    private val _binder = LocalBinder()

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): BluetoothLeService = this@BluetoothLeService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return _binder
    }

    override fun onDestroy() {
        super.onDestroy()
        _job.cancel()
    }

}
