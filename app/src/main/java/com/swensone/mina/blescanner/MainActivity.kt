package com.swensone.mina.blescanner

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.swensone.mina.blescanner.ble.BluetoothLeService
import com.swensone.mina.blescanner.ble.BluetoothLeServiceWrapper
import com.swensone.mina.blescanner.ble.ScanFailure
import com.swensone.mina.blescanner.ble.ScanStatus
import com.swensone.mina.blescanner.databinding.ActivityMainBinding
import com.swensone.mina.blescanner.permission.RequestCode
import com.swensone.mina.blescanner.permission.enableBluetooth
import com.swensone.mina.blescanner.permission.requestBleForAndroidS
import com.swensone.mina.blescanner.permission.requestLocationAndConnectPermissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var bluetoothLeServiceWrapper: BluetoothLeServiceWrapper

    private val viewModel: BleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // add the to the life scycle check @BluetoothLeServiceWrapper to see the on start event attached.
        lifecycle.addObserver(bluetoothLeServiceWrapper)

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


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
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