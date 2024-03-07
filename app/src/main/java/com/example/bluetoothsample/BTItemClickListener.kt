package com.example.bluetoothsample

import android.bluetooth.BluetoothDevice

interface BTItemClickListener {
    fun myBTDevice(device: BluetoothDevice?)
}