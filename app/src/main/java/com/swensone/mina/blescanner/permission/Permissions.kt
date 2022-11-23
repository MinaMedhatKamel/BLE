package com.swensone.mina.blescanner.permission

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun Activity.enableBluetooth() {
    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    startActivityForResult(intent, RequestCode.EnableBluetooth)
}

object RequestCode {
    const val EnableBluetooth = 55001
    const val LocationPermission = 55002
}

val Context.hasLocationAndConnectPermissions: Boolean
    get() = (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ||
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) && hasPermission(Manifest.permission.BLUETOOTH_ADMIN) && hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

val Context.hasAndroidSPermission : Boolean
    @RequiresApi(Build.VERSION_CODES.S)
    get() = hasPermission(Manifest.permission.BLUETOOTH_SCAN) && hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

fun Context.hasPermission(
    permission: String,
): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/**
 * Shows the native Android permission request dialog.
 *
 * The result of the dialog will come back via [Activity.onRequestPermissionsResult] method.
 */
fun Activity.requestLocationAndConnectPermissions() {
    val permissions =
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_CONNECT)
    ActivityCompat.requestPermissions(this, permissions, RequestCode.LocationPermission)
}

@RequiresApi(Build.VERSION_CODES.S)
fun Activity.requestBleForAndroidS() {
    val permissions =
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    ActivityCompat.requestPermissions(this, permissions, RequestCode.LocationPermission)
}

