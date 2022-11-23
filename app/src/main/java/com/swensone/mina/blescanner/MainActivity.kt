package com.swensone.mina.blescanner

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.swensone.mina.blescanner.ble.BluetoothLeServiceWrapper
import com.swensone.mina.blescanner.ble.ScanFailure
import com.swensone.mina.blescanner.ble.ScanStatus
import com.swensone.mina.blescanner.compose.ScanningScreen
import com.swensone.mina.blescanner.permission.RequestCode
import com.swensone.mina.blescanner.permission.enableBluetooth
import com.swensone.mina.blescanner.permission.requestBleForAndroidS
import com.swensone.mina.blescanner.permission.requestLocationAndConnectPermissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var bluetoothLeServiceWrapper: BluetoothLeServiceWrapper

    private val viewModel: BleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // add the to the life scycle check @BluetoothLeServiceWrapper to see the on start event attached.
        lifecycle.addObserver(bluetoothLeServiceWrapper)
        setContent { ScanningScreen() }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.scanStatus.collect {
                    if (it is ScanStatus.Failed) {
                        when (it.failure) {
                            is ScanFailure.BluetoothNotEnabled -> enableBluetooth()
                            is ScanFailure.PermissionsMissing -> requestLocationAndConnectPermissions()
                            is ScanFailure.AndroidSPermissionsMissing -> requestBleForAndroidS()
                            else -> {
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (permissions.isEmpty()) {
            Toast.makeText(
                applicationContext,
                "Permission not granted - popup cancelled?",
                Toast.LENGTH_LONG
            ).show()
        } else {
            when (requestCode) {
                RequestCode.LocationPermission -> {
                    if (grantResults.any { it == PackageManager.PERMISSION_DENIED }) {
                        Toast.makeText(
                            applicationContext,
                            "Permission denied - can't start scan",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        viewModel.startScan()
                        Toast.makeText(
                            applicationContext,
                            "Permission granted - starting scan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestCode.EnableBluetooth -> {
                if (resultCode == RESULT_OK) {
                    viewModel.startScan()
                    Toast.makeText(
                        applicationContext,
                        "Bluetooth turned on - starting scan",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(
                        applicationContext,
                        "Bluetooth not turned on - can't start scan",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


}