package com.example.bluetoothsample

import android.bluetooth.BluetoothDevice

interface BluetoothListener {
    fun deviceFound(device: BluetoothDevice?)
    fun discoveryFinished()
}