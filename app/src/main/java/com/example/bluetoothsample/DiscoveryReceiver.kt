package com.example.bluetoothsample

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat

class DiscoveryReceiver(private val listener: BluetoothListener) : BroadcastReceiver() {

   companion object{
       private const val TAG = "DiscoveryReceiver"
   }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                if (Build.VERSION.SDK_INT>Build.VERSION_CODES.R){
                    if (ActivityCompat.checkSelfPermission(
                            context!!.applicationContext,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }
                }else{
                    if (ActivityCompat.checkSelfPermission(
                            context!!.applicationContext,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }
                }

                    Log.d(TAG, "Device found: ${device?.name}, ${device?.address}")
                // Handle the discovered device as needed

                device.let {
                    listener.deviceFound(it)
                }
            }
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED->{
                listener.discoveryFinished()
                Log.d(TAG, "Bluetooth discovery process has been completed")

            }
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                Log.d(TAG, "Bluetooth device connected")
                // Handle the connected device as needed
            }
            BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED -> {
                Log.d(TAG, "Bluetooth device disconnect requested")
                // Handle the disconnect request as needed
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                Log.d(TAG, "Bluetooth device disconnected")
                // Handle the disconnected device as needed
            }
        }
    }
}


/*
 <action android:name="android.bluetooth.adapter.action.STATE_CHANGED" />
            <action android:name="android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED" />
            <action android:name="android.bluetooth.device.action.ACL_CONNECTED" />
            <action android:name="android.bluetooth.device.action.ACL_DISCONNECTED" />
            <action android:name="android.bluetooth.device.action.FOUND" />
            <action android:name="android.bluetooth.device.action.BOND_STATE_CHANGED" />
 */