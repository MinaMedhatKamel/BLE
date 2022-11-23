package com.swensone.mina.blescanner.ble

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import com.juul.kable.DiscoveredService
import com.swensone.mina.blescanner.data.AdvertisementWrapper
import com.swensone.mina.blescanner.data.Device
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch

/**
 * I used this wrapper for avoiding holding a refrence of the services andorid component inside the viewmodel.
 * it is a lifecycle aware.
 */
class BluetoothLeServiceWrapper(val applicationContext: Context) : LifecycleObserver {

    private val _advertisements: MutableStateFlow<List<AdvertisementWrapper>> =
        MutableStateFlow(emptyList())

    public val advertisements: StateFlow<List<AdvertisementWrapper>> = _advertisements.asStateFlow()


    private val _scanStatus: MutableStateFlow<ScanStatus?> = MutableStateFlow(null)

    public val scanStatus: StateFlow<ScanStatus?> = _scanStatus.asStateFlow()

    protected lateinit var _service: BluetoothLeService

    private var _bound: Boolean = false

    public val connection: ServiceConnection = object : ServiceConnection {
        public override fun onServiceConnected(className: ComponentName, service: IBinder): Unit {
            val binder = service as BluetoothLeService.LocalBinder
            _service = binder.getService()
            _bound = true
            _service.lifecycleScope.launch {
                launch {
                    _advertisements.emitAll(_service.advertisements)
                }
                launch {
                    _scanStatus.emitAll(_service.scanStatus)
                }
            }
        }

        public override fun onServiceDisconnected(className: ComponentName): Unit {
            _bound = false
        }
    }

    fun startScan() {
        _service.startScan()
    }

    fun stopScan() {
        _service.stopScan()
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public fun handleLifecycleStart(): Unit {
        Intent(applicationContext, BluetoothLeService::class.java).also { intent ->
            applicationContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }
}